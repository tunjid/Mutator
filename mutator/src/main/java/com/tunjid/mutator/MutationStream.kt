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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.onStart

/**
 * Transforms a [Flow] of [Action] to a [Flow] of [Mutation] of [State], allowing for finer grained
 * transforms on subtypes of [Action]. This allows for certain actions to be processed differently
 * than others. For example: a certain action may need to only cause mutations on distinct
 * emissions, whereas other actions may need to use more complex [Flow] transformations like
 * [Flow.flatMapMerge] and so on.
 */
fun <Action : Any, State : Any> Flow<Action>.toMutationStream(
    // Ergonomic hack to simulate multiple receivers
    transform: TransformationContext<Action>.() -> Flow<Mutation<State>>
): Flow<Mutation<State>> =
    channelFlow<Flow<Mutation<State>>> mutationFlow@{
        val keysToFlowHolders = mutableMapOf<String, FlowHolder<Action>>()
        this@toMutationStream
            .collect { item ->
                val flowKey = item.flowKey
                when (val existingHolder = keysToFlowHolders[flowKey]) {
                    null -> {
                        val holder = FlowHolder(item)
                        keysToFlowHolders[flowKey] = holder
                        val emission = TransformationContext(item, holder.exposedFlow)
                        val mutationFlow = transform(emission)
                        channel.send(mutationFlow)
                    }
                    else -> {
                        // Wait for downstream to be connected
                        existingHolder.internalSharedFlow.subscriptionCount.first { it > 0 }
                        existingHolder.internalSharedFlow.emit(item)
                    }
                }
            }
    }
        .flatMapMerge(
            concurrency = Int.MAX_VALUE,
            transform = { it }
        )

/**
 * Class holding context of the [Action] emitted that is being split out into a [Mutation] [Flow].
 * Use typically involves invoking [type] to identify the stream being transformed, and
 * subsequently invoking [flow] to perform a custom transformation on the split out [Flow].
 */
data class TransformationContext<Action : Any>(
    private val type: Action,
    val backing: Flow<Action>
) {
    inline val <reified Subtype : Action> Subtype.flow: Flow<Subtype>
        get() = backing as Flow<Subtype>

    fun type() = type
}

/**
 * Container for representing a [Flow] of a subtype of [Action] that has been split out from
 * a [Flow] of [Action]
 */
private data class FlowHolder<Action>(
    val firstEmission: Action,
) {
    val internalSharedFlow: MutableSharedFlow<Action> = MutableSharedFlow()
    val exposedFlow: Flow<Action> = internalSharedFlow.onStart { emit(firstEmission) }
}

/**
 * Uniquely identifies a class using it's kotlin class simple name.
 * Note the the above makes it unsuitable for anonymous classes.
 */
private val Any.flowKey get() = this::class.simpleName!!
