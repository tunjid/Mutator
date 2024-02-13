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

import com.tunjid.mutator.ActionStateProducer
import com.tunjid.mutator.Mutation
import com.tunjid.mutator.StateHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

typealias SuspendingStateHolder<State> = StateHolder<suspend () -> State>

/**
 * Defines a [ActionStateProducer] to convert a [Flow] of [Action] into a [StateFlow] of [State].
 *
 * [this@actionStateFlowProducer]: The [CoroutineScope] for the resulting [StateFlow]. Any [Action]s sent if there are no
 * subscribers to the output [StateFlow] will suspend until there is as least one subscriber.
 *
 * [initialState]: The seed state for the resulting [StateFlow].
 *
 * [started]: Semantics for the "hotness" of the output [StateFlow] @see [Flow.stateIn]
 *
 * [stateTransform]: Further transformations o be applied to the output [StateFlow]
 *
 * [actionTransform]: Defines the transformations to the [Action] [Flow] to create [Mutation]s
 * of state that will be reduced into the [initialState]. This is often achieved through the
 * [toMutationStream] [Flow] extension function.
 */
fun <Action : Any, State : Any> CoroutineScope.actionStateFlowProducer(
    initialState: State,
    started: SharingStarted = SharingStarted.WhileSubscribed(DEFAULT_STOP_TIMEOUT_MILLIS),
    inputs: List<Flow<Mutation<State>>> = listOf(),
    stateTransform: (Flow<State>) -> Flow<State> = { it },
    actionTransform: SuspendingStateHolder<State>.(Flow<Action>) -> Flow<Mutation<State>> = { emptyFlow() }
): ActionStateProducer<Action, StateFlow<State>> = ActionStateFlowProducer(
    coroutineScope = this,
    initialState = initialState,
    started = started,
    inputs = inputs,
    stateTransform = stateTransform,
    actionTransform = actionTransform
)

private class ActionStateFlowProducer<Action : Any, State : Any>(
    coroutineScope: CoroutineScope,
    initialState: State,
    started: SharingStarted,
    inputs: List<Flow<Mutation<State>>>,
    stateTransform: (Flow<State>) -> Flow<State> = { it },
    actionTransform: SuspendingStateHolder<State>.(Flow<Action>) -> Flow<Mutation<State>>
) : ActionStateProducer<Action, StateFlow<State>>,
    suspend () -> State {

    private val actions = Channel<Action>()

    // Allows for reading the current state in concurrent contexts.
    // Note that it suspends to prevent reading state before this class is fully constructed
    private val stateReader = object : SuspendingStateHolder<State> {
        override val state: suspend () -> State = this@ActionStateFlowProducer
    }

    override val state: StateFlow<State> =
        coroutineScope.produceState(
            initialState = initialState,
            started = started,
            stateTransform = stateTransform,
            inputs = inputs + actionTransform(stateReader, actions.receiveAsFlow())
        )

    override val accept: (Action) -> Unit = { action ->
        coroutineScope.launch {
            actions.send(action)
        }
    }

    // There's no need to suspend when reading the value, however the caller must suspend to
    // invoke it
    override suspend fun invoke(): State = state.value
}

/**
 * Represents a type as a StateFlowMutator of itself with no op [Action]s.
 *
 * This is typically useful for testing or previews
 */
fun <Action : Any, State : Any> State.asNoOpStateFlowMutator(): ActionStateProducer<Action, StateFlow<State>> =
    object : ActionStateProducer<Action, StateFlow<State>> {
        override val accept: (Action) -> Unit = {}
        override val state: StateFlow<State> = MutableStateFlow(this@asNoOpStateFlowMutator)
    }

private const val DEFAULT_STOP_TIMEOUT_MILLIS = 5000L
