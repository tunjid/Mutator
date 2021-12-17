# Mutator

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

A `StateFlow` Mutator` of the above can be created by:

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
                        is Action.Subtract -> action.flow
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

Ultimately a `Mutator` serves to produce a stream of `State` from a stream of `Actions`,
the implementation of which is completely open ended.