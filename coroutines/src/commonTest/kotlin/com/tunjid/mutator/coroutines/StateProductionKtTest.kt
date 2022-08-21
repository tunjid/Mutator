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
import com.tunjid.mutator.Mutation
import com.tunjid.mutator.mutation
import com.tunjid.mutator.plus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StateProductionKtTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var scope: CoroutineScope
    private lateinit var eventMutations: MutableSharedFlow<Mutation<State>>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        scope = TestScope(testDispatcher)
        eventMutations = MutableSharedFlow()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun test_simple_state_production() = runTest {
        val state = scope.produceState(
            initialState = State(),
            started = SharingStarted.WhileSubscribed(),
            mutationFlows = listOf(
                eventMutations
            )
        )

        state.test {
            assertEquals(State(0.0), awaitItem())
            eventMutations.emit { copy(count = count + 1) }
            assertEquals(State(1.0), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun test_state_production_persists_after_unsubscribing() = runTest {
        val state = scope.produceState(
            initialState = State(),
            started = SharingStarted.WhileSubscribed(),
            mutationFlows = listOf(
                eventMutations
            )
        )

        // Subscribe the first time
        state.test {
            assertEquals(State(0.0), awaitItem())
            eventMutations.emit { copy(count = count + 1) }
            assertEquals(State(1.0), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        // Subscribe again. The state flow count should not be reset by the pipeline restarting
        state.test {
            assertEquals(State(1.0), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun test_state_production_with_merged_flows() = runTest {
        val state = scope.produceState(
            initialState = State(),
            started = SharingStarted.WhileSubscribed(),
            mutationFlows = listOf(
                eventMutations,
                flow {
                    delay(1000)
                    emit(mutation { copy(count = 3.0) })

                    delay(1000)
                    emit(mutation { copy(count = 7.0) })
                }
            )
        )

        state.test {
            assertEquals(State(0.0), awaitItem())

            advanceTimeBy(1200)
            assertEquals(State(3.0), awaitItem())

            advanceTimeBy(1200)
            assertEquals(State(7.0), awaitItem())

            eventMutations.emit { copy(count = 0.0) }
            assertEquals(State(0.0), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun test_state_change_addition() {
        val additionMutation = mutation<State> {
            copy(count = count + 1)
        } +
                mutation {
                    copy(count = count + 1)
                } +
                mutation {
                    copy(count = count + 1)
                }

        val state = additionMutation(State())

        assertEquals(State(3.0), state)
    }
}
