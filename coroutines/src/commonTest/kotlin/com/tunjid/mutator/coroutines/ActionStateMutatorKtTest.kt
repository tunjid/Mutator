/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tunjid.mutator.coroutines

import app.cash.turbine.test
import com.tunjid.mutator.ActionStateMutator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ActionStateMutatorKtTest {

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
    fun actionStateFlowMutatorRemembersLastValue() = runTest {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        val mutator = scope.actionStateFlowMutator<IntAction, State>(
            initialState = State(),
            started = SharingStarted.WhileSubscribed(),
            actionTransform = { actions ->
                actions.toMutationStream {
                    when (val action = type()) {
                        is IntAction.Add -> action.flow
                            .map { it.mutation }

                        is IntAction.Subtract -> action.flow
                            .map { it.mutation }
                    }
                }
            }
        )
        mutator.state.test {
            // Read first emission, should be the initial value
            assertEquals(State(), awaitItem())

            // Add 2, then wait till its processed
            mutator.accept(IntAction.Add(value = 2))
            assertEquals(expected = State(count = 2.0), actual = awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        // At this point, there are no subscribers and the upstream is cancelled
        assertEquals(
            expected = State(count = 2.0),
            actual = mutator.state.value
        )

        // Subscribe again
        mutator.state.test {
            // Read first emission, should be the last value seen
            assertEquals(expected = State(2.0), actual = awaitItem())

            // Subtract 5, then wait till its processed
            mutator.accept(IntAction.Subtract(value = 5))
            assertEquals(State(count = -3.0), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        scope.cancel()
    }

    @Test
    fun actionStateFlowMutatorSuspendsWithNoSubscribers() = runTest {
        val dispatcher = StandardTestDispatcher()
        val scope = CoroutineScope(SupervisorJob() + dispatcher)

        val mutator = scope.actionStateFlowMutator<IntAction, State>(
            initialState = State(),
            started = SharingStarted.WhileSubscribed(),
            actionTransform = { actions ->
                actions
                    .onEach { delay(1000) }
                    .toMutationStream {
                    when (val action = type()) {
                        is IntAction.Add -> action.flow
                            .map { it.mutation }

                        is IntAction.Subtract -> action.flow
                            .map { it.mutation }
                    }
                }
            }
        )

        mutator.accept(IntAction.Add(value = 1))
        mutator.accept(IntAction.Add(value = 1))
        mutator.accept(IntAction.Add(value = 1))
        mutator.accept(IntAction.Add(value = 1))

        mutator.state.test {
            // Read first emission, should be the initial value
            assertEquals(State(), awaitItem())

            // Queue should be drained
            dispatcher.scheduler.advanceTimeBy(1000)
            assertEquals(State(1.0), awaitItem())

            dispatcher.scheduler.advanceTimeBy(1000)
            assertEquals(State(2.0), awaitItem())

            dispatcher.scheduler.advanceTimeBy(1000)
            assertEquals(State(3.0), awaitItem())

            dispatcher.scheduler.advanceTimeBy(1000)
            assertEquals(State(4.0), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        scope.cancel()
    }

    @Test
    fun noOpOperatorCompiles() {
        val noOpActionStateMutator: ActionStateMutator<Action, StateFlow<State>> = State().asNoOpStateFlowMutator()
        noOpActionStateMutator.accept(IntAction.Add(value = 1))

        assertEquals(
            expected = State(),
            actual = noOpActionStateMutator.state.value
        )
    }
}
