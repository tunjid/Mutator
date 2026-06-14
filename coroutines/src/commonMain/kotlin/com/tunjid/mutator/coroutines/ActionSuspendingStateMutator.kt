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
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * An [ActionStateMutator] that can be suspended.
 *
 * This interface combines the capabilities of [ActionStateMutator] and [SuspendingStateMutator],
 * allowing for state production to be driven by actions and tied to the lifecycle of the collector.
 */
interface ActionSuspendingStateMutator<in Action : Any, out State : Any> :
    ActionStateMutator<Action, State>,
    SuspendingStateMutator<State>

/**
 * Creates an [ActionSuspendingStateMutator] that derives its state from a [producer] which
 * consumes a stream of [Action]s.
 *
 * @param state The state holder used by the mutator. This is expected to be a mutable holder
 * (e.g. an `androidx.compose.runtime.mutableStateOf`-backed object, a `MutableStateFlow`, or any
 * other observable mutable container). The [producer] is re-invoked with this same instance on
 * every [SharingCommand.START], so any mutations applied to it persist across stop→restart
 * cycles. If you need immutable state semantics, use [actionStateFlowMutator] instead.
 *
 * [ActionSuspendingStateMutator.accept] is decoupled from the [started] lifecycle:
 * actions sent while no collector is active are buffered and processed when the producer
 * next becomes active. Action ordering is not guaranteed — use `launchMutationsIn` with a
 * key for sequential processing of related actions.
 *
 * @param started The [SharingStarted] strategy to control when the producer is active.
 * @param producer A suspending lambda that produces state changes. It is invoked when the
 * [started] strategy dictates that the producer should be active. It receives the [state] holder
 * and a [Flow] of actions.
 */
fun <Action : Any, State : Any> CoroutineScope.actionSuspendingStateMutator(
    state: State,
    started: SharingStarted = SharingStarted.WhileSubscribed(DEFAULT_STOP_TIMEOUT_MILLIS),
    producer: suspend CoroutineScope.(State, Flow<Action>) -> Unit,
): ActionSuspendingStateMutator<Action, State> = DelegatingActionSuspendingStateMutator(
    coroutineScope = this,
    state = state,
    started = started,
    producer = producer,
)

/**
 * Represents the receiver as a no-op [ActionSuspendingStateMutator]: its
 * [ActionSuspendingStateMutator.accept] and [ActionSuspendingStateMutator.collect] do nothing and
 * its state always holds the receiver.
 *
 * Typically useful for tests or previews.
 */
fun <Action : Any, State : Any> State.asNoOpActionSuspendingStateMutator(): ActionSuspendingStateMutator<Action, State> =
    NoOpActionSuspendingStateMutator(this)

/**
 * Returns whether the receiver is a no-op [ActionSuspendingStateMutator] — that is, either `null`
 * or an instance produced by [asNoOpActionSuspendingStateMutator]. Useful for short-circuiting when
 * no real state production is wired up, for example in tests or previews.
 */
fun <Action : Any, State : Any> ActionSuspendingStateMutator<Action, State>?.isNoOp() =
    this == null || this is NoOpActionSuspendingStateMutator

private class DelegatingActionSuspendingStateMutator<Action : Any, State : Any>(
    coroutineScope: CoroutineScope,
    state: State,
    started: SharingStarted,
    producer: suspend CoroutineScope.(State, Flow<Action>) -> Unit,
) : ActionSuspendingStateMutator<Action, State> {

    private val actions = Channel<Action>()

    val mutator = coroutineScope.suspendingStateMutator(
        state = state,
        started = started,
        producer = { state ->
            producer(
                state,
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

private class NoOpActionSuspendingStateMutator<Action : Any, State : Any>(
    override val state: State,
) : ActionSuspendingStateMutator<Action, State> {
    override val accept: (Action) -> Unit = {}
    override suspend fun collect() = Unit
}
