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
import kotlin.test.assertSame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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
            state = SnapshotMutableState(),
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

    @Test
    fun producerRestartsWhenResubscribedAfterStopTimeout() = runTest {
        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        var producerInvocations = 0

        val mutator = scope.actionSuspendingStateMutator<IntAction, SnapshotMutableState>(
            state = SnapshotMutableState(),
            producer = { _, _ ->
                producerInvocations++
                awaitCancellation()
            },
        )

        val firstCollector = scope.launch { mutator.collect() }
        advanceUntilIdle()
        assertEquals(1, producerInvocations)

        firstCollector.cancel()
        // Cross the 5s WhileSubscribed timeout so SharingCommand.STOP fires.
        advanceTimeBy(6_000)
        runCurrent()

        val secondCollector = scope.launch { mutator.collect() }
        advanceUntilIdle()
        assertEquals(2, producerInvocations)

        secondCollector.cancel()
        scope.cancel()
    }

    @Test
    fun actionsSentWhileUnsubscribedAreDrainedAfterResubscribe() = runTest {
        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        val stateHolder = SnapshotMutableState()

        val mutator = scope.actionSuspendingStateMutator<IntAction, SnapshotMutableState>(
            state = stateHolder,
            producer = { state, actions ->
                actions.launchMutationsIn(productionScope = this) {
                    when (val action = type()) {
                        is IntAction.Add -> action.flow.collect { state.count += it.value }
                        is IntAction.Subtract -> action.flow.collect { state.count -= it.value }
                    }
                }
            },
        )

        val firstCollector = scope.launch { mutator.collect() }
        advanceUntilIdle()

        mutator.accept(IntAction.Add(value = 1))
        advanceUntilIdle()
        assertEquals(1.0, stateHolder.count)

        firstCollector.cancel()
        advanceTimeBy(6_000)
        runCurrent()

        // Producer is now cancelled. These accepts suspend on the rendezvous channel.
        mutator.accept(IntAction.Add(value = 2))
        mutator.accept(IntAction.Add(value = 3))
        advanceUntilIdle()
        assertEquals(1.0, stateHolder.count)

        // Resubscribing restarts the producer and drains the buffered sends.
        val secondCollector = scope.launch { mutator.collect() }
        advanceUntilIdle()
        assertEquals(6.0, stateHolder.count)

        secondCollector.cancel()
        scope.cancel()
    }

    @Test
    fun stateHolderIsReusedAcrossStopRestartCycles() = runTest {
        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        val stateHolder = SnapshotMutableState()

        val mutator = scope.actionSuspendingStateMutator<IntAction, SnapshotMutableState>(
            state = stateHolder,
            producer = { state, actions ->
                actions.launchMutationsIn(productionScope = this) {
                    when (val action = type()) {
                        is IntAction.Add -> action.flow.collect { state.count += it.value }
                        is IntAction.Subtract -> action.flow.collect { state.count -= it.value }
                    }
                }
            },
        )

        val firstCollector = scope.launch { mutator.collect() }
        advanceUntilIdle()
        mutator.accept(IntAction.Add(value = 5))
        advanceUntilIdle()
        assertEquals(5.0, stateHolder.count)
        assertSame(stateHolder, mutator.state)

        firstCollector.cancel()
        advanceTimeBy(6_000)
        runCurrent()

        val secondCollector = scope.launch { mutator.collect() }
        advanceUntilIdle()
        // The mutable holder survived the stop→restart cycle: same instance, same value.
        assertSame(stateHolder, mutator.state)
        assertEquals(5.0, stateHolder.count)

        secondCollector.cancel()
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
