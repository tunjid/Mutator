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

import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import app.cash.turbine.test
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class ActionSuspendingStateMutatorTest {

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
    fun actionStateMutatorWorksWithMutableState() = runTestWithSnapshotNotifications {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        val mutator = scope.actionSuspendingStateMutator<IntAction, SnapshotMutableState>(
            initialState = SnapshotMutableState(),
            producer = { state, actions ->
                actions.launchMutationsIn(productionScope = this) {
                    when (val action = type()) {
                        is IntAction.Add -> action.flow.collect {
                            state.count += it.value
                        }

                        is IntAction.Subtract -> action.flow.collect {
                            state.count -= it.value
                        }
                    }
                }
            },
        )

        scope.launch {
            mutator.collect()
        }

        snapshotFlow {
            SnapshotMutableState(count = mutator.state.count)
        }.test {
            // Read first emission, should be the initial value
            assertEquals(
                expected = SnapshotMutableState().toImmutable(),
                actual = awaitItem().toImmutable(),
            )

            // Add 2, then wait till its processed
            mutator.accept(IntAction.Add(value = 2))
            assertEquals(
                expected = SnapshotMutableState(count = 2.0).toImmutable(),
                actual = awaitItem().toImmutable(),
            )

            // Subtract 5, then wait till its processed
            mutator.accept(IntAction.Subtract(value = 5))
            assertEquals(
                expected = SnapshotMutableState(count = -3.0).toImmutable(),
                actual = awaitItem().toImmutable(),
            )

            cancelAndIgnoreRemainingEvents()
        }

        scope.cancel()
    }
}

/**
 * Runs tests with a snapshot observer similar to the compose runtime to allow validating changes
 * that happen in snapshot state or depend on using [snapshotFlow].
 */
fun runTestWithSnapshotNotifications(
    context: CoroutineContext = EmptyCoroutineContext,
    test: suspend TestScope.() -> Unit,
) = runTest(context) {
    val snapshotChanged = Channel<Unit>(1)
    launch {
        snapshotChanged.consumeEach {
            Snapshot.sendApplyNotifications()
        }
    }

    val snapshotObserver = Snapshot.registerGlobalWriteObserver {
        snapshotChanged.trySend(Unit)
    }

    try {
        test()
    } finally {
        snapshotObserver.dispose()
        snapshotChanged.close()
    }
}
