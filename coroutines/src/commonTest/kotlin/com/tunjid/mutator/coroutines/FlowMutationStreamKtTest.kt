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

import com.tunjid.mutator.mutationOf
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals


class FlowMutationStreamKtTest {
    @Test
    fun simple_stream_splitting() = runTest {
        val actions = listOf(
            IntAction.Add(value = 1),
            IntAction.Add(value = 1),
            IntAction.Subtract(value = 2),
            IntAction.Add(value = 7),
        )
        val testFlow = actions
            .asFlow()
            .toMutationStream {
                type().flow.map { it.mutation }
            }
            .reduceInto(State())

        assertEquals(
            State(count = 7.0),
            testFlow.take(count = actions.size + 1).last()
        )
    }

    @Test
    fun stream_splitting_with_common_flow_operator() = runTest {
        val actions = listOf(
            IntAction.Add(value = 1),
            IntAction.Add(value = 1),
            IntAction.Add(value = 1),
            IntAction.Add(value = 1),
            IntAction.Add(value = 1),

            IntAction.Subtract(value = 2),
            IntAction.Subtract(value = 2),
            IntAction.Subtract(value = 2),

            IntAction.Add(value = 7),
            IntAction.Add(value = 7),
            IntAction.Add(value = 7),
            IntAction.Add(value = 7),
        )

        val testFlow = actions
            .asFlow()
            .toMutationStream {
                type().flow
                    .distinctUntilChanged()
                    .map { it.mutation }
            }
            .reduceInto(State())

        assertEquals(
            State(count = 6.0),
            testFlow.take(count = actions.distinct().size + 1).last()
        )
    }

    @Test
    fun stream_splitting_with_different_flow_operators() = runTest {
        val actions = listOf(
            IntAction.Add(value = -1),
            IntAction.Add(value = 1),
            IntAction.Add(value = 2),

            IntAction.Subtract(value = -1),
            IntAction.Subtract(value = 1),
            IntAction.Subtract(value = 2),
        )

        val testFlow = actions
            .asFlow()
            .toMutationStream {
                when (val type = type()) {
                    is IntAction.Add -> type.flow
                        .filter { it.value > 0 && it.value % 2 == 0 }
                        .map { it.mutation }

                    is IntAction.Subtract -> type.flow
                        .filter { it.value > 0 && it.value % 2 != 0 }
                        .map { it.mutation }
                }
            }
            .reduceInto(State())

        assertEquals(
            State(count = 1.0),
            testFlow.take(count = 3).last()
        )
    }

    @Test
    fun stream_splitting_works_with_custom_keys() = runTest {
        val intActions = mutableListOf<IntAction>()
        val doubleActions = mutableListOf<DoubleAction>()

        val actions = listOf(
            IntAction.Add(value = 2),
            IntAction.Subtract(value = 1),

            DoubleAction.Multiply(value = 6.0),
            DoubleAction.Divide(value = 3.0),
        )

        val testFlow = actions
            .asFlow()
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
                        .onEach { intActions.add(it) }
                        .map { it.mutation }

                    is DoubleAction -> type.flow
                        .onEach { doubleActions.add(it) }
                        .map { it.mutation }
                }
            }
            .reduceInto(State())

        assertEquals(
            State(count = 2.0),
            testFlow.take(count = 5).last()
        )

        assertEquals(
            listOf(
                IntAction.Add(value = 2),
                IntAction.Subtract(value = 1),
            ),
            intActions
        )

        assertEquals(
            listOf(
                DoubleAction.Multiply(value = 6.0),
                DoubleAction.Divide(value = 3.0),
            ),
            doubleActions
        )
    }

    object Split {
        sealed class Action {
            object Regular : Action()
            object Delayed : Action()
        }

        data class State(
            val regularCount: Int = 0,
            val delayedCount: Int = 0,
        )
    }


    @Test
    fun stream_splitting_channels_can_be_configured() = runTest {
        val actions = listOf(
            Split.Action.Delayed,
            Split.Action.Delayed,
            Split.Action.Regular,
            Split.Action.Regular,
            Split.Action.Delayed,
            Split.Action.Regular,
        )
        val testFlow = actions
            .asFlow()
            .toMutationStream<Split.Action, Split.State>(
                capacity = 1,
                onBufferOverflow = BufferOverflow.SUSPEND,
            ) {
                when (val type = type()) {
                    is Split.Action.Regular -> type.flow
                        .map {
                            mutationOf { copy(regularCount = regularCount + 1) }
                        }

                    is Split.Action.Delayed -> type.flow
                        .map {
                            // Delay when processing this action
                            delay(1000)
                            mutationOf { copy(delayedCount = delayedCount + 1) }
                        }
                }
            }
            .reduceInto(Split.State())

        val output = testFlow.take(count = 7).toList()

        assertEquals(
            expected = Split.State(
                regularCount = 0,
                delayedCount = 0,
            ),
            actual = output[0]
        )

        assertEquals(
            expected = Split.State(
                regularCount = 1,
                delayedCount = 0,
            ),
            actual = output[1]
        )

        assertEquals(
            expected = Split.State(
                regularCount = 2,
                delayedCount = 0,
            ),
            actual = output[2]
        )

        // Buffer is full for delayed here, so the next action processed must be a delayed one.
        assertEquals(
            expected = Split.State(
                regularCount = 2,
                delayedCount = 1,
            ),
            actual = output[3]
        )

        // The buffer is now open for delayed actions, regular actions can be processed.
        assertEquals(
            expected = Split.State(
                regularCount = 3,
                delayedCount = 1,
            ),
            actual = output[4]
        )

        // Process remaining delayed actions.
        assertEquals(
            expected = Split.State(
                regularCount = 3,
                delayedCount = 2,
            ),
            actual = output[5]
        )

        assertEquals(
            expected = Split.State(
                regularCount = 3,
                delayedCount = 3,
            ),
            actual = output[6]
        )
    }
}