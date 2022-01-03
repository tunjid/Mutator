package com.tunjid.mutator.coroutines

import app.cash.turbine.test
import com.tunjid.mutator.Mutator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.*

class StateFlowMutatorKtTest {


    @Test
    fun stateFlowMutatorRemembersLastValue() = runTest {
        val scope = CoroutineScope(StandardTestDispatcher())
        val results = mutableListOf<State>()

        val stateFlowMutator = StateFlowMutator<Action, State>(
            scope = scope,
            initialState = State(),
            started = SharingStarted.WhileSubscribed(),
            transform = { actions ->
                actions.toMutationStream {
                    when (val action = type()) {
                        is Action.Add -> action.flow
                            .map { it.mutation }
                        is Action.Subtract -> action.flow
                            .map { it.mutation }
                    }
                }
            }
        )

        val mutator = object : Mutator<Action, Flow<State>> {
            override val state: Flow<State> = stateFlowMutator.state.onEach {
                results.add(it)
                println(results)
            }
            override val accept: (Action) -> Unit = stateFlowMutator.accept
        }


        // Read first emission, should be the initial value
        mutator.state.take(1).first()
        assertEquals(
            listOf(
                State(),
            ),
            results
        )
        stateFlowMutator.actions.subscriptionCount.first { it == 0 }
        println("Sub count a = ${stateFlowMutator.actions.subscriptionCount.value}")

        // Add 2, then wait till its processed
        mutator.accept(Action.Add(2))
        mutator.state.filter { it.count == 2 }.first()

        stateFlowMutator.actions.subscriptionCount.first { it == 0 }
        println("Sub count b = ${stateFlowMutator.actions.subscriptionCount.value}")

        assertEquals(
            listOf(
                State(),
                State(),
                State(2),
            ),
            results
        )

        // At this point, there are no subscribers and the upstream is cancelled
        // Add a new subscriber and subtract 4
//        delay(1000)
        stateFlowMutator.actions.subscriptionCount.first { it == 0 }
        println("Sub count c = ${stateFlowMutator.actions.subscriptionCount.value}")

        mutator.accept(Action.Subtract(4))
        mutator.state.filter { it.count < 0 }.first()

        // The mutator should remember the last value produced from the last subscriber
        assertEquals(
            listOf(
                State(),
                State(),
                State(2),
                State(2),
                State(-2),
            ),
            results
        )
        stateFlowMutator.actions.subscriptionCount.first { it == 0 }
        scope.cancel()
        println("DONE")

    }

    @Test
    fun a() = runTest {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val a = MutableSharedFlow<Int>(0)
        val b = a.stateIn(scope = scope, initialValue = 0, started = SharingStarted.WhileSubscribed())
        b.first()
        a.subscriptionCount.first { it == 0 }
    }

    @Test
    fun b() = runTest {
        val scope = CoroutineScope(StandardTestDispatcher())
        val results = mutableListOf<State>()

        val stateFlowMutator = StateFlowMutator<Action, State>(
            scope = this,
            initialState = State(),
            started = SharingStarted.WhileSubscribed(),
            transform = { actions ->
                actions.toMutationStream {
                    when (val action = type()) {
                        is Action.Add -> action.flow
                            .map { it.mutation }
                        is Action.Subtract -> action.flow
                            .map { it.mutation }
                    }
                }
            }
        )

        val mutator = object : Mutator<Action, Flow<State>> {
            override val state: Flow<State> = stateFlowMutator.state.onEach {
                results.add(it)
                println(results)
            }
            override val accept: (Action) -> Unit = stateFlowMutator.accept
        }

        mutator.state.test {
            assertEquals(State(), awaitItem())
            mutator.accept(Action.Add(2))
            assertEquals(State(2), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

    }
}