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
import kotlinx.coroutines.flow.*

/**
 * Class holding the context of the [Action] emitted that is being split out into
 * a [Mutation] [Flow].
 *
 * Use typically involves invoking [type] to identify the [Action] stream being transformed, and
 * subsequently invoking [flow] to perform a custom transformation on the split out [Flow].
 */
data class TransformationContext<Action : Any>(
    private val type: Action,
    val backing: Flow<Action>
) {

    /**
     * A convenience for the backing [Flow] of the [Action] subtype  from the parent [Flow]
     */
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
 * [Flow.flatMapMerge] and so on.
 *
 *  [transform]: a function for mapping independent [Flow]s of [Action] to [Flow]s of [State]
 *  [Mutation]s
 * @see [splitByType]
 */
fun <Action : Any, State : Any> Flow<Action>.toMutationStream(
    // Ergonomic hack to simulate multiple receivers
    transform: TransformationContext<Action>.() -> Flow<Mutation<State>>
): Flow<Mutation<State>> = splitByType(
    selector = { it },
    transform = transform
)

/**
 * Transforms a [Flow] of [Input] to a [Flow] of [Output] by splitting the original into [Flow]s
 * of type [Selector]. Each independent [Flow] of the [Selector] type can then be transformed
 * into a [Flow] of [Output].
 *
 * [selector]: The mapping to the type the [Input] [Flow] should be split into
 * [transform]: a function for mapping independent [Flow]s of [Selector] to [Flow]s of [Output]
 */
fun <Input : Any, Selector : Any, Output : Any> Flow<Input>.splitByType(
    selector: (Input) -> Selector,
    // Ergonomic hack to simulate multiple receivers
    transform: TransformationContext<Selector>.() -> Flow<Output>
): Flow<Output> =
    channelFlow<Flow<Output>> mutationFlow@{
        val keysToFlowHolders = mutableMapOf<String, FlowHolder<Selector>>()
        this@splitByType
            .collect { item ->
                val mapped = selector(item)
                val flowKey = mapped::class.qualifiedName
                    ?: throw IllegalArgumentException("Only well defined classes can be split")
                when (val existingHolder = keysToFlowHolders[flowKey]) {
                    null -> {
                        val holder = FlowHolder(mapped)
                        keysToFlowHolders[flowKey] = holder
                        val emission = TransformationContext(mapped, holder.exposedFlow)
                        val mutationFlow = transform(emission)
                        channel.send(mutationFlow)
                    }
                    else -> {
                        // Wait for downstream to be connected
                        existingHolder.internalSharedFlow.subscriptionCount.first { it > 0 }
                        existingHolder.internalSharedFlow.emit(mapped)
                    }
                }
            }
    }
        .flatMapMerge(
            concurrency = Int.MAX_VALUE,
            transform = { it }
        )

fun <State : Any> Flow<Mutation<State>>.reduceInto(initialState: State): Flow<State> =
    scan(initialState) { state, mutation -> mutation.mutate(state) }

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
