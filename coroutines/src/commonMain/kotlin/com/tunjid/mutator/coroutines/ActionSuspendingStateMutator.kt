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

import com.tunjid.mutator.ActionStateMutator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * An [ActionStateMutator] that can be suspended.
 *
 * This interface combines the capabilities of [ActionStateMutator] and [SuspendingStateMutator],
 * allowing for state production to be driven by actions and tied to the lifecycle of the collector.
 */
interface ActionSuspendingStateMutator<Action : Any, State : Any> :
    ActionStateMutator<Action, State>,
    SuspendingStateMutator<State>

/**
 * Creates an [ActionSuspendingStateMutator] that derives its state from a [producer] which
 * consumes a stream of [Action]s.
 *
 * @param initialState The initial state of the mutator.
 * @param started The [SharingStarted] strategy to control when the producer is active.
 * @param producer A suspending lambda that produces state changes. It is invoked when the
 * [started] strategy dictates that the producer should be active. It receives the current state
 * and a [Flow] of actions.
 */
fun <Action : Any, State : Any> CoroutineScope.actionSuspendingStateMutator(
    initialState: State,
    started: SharingStarted = SharingStarted.WhileSubscribed(DEFAULT_STOP_TIMEOUT_MILLIS),
    producer: suspend CoroutineScope.(State, Flow<Action>) -> Unit,
): ActionSuspendingStateMutator<Action, State> = DelegatingActionSuspendingStateMutator(
    coroutineScope = this,
    initialState = initialState,
    started = started,
    producer = producer,
)

private class DelegatingActionSuspendingStateMutator<Action : Any, State : Any>(
    coroutineScope: CoroutineScope,
    initialState: State,
    started: SharingStarted,
    producer: suspend CoroutineScope.(State, Flow<Action>) -> Unit,
) : ActionSuspendingStateMutator<Action, State> {

    private val actions = Channel<Action>()

    val mutator = coroutineScope.suspendingStateMutator(
        state = initialState,
        started = started,
        producer = { currentState ->
            producer(
                currentState,
                actions.receiveAsFlow(),
            )
        },
    )

    override val state: State
        get() = mutator.state

    override val accept: (Action) -> Unit = { action ->
        coroutineScope.launch {
            actions.send(action)
        }
    }

    override suspend fun collect() =
        mutator.collect()
}
