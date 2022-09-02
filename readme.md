# Mutator

[![JVM Tests](https://github.com/tunjid/Mutator/actions/workflows/tests.yml/badge.svg)](https://github.com/tunjid/Mutator/actions/workflows/tests.yml)
![Mutator Core](https://img.shields.io/maven-central/v/com.tunjid.mutator/core?label=mutator-core)
![Mutator Coroutines](https://img.shields.io/maven-central/v/com.tunjid.mutator/coroutines?label=mutator-coroutines)

![badge][badge-ios]
![badge][badge-js]
![badge][badge-jvm]
![badge][badge-linux]
![badge][badge-windows]
![badge][badge-mac]
![badge][badge-tvos]
![badge][badge-watchos]

![Android Weekly Feature](https://androidweekly.net/issues/issue-510/badge)

Please note, this is not an official Google repository. It is a Kotlin multiplatform experiment
that makes no guarantees about API stability or long term support. None of the works presented here
are production tested, and should not be taken as anything more than its face value.

## Introduction

Mutator is a Kotlin multiplatform library that provides a suite of tools that help with producing state while following unidirectional data flow (UDF) principles. More specifically it provides implementations of the paradigm `newState = oldState + Δstate`.

Where `Δstate` represents state changes over time and is expressed in Kotlin with the type:

```kotlin
typealias Mutation<State> = State.() -> State
```

At the moment, there are two implementations:

```kotlin
fun <State : Any> CoroutineScope.produceState(
    initialState: State,
    started: SharingStarted,
    mutationFlows: List<Flow<Mutation<State>>>
): StateFlow<State>  
```

and 

```kotlin
fun <Action : Any, State : Any> CoroutineScope.actionStateFlowProducer(
    initialState: State,
    started: SharingStarted,
    mutationFlows: List<Flow<Mutation<State>>>,
    actionTransform: (Flow<Action>) -> Flow<Mutation<State>>
): Mutator<Action, StateFlow<State>>
```

Where a `Mutator<Action, StateFlow<State>>` exposes fields for
* state: `StateFlow<State>`
* action: `(Action) -> Unit`

`produceState` with is well suited for MVVM style applications and `actionStateFlowProducer` for MVI like approaches.

## Download

```kotlin
implementation("com.tunjid.mutator:core:version")
implementation("com.tunjid.mutator:coroutines:version")
```

Where the latest version is indicated by the badge at the top of this file.

## Examples and sample code

Please refer to the project [website](https://tunjid.github.io/Mutator/) for an interactive walk through of the problem space this library operates in and visual examples.


### `CoroutineScope.produceState`

`CoroutineScope.produceState` is a function that allows for mutating an initial state over time, by providing a `List` of `Flows` that contribute to state changes. A simple example follows:

```kotlin
data class SnailState(
    val progress: Float = 0f,
    val speed: Speed = Speed.One,
    val color: Color = Color.Blue,
    val colors: List<Color> = MutedColors.colors(false).map(::Color)
)

class SnailStateHolder(
    private val scope: CoroutineScope
) {

    private val speed: Flow<Speed> = scope.speedFlow()

    private val speedChanges: Flow<Mutation<Snail5State>> = speed
        .map { mutation { copy(speed = it) } }

    private val progressChanges: Flow<Mutation<Snail5State>> = speed
        .toInterval()
        .map { mutation { copy(progress = (progress + 1) % 100) } }

    private val userChanges = MutableSharedFlow<Mutation<Snail5State>>()

    val state: StateFlow<SnailState> = scope.produceState(
        initialState = Snail6State(),
        started = SharingStarted.WhileSubscribed(),
        mutationFlows = listOf(
            speedChanges,
            progressChanges,
            userChanges,
        )
    )

    fun setSnailColor(index: Int) {
        scope.launch {
            userChanges.emit { copy(color = colors[index]) }
        }
    }

    fun setProgress(progress: Float) {
        scope.launch {
            userChanges.emit { copy(progress = progress) }
        }
    }
}
```

### `CoroutineScope.actionStateFlowProducer`

The `actionStateFlowProducer` function transforms a `Flow` of `Action` into a `Flow` of `State` by first
mapping each `Action` into a `Mutation` of `State`, and then reducing the `Mutations` into an
initial state within the provided `CoroutineScope`.

The above is typically achieved with the `toMutationStream` extension function which allows for
the splitting of a source `Action` stream, into individual streams of each `Action` subtype. These
subtypes may then be transformed independently, for example, given a sealed class representative of
simple arithmetic actions:

```kotlin
sealed class Action {
    abstract val value: Int

    data class Add(override val value: Int) : Action()
    data class Subtract(override val value: Int) : Action()
}
```

and a `State` representative of the cumulative result of the application of those `Actions`:

```kotlin
data class State(
    val count: Int = 0
)
```

A `StateFlow` `Mutator` of the above can be created by:

```kotlin
        val mutator = scope.actionStateFlowProducer<Action, State>(
            initialState = State(),
            started = SharingStarted.WhileSubscribed(),
            transform = { actions ->
                actions.toMutationStream {
                    when (val action = type()) {
                        is Action.Add -> action.flow
                            .map {
                                mutation { copy(count = count + value) }
                            }
                        is Action.Subtract -> action.flow
                            .map {
                                mutation { copy(count = count - value) }
                            }
                    }
                }
            }
        )
```

Non trivially, given an application that fetches data for a query that can be sorted on demand. Its
`State` and `Action` may be defined by:

```kotlin
data class State(
    val comparator: Comparator<Item>,
    val items: List<Item> = listOf()
)

sealed class Action {
    data class Fetch(val query: Query) : Action()
    data class Sort(val comparator: Comparator<Item>) : Action()
}
```

In the above, fetching may need to be done consecutively, whereas only the most recently received
sorting request should be honored. A `StateFlow` `Mutator` for the above therefore may resemble:

```kotlin
val mutator = scope.actionStateFlowProducer<Action, State>(
    initialState = State(comparator = defaultComparator),
    started = SharingStarted.WhileSubscribed(),
    transform = { actions ->
        actions.toMutationStream {
            when (val action = type()) {
                is Action.Fetch -> action.flow
                    .map { fetch ->
                        val fetched = repository.get(fetch.query)
                        mutation {
                            copy(
                                items = (items + fetched).sortedWith(comparator),
                            )
                        }
                    }
                is Action.Sort -> action.flow
                    .mapLatest { sort ->
                        mutation {
                            copy(
                                comparator = sort.comparator,
                                items = items.sortedWith(comparator)
                            )
                        }
                    }
            }
        }
    }
)
```

In the above, by splitting the `Action` `Flow` into independent `Flows` of it's subtypes,
`Mutation` instances are easily generated that can be reduced into the current `State`.

A more robust example can be seen in the [Me](https://github.com/tunjid/me) project.

#### Nuanced use cases

Sometimes when splitting an `Action` into a `Mutation` stream, the `Action` type may need to be
split by it's super class and not it's actual class. Take the following `Action` and `State`
pairing:

```kotlin
data class State(
    val count: Double = 0.0
)

sealed class Action

sealed class IntAction: Action() {
    abstract val value: Int

    data class Add(override val value: Int) : IntAction()
    data class Subtract(override val value: Int) : IntAction()
}

sealed class DoubleAction: Action() {
    abstract val value: Double

    data class Divide(override val value: Double) : DoubleAction()
    data class Multiply(override val value: Double) : DoubleAction()
}
```

By default, all 4 `Actions` will need to have their resulting `Flows` defined. To help group them
into `Flows` of their super types, a `keySelector` can be used:

```kotlin
val actions = MutableSharedFlow<Action>()

actions
    .toMutationStream(
        keySelector = { action ->
            when (action) {
                is IntAction -> "IntAction"
                is DoubleAction -> "DoubleAction"
            }
        }
    ) {
        when (val type = type()) {
            is IntAction -> type.flow
                .map { it.mutation }
            is DoubleAction -> type.flow
                .map { it.mutation }
        }
    }
```

In the above the two distinct keys map to the `IntAction` and `DoubleAction` super types allowing
for granular control of the ensuing `Mutation` stream.

Ultimately a `Mutator` serves to produce a stream of `State` from a stream of `Actions`,
the implementation of which is completely open ended.

## License

    Copyright 2021 Google LLC
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        https://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[badge-android]: http://img.shields.io/badge/-android-6EDB8D.svg?style=flat

[badge-jvm]: http://img.shields.io/badge/-jvm-DB413D.svg?style=flat

[badge-js]: http://img.shields.io/badge/-js-F8DB5D.svg?style=flat

[badge-js-ir]: https://img.shields.io/badge/support-[IR]-AAC4E0.svg?style=flat

[badge-nodejs]: https://img.shields.io/badge/-nodejs-68a063.svg?style=flat

[badge-linux]: http://img.shields.io/badge/-linux-2D3F6C.svg?style=flat

[badge-windows]: http://img.shields.io/badge/-windows-4D76CD.svg?style=flat

[badge-wasm]: https://img.shields.io/badge/-wasm-624FE8.svg?style=flat

[badge-apple-silicon]: http://img.shields.io/badge/support-[AppleSilicon]-43BBFF.svg?style=flat

[badge-ios]: http://img.shields.io/badge/-ios-CDCDCD.svg?style=flat

[badge-mac]: http://img.shields.io/badge/-macos-111111.svg?style=flat

[badge-watchos]: http://img.shields.io/badge/-watchos-C0C0C0.svg?style=flat

[badge-tvos]: http://img.shields.io/badge/-tvos-808080.svg?style=flat
