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
import com.tunjid.mutator.Mutator
import com.tunjid.mutator.StateProducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

fun <State: Any> CoroutineScope.stateFlowProducer(
    initial: State,
    started: SharingStarted = SharingStarted.WhileSubscribed(),
    mutationFlows: List<Flow<Mutation<State>>>
) = StateFlowProducer(
    scope = this,
    initialState = initial,
    started = started,
    mutationFlows = mutationFlows
)

/**
 * Manges state production for [State]
 */
class StateFlowProducer<State : Any> internal constructor(
    private val scope: CoroutineScope,
    initialState: State,
    started: SharingStarted = SharingStarted.WhileSubscribed(),
    mutationFlows: List<Flow<Mutation<State>>>
) : StateProducer<StateFlow<State>> {
    private val taskQueue = MutableSharedFlow<Flow<Mutation<State>>>()

    override val state = scope.produceState(
        initialState = initialState,
        started = started,
        mutationFlows = mutationFlows + taskQueue.flatMapMerge { it }
    )

    /**
     * Runs [block] in [CoroutineScope] that has a [Job] as the child of the [Job] of [scope].
     * This allows for the child [Job] to be passed a parameter in the [block] lambda allowing
     * for cancelling the coroutine that runs [block].
     */
    fun launch(
        block: suspend Mutator<State>.() -> Unit
    ) {
        scope.launch {
            taskQueue.emit(flow {
                block(this.asMutator())
            })
        }
    }
}

private fun <State : Any> FlowCollector<Mutation<State>>.asMutator() = object : Mutator<State> {
    override suspend fun mutate(mutation: Mutation<State>) {
        this@asMutator.emit(mutation)
    }
}