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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Class holding the context of the [Action] emitted that is being split out into
 * a [Mutation] [Flow].
 *
 * Use typically involves invoking [type] to identify the [Action] stream being transformed, and
 * subsequently invoking [flow] to perform a custom transformation on the split out [Flow].
 */
class TransformationContext<Action : Any>(
    private val type: Action,
    val backing: Flow<Action>,
) {

    /**
     * A convenience for the backing [Flow] of the [Action] subtype  from the parent [Flow]
     */
    @Suppress("unused", "UNCHECKED_CAST", "UnusedReceiverParameter")
    inline val <reified Subtype : Action> Subtype.flow: Flow<Subtype>
        get() = backing as Flow<Subtype>

    /**
     * the first [Action] of the specified type emitted from the parent [Flow]
     */
    fun type() = type
}

/**
 * Transforms a [Flow] of [Action] to a [Flow] of [Mutation] of [State], allowing for finer grained
 * transforms on subtypes of [Action]. This allows for certain actions to be processed differently
 * than others. For example: a certain action may need to only cause mutations on distinct
 * emissions, whereas other actions may need to use more complex [Flow] transformations like
 * [Flow.flatMapMerge] and so on. It does so by creating a [Channel] for each subtype.
 *
 * [capacity]: The capacity for the [Channel] created for each subtype. See the [Channel] factory
 * function for details.
 *
 * [onBufferOverflow]: The behavior of the [Channel] on overflow. See the [BufferOverflow]
 * for details.
 *
 * [keySelector]: The mapping for the [Action] to the key used to identify it. This is useful
 * for nested class hierarchies. By default each distinct type will be split out, but if you want
 * to treat certain subtypes as one type, this lets you do that.
 *
 * [transform]: a function for mapping independent [Flow]s of [Action] to [Flow]s of [State]
 * [Mutation]s
 * @see [splitByType]
 */
fun <Action : Any, State : Any> Flow<Action>.toMutationStream(
    capacity: Int = Channel.BUFFERED,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
    keySelector: (Action) -> Any = Any::defaultKeySelector,
    // Ergonomic hack to simulate multiple receivers
    transform: suspend TransformationContext<Action>.() -> Flow<Mutation<State>>,
): Flow<Mutation<State>> = splitByType(
    capacity = capacity,
    onBufferOverflow = onBufferOverflow,
    typeSelector = { it },
    keySelector = keySelector,
    transform = transform,
)

/**
 * Processes a [Flow] of [Action] in the provided [productionScope], allowing for finer grained
 * processing on subtypes of [Action]. This allows for certain actions to be processed differently
 * than others. For example: a certain action may need to be only processed on distinct
 * emissions, whereas other actions may need to use more complex [Flow] transformations like
 * [Flow.flatMapMerge] and so on. It does so by creating a [Channel] for each subtype.
 *
 * [productionScope]: The [CoroutineScope] for processing flows transformed.
 * [capacity]: The capacity for the [Channel] created for each subtype. See the [Channel] factory
 * function for details.
 *
 * [onBufferOverflow]: The behavior of the [Channel] on overflow. See the [BufferOverflow]
 * for details.
 *
 * [keySelector]: The mapping for the [Action] to the key used to identify it. This is useful
 * for nested class hierarchies. By default each distinct type will be split out, but if you want
 * to treat certain subtypes as one type, this lets you do that.
 *
 *  [transform]: a function for processing independent [Flow]s of [Action]. Each independent flow should be collected
 *  in this block.
 *
 *  @see [splitByType]
 */
fun <Action : Any> Flow<Action>.launchMutationsIn(
    productionScope: CoroutineScope,
    capacity: Int = Channel.BUFFERED,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
    keySelector: (Action) -> Any = Any::defaultKeySelector,
    transform: suspend TransformationContext<Action>.() -> Unit,
) {
    productionScope.launch {
        splitByType(
            capacity = capacity,
            onBufferOverflow = onBufferOverflow,
            typeSelector = { it },
            keySelector = keySelector,
            transform = transformation@{
                flow {
                    transform(this@transformation)
                    emit(Unit)
                }
            },
        )
            .collect()
    }
}

/**
 * Transforms a [Flow] of [Input] to a [Flow] of [Output] by splitting the original into [Flow]s
 * of type [Selector]. Each independent [Flow] of the [Selector] type can then be transformed
 * into a [Flow] of [Output].
 *
 * [typeSelector]: The mapping to the type the [Input] [Flow] should be split into
 *
 * [keySelector]: The mapping to the [Selector] to the key used to identify it. This is useful
 * for nested class hierarchies. By default each distinct type will be split out, but if you want
 * to treat certain subtypes as one type, this lets you do that.
 * [transform]: a function for mapping independent [Flow]s of [Selector] to [Flow]s of [Output]
 */
fun <Input : Any, Selector : Any, Output : Any> Flow<Input>.splitByType(
    capacity: Int,
    onBufferOverflow: BufferOverflow,
    typeSelector: (Input) -> Selector,
    keySelector: (Selector) -> Any = Any::defaultKeySelector,
    // Ergonomic hack to simulate multiple receivers
    transform: suspend TransformationContext<Selector>.() -> Flow<Output>,
): Flow<Output> =
    channelFlow mutationFlow@{
        val keysToFlowHolders = mutableMapOf<Any, FlowHolder<Selector>>()
        this@splitByType
            .collect { item ->
                val selected = typeSelector(item)
                val flowKey = keySelector(selected)
                when (val existingHolder = keysToFlowHolders[flowKey]) {
                    null -> {
                        val holder = FlowHolder(
                            capacity = capacity,
                            onBufferOverflow = onBufferOverflow,
                            firstEmission = selected,
                        )
                        keysToFlowHolders[flowKey] = holder
                        val context = TransformationContext(selected, holder.exposedFlow)
                        val mutationFlow = transform(context)
                        channel.send(mutationFlow)
                    }

                    else -> {
                        existingHolder.channel.send(selected)
                    }
                }
            }
        keysToFlowHolders.values.forEach { it.channel.close() }
    }
        .flatMapMerge(
            concurrency = Int.MAX_VALUE,
            transform = { it },
        )

/**
 * Container for representing a [Flow] of a subtype of [Action] that has been split out from
 * a [Flow] of [Action]
 */
private data class FlowHolder<Action>(
    val capacity: Int,
    val onBufferOverflow: BufferOverflow,
    val firstEmission: Action,
) {
    val channel: Channel<Action> = Channel(
        capacity = capacity,
        onBufferOverflow = onBufferOverflow,
    )
    val exposedFlow: Flow<Action> = channel
        .receiveAsFlow()
        .onStart { emit(firstEmission) }
}

private fun Any.defaultKeySelector(): Any = this::class
