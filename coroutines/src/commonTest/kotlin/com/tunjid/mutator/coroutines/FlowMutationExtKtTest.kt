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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

class FlowMutationExtKtTest {

    // The TestScope receiver satisfies the `context(scope: CoroutineScope)` parameter.

    @Test
    fun launchedCollectInvokesBlockForEveryEmission() = runTest {
        val collected = mutableListOf<Int>()

        flowOf(1, 2, 3).launchedCollect { collected.add(it) }
        advanceUntilIdle()

        assertEquals(listOf(1, 2, 3), collected)
    }

    @Test
    fun launchedCollectLatestOnlyCompletesTheLatestEmission() = runTest {
        val completed = mutableListOf<Int>()

        flow {
            emit(1)
            emit(2)
            emit(3)
        }.launchedCollectLatest {
            // A slower-than-emission block: earlier emissions are cancelled mid-flight,
            // so only the latest emission runs to completion.
            delay(100)
            completed.add(it)
        }
        advanceUntilIdle()

        assertEquals(listOf(3), completed)
    }
}
