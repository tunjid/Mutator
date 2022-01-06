package com.tunjid.mutator.coroutines

import app.cash.turbine.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StateFlowMutatorKtTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun stateFlowMutatorRemembersLastValue() = runTest {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        val mutator = stateFlowMutator<Action, State>(
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
        mutator.state.test {
            // Read first emission, should be the initial value
            assertEquals(State(), awaitItem())

            // Add 2, then wait till its processed
            mutator.accept(Action.Add(2))
            assertEquals(State(2), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        // At this point, there are no subscribers and the upstream is cancelled
        assertEquals(
            expected = State(2),
            actual = mutator.state.value
        )

        // Subscribe again
        mutator.state.test {
            // Read first emission, should be the last value seen
            assertEquals(State(2), awaitItem())

            // Add 2, then wait till its processed
            mutator.accept(Action.Subtract(5))
            assertEquals(State(-3), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        scope.cancel()
    }
}
