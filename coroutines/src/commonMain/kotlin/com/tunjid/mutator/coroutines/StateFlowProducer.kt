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

import com.tunjid.mutator.Mutation
import com.tunjid.mutator.StateProducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

fun <State : Any> CoroutineScope.stateFlowProducer(
    initialState: State,
    started: SharingStarted = SharingStarted.WhileSubscribed(),
    inputs: List<Flow<Mutation<State>>>
) = StateFlowProducer(
    scope = this,
    initialState = initialState,
    started = started,
    inputs = inputs
)

/**
 * Manges state production for [State]
 */
class StateFlowProducer<State : Any> internal constructor(
    private val scope: CoroutineScope,
    initialState: State,
    started: SharingStarted = SharingStarted.WhileSubscribed(),
    inputs: List<Flow<Mutation<State>>>
) : StateProducer<StateFlow<State>> {
    private val mutationChannel = Channel<Mutation<State>>()
    private val mutationSender = FlowCollector(mutationChannel::send)

    override val state = scope.produceState(
        initialState = initialState,
        started = started,
        inputs = inputs + mutationChannel.receiveAsFlow()
    )

    /**
     * Runs [block] in parallel with any other tasks submitted to [launch]. [block] is only ever run if there is an
     * active collector of [state], and is managed under the [SharingStarted] policy passed to this [StateProducer].
     *
     * If there are no observers of [state] at the invocation of [launch], the Coroutine launched will suspend till
     * a collector begins to collect from [state].
     */
    fun launch(
        block: suspend FlowCollector<Mutation<State>>.() -> Unit
    ): Job = scope.launch {
        block(mutationSender)
    }
}
