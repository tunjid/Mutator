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

package com.tunjid.mutator

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FlowMutationStreamKtTest {
    @Test
    fun `simple stream splitting`() = runBlocking {
        val actions = listOf(
            Action.Add(value = 1),
            Action.Add(value = 1),
            Action.Subtract(value = 2),
            Action.Add(value = 7),
        )
        val testFlow = actions
            .asFlow()
            .toMutationStream {
                type().flow.map { it.mutation }
            }
            .reduceInto(State())

        assertEquals(
            State(count = 7),
            testFlow.take(count = actions.size + 1).last()
        )
    }

    @Test
    fun `stream splitting with common flow operator`() = runBlocking {
        val actions = listOf(
            Action.Add(value = 1),
            Action.Add(value = 1),
            Action.Add(value = 1),
            Action.Add(value = 1),
            Action.Add(value = 1),

            Action.Subtract(value = 2),
            Action.Subtract(value = 2),
            Action.Subtract(value = 2),

            Action.Add(value = 7),
            Action.Add(value = 7),
            Action.Add(value = 7),
            Action.Add(value = 7),
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
            State(count = 6),
            testFlow.take(count = actions.distinct().size + 1).last()
        )
    }

    @Test
    fun `stream splitting with different flow operators`() = runBlocking {
        val actions = listOf(
            Action.Add(value = -1),
            Action.Add(value = 1),
            Action.Add(value = 2),

            Action.Subtract(value = -1),
            Action.Subtract(value = 1),
            Action.Subtract(value = 2),
        )

        val testFlow = actions
            .asFlow()
            .toMutationStream {
                when(val type = type()) {
                    is Action.Add -> type.flow
                        .filter { it.value > 0 && it.value % 2 == 0 }
                        .map { it.mutation }
                    is Action.Subtract -> type.flow
                        .filter { it.value > 0 && it.value % 2 != 0 }
                        .map { it.mutation }
                }
            }
            .reduceInto(State())

        assertEquals(
            State(count = 1),
            testFlow.take(count = 3).last()
        )
    }
}