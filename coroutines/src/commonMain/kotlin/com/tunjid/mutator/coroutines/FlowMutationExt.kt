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
import com.tunjid.mutator.mutationOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

/**
 * Maps each emission of [T] into a single mutation of [State]
 * @see [Flow.map]
 */
inline fun <T, State> Flow<T>.mapToMutation(
    crossinline mapper: State.(T) -> State,
): Flow<Mutation<State>> =
    map { mutationOf { mapper(it) } }

/**
 * Maps the latest emission of [T] into a single mutation of [State]
 * @see [Flow.mapLatest]
 */
inline fun <T, State> Flow<T>.mapLatestToMutation(
    crossinline mapper: State.(T) -> State,
): Flow<Mutation<State>> =
    mapLatest { mutationOf { mapper(it) } }

/**
 * Maps each emission of [T] into multiple mutations of [State]
 * @see [Flow.flatMapConcat]
 */
inline fun <T, State> Flow<T>.mapToManyMutations(
    crossinline block: suspend FlowCollector<Mutation<State>>.(T) -> Unit,
): Flow<Mutation<State>> =
    flatMapConcat { flow { block(it) } }

/**
 * Maps the latest emission of [T] into multiple mutations of [State]
 * @see [Flow.flatMapLatest]
 */
inline fun <T, State> Flow<T>.mapLatestToManyMutations(
    crossinline block: suspend FlowCollector<Mutation<State>>.(T) -> Unit,
): Flow<Mutation<State>> =
    flatMapLatest { flow { block(it) } }

/**
 * Launches a coroutine in the [CoroutineScope] context parameter that collects this [Flow],
 * invoking [block] for each emission. Returns immediately; collection runs until the context
 * scope is cancelled.
 * @see [Flow.collect]
 */
context(scope: CoroutineScope)
inline fun <T> Flow<T>.launchedCollect(
    crossinline block: suspend (T) -> Unit,
) {
    scope.launch {
        collect {
            block(it)
        }
    }
}

/**
 * Launches a coroutine in the [CoroutineScope] context parameter that collects this [Flow] with
 * [collectLatest], invoking [block] for each emission and cancelling the in-progress [block] when a
 * newer emission arrives. Returns immediately; collection runs until the context scope is cancelled.
 * @see [Flow.collectLatest]
 */
context(scope: CoroutineScope)
inline fun <T> Flow<T>.launchedCollectLatest(
    crossinline block: suspend (T) -> Unit,
) {
    scope.launch {
        collectLatest {
            block(it)
        }
    }
}
