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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface StateHolder<Action : Any, State : Any> {
    val state: StateFlow<State>
    val accept: (Action) -> Unit
}

fun <Action : Any, State : Any> scopedStateHolder(
    scope: CoroutineScope,
    initialState: State,
    started: SharingStarted = SharingStarted.WhileSubscribed(DefaultStopTimeoutMillis),
    transform: (Flow<Action>) -> Flow<Mutation<State>>
): StateHolder<Action, State> = object : StateHolder<Action, State> {
    val actions = MutableSharedFlow<Action>()

    override val state: StateFlow<State> =
        transform(actions)
            .reduceInto(initialState)
            .stateIn(
                scope = scope,
                started = started,
                initialValue = initialState
            )

    override val accept: (Action) -> Unit = { action ->
        scope.launch {
            // Suspend till downstream is connected
            actions.subscriptionCount.first { it > 0 }
            actions.emit(action)
        }
    }
}

private const val DefaultStopTimeoutMillis = 5000L