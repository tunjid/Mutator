package com.tunjid.mutator.coroutines

import com.tunjid.mutator.Mutator
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Test

class StateFlowMutatorKtTest {

    @Test
    fun stateFlowMutatorRemembersLastValue() = runBlocking {
        val testScope = TestCoroutineScope()
        val results = mutableListOf<State>()

        val mutator = stateFlowMutator<Action, State>(
            scope = testScope,
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
        ).let { mutator ->
            object : Mutator<Action, Flow<State>> {
                override val state: Flow<State> = mutator.state.onEach {
                    results.add(it)
                }
                override val accept: (Action) -> Unit = mutator.accept
            }
        }

        // Read first emission, should be the initial value
        mutator.state.take(1).first()
        assertEquals(
            listOf(
                State(),
            ),
            results
        )

        // Add 2, then wait till its processed
        mutator.accept(Action.Add(2))
        mutator.state.filter { it.count == 2 }.first()
        assertEquals(
            listOf(
                State(),
                State(2),
            ),
            results
        )

        // At this point, there are no subscribers and the upstream is cancelled
        // Add a new subscriber and subtract 4
        mutator.accept(Action.Subtract(4))
        mutator.state.filter { it.count < 0 }.first()

        // The mutator should remember the last value produced from the last subscriber
        assertEquals(
            listOf(
                State(),
                State(2),
                State(-2),
            ),
            results
        )
    }
}