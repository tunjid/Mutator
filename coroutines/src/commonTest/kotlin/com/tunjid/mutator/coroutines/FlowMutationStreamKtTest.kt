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

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals


class FlowMutationStreamKtTest {
    @Test
    fun `simple stream splitting`() = runTest {
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
    fun `stream splitting with common flow operator`() = runTest {
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
    fun `stream splitting with different flow operators`() = runTest {
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
    fun `stream splitting works with custom keys`() = runTest {
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
}