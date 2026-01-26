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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn

/**
 * Produces a [StateFlow] by merging [inputs] and reducing them into an
 * [initialState] state within [this] [CoroutineScope]
 */
fun <State : Any> CoroutineScope.mutateState(
    initialState: State,
    started: SharingStarted,
    inputs: List<Flow<Mutation<State>>>,
    stateTransform: (Flow<State>) -> Flow<State> = { it },
): StateFlow<State> {
    // Set the seed for the state
    var seed = initialState

    // Use the flow factory function to capture the seed variable
    return stateTransform(
        flow {
            emitAll(
                merge(*inputs.toTypedArray())
                    // Reduce into the seed so if resubscribed, the last value of state is persisted
                    // when the flow pipeline is started again
                    .reduceInto(seed)
                    // Set seed after each emission
                    .onEach { seed = it },
            )
        },
    )
        .stateIn(
            scope = this,
            started = started,
            initialValue = seed,
        )
}

fun <State : Any> Flow<Mutation<State>>.reduceInto(initialState: State): Flow<State> =
    scan(initialState) { state, mutation -> mutation(state) }
