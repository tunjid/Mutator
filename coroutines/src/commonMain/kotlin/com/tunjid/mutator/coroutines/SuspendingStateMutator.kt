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

import com.tunjid.mutator.StateMutator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * A [StateMutator] that can be suspended.
 *
 * This interface allows for the state production to be tied to the lifecycle of the collector.
 * When [collect] is called, it signals that the state production should be active.
 */
interface SuspendingStateMutator<State : Any> : StateMutator<State> {
    /**
     * Suspends and keeps the upstream producer active for as long as this function
     * is running.
     *
     * Callers should launch this in a coroutine. When the coroutine is cancelled,
     * the reference count is decremented.
     * * This function does not return normally; it suspends until cancellation.
     */
    suspend fun collect()
}

/**
 * Creates a [SuspendingStateMutator] that derives its state from a [producer].
 *
 * @param initialState The initial state of the mutator.
 * @param started The [SharingStarted] strategy to control when the producer is active.
 * @param producer A suspending lambda that produces state changes. It is invoked when the
 * [started] strategy dictates that the producer should be active.
 */
fun <State : Any> CoroutineScope.suspendingStateMutator(
    initialState: State,
    started: SharingStarted = SharingStarted.WhileSubscribed(DEFAULT_STOP_TIMEOUT_MILLIS),
    producer: suspend CoroutineScope.(State) -> Unit,
): SuspendingStateMutator<State> {
    val subscriptionCount = MutableStateFlow(0)
    val mutator = RefCountingSuspendingStateMutator(
        state = initialState,
        subscriptionCount = subscriptionCount,
    )

    launch {
        var producerJob: Job? = null
        started.command(subscriptionCount).collect { command ->
            when (command) {
                SharingCommand.START -> {
                    if (producerJob == null || producerJob?.isActive == false) {
                        producerJob = launch { producer(initialState) }
                    }
                }

                SharingCommand.STOP,
                SharingCommand.STOP_AND_RESET_REPLAY_CACHE,
                -> {
                    producerJob?.cancel()
                    producerJob = null
                }
            }
        }
    }

    return mutator
}

private class RefCountingSuspendingStateMutator<State : Any>(
    override val state: State, // The stable state holder
    private val subscriptionCount: MutableStateFlow<Int>,
) : SuspendingStateMutator<State> {
    override suspend fun collect() {
        try {
            subscriptionCount.update { it + 1 }
            awaitCancellation()
        } finally {
            subscriptionCount.update { it - 1 }
        }
    }
}
