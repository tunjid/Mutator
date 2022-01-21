# Mutator

[![JVM Tests](https://github.com/tunjid/Mutator/actions/workflows/tests.yml/badge.svg)](https://github.com/tunjid/Mutator/actions/workflows/tests.yml)
![Mutator Core](https://img.shields.io/maven-central/v/com.tunjid.mutator/core?label=mutator-core)
![Mutator Coroutines](https://img.shields.io/maven-central/v/com.tunjid.mutator/coroutines?label=mutator-coroutines)

Please note, this is not an official Google repository. It is a Kotlin multiplatform experiment
that makes no guarantees about API stability or long term support. None of the works presented here
are production tested, and should not be taken as anything more than its face value.

## Introduction

A `Mutator` is an abstract data type declaration of the form:

```kotlin
interface Mutator<Action : Any, State : Any> {
    val state: State
    val accept: (Action) -> Unit
}
```

where `Action` defines an input type that yields production of the `State` output type. In other
words, a `Mutator` represents a production pipeline of `State` which may be altered by input of
`Action`.

The unit of change for processing an `Action` into a new `State` is a `Mutation`, a data class
hosting a lambda with the existing `State` as a receiver, that when invoked produces a
new `State`.

```kotlin
data class Mutation<T : Any>(
    val mutate: T.() -> T
)
```

## Implementations

### `stateFlowMutator`

The `stateFlowMutator` function transforms a `Flow` of `Action` into a `Flow` of `State` by first
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
        val mutator = stateFlowMutator<Action, State>(
            scope = scope,
            initialState = State(),
            started = SharingStarted.WhileSubscribed(),
            transform = { actions ->
                actions.toMutationStream {
                    when (val action = type()) {
                        is Action.Add -> action.flow
                            .map {
                                Mutation { copy(count = count + value) }
                            }
                        is Action.Subtract -> action.flow
                            .map {
                                Mutation { copy(count = count - value) }
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
val mutator = stateFlowMutator<Action, State>(
    scope = scope,
    initialState = State(comparator = defaultComparator),
    started = SharingStarted.WhileSubscribed(),
    transform = { actions ->
        actions.toMutationStream {
            when (val action = type()) {
                is Action.Fetch -> action.flow
                    .map { fetch ->
                        val fetched = repository.get(fetch.query)
                        Mutation {
                            copy(
                                items = (items + fetched).sortedWith(comparator),
                            )
                        }
                    }
                is Action.Sort -> action.flow
                    .flatMapLatest { sort ->
                        flowOf(
                            Mutation {
                                copy(
                                    comparator = sort.comparator,
                                    items = items.sortedWith(comparator)
                                )
                            }
                        )
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

```
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

```
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