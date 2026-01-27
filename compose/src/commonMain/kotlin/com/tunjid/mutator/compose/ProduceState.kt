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

package com.tunjid.mutator.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.tunjid.mutator.StateMutator
import com.tunjid.mutator.coroutines.SuspendingStateMutator
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Produces state by collecting the [StateFlow] in the [StateMutator].
 */
@Composable
fun <T : Any> StateMutator<StateFlow<T>>.produceState(): T =
    state.collectAsState().value

/**
 * Produces state by collecting the [StateFlow] in the [StateMutator].
 *
 * @param lifecycle The [Lifecycle] to observe.
 * @param minActiveState The minimum active state of the lifecycle required to collect the flow.
 * @param context The [CoroutineContext] to use for collection.
 */
@Composable
fun <T : Any> StateMutator<StateFlow<T>>.produceStateWithLifecycle(
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    context: CoroutineContext = EmptyCoroutineContext,
): T =
    state.collectAsStateWithLifecycle(
        lifecycle = lifecycle,
        minActiveState = minActiveState,
        context = context,
    ).value

/**
 * Produces state by launching a coroutine to collect the mutator's state and keeps it active
 * as long as the composable is in the composition.
 */
@Composable
fun <T : Any> SuspendingStateMutator<T>.produceState(): T {
    val scope = rememberCoroutineScope()

    DisposableEffect(this, scope) {
        val job = scope.launch { collect() }
        onDispose(job::cancel)
    }

    return state
}

/**
 * Produces state by launching a coroutine aware of the [lifecycle] to collect the mutator's
 * state and keeps it active as long as the composable is in the composition.
 *
 * The [lifecycle] will need to be at least in [minActiveState] for state production to occur.
 *
 * @param lifecycle The [Lifecycle] to observe.
 * @param minActiveState The minimum active state of the lifecycle required to collect the mutator.
 * @param context The [CoroutineContext] to use for collection.
 */
@Composable
fun <T : Any> SuspendingStateMutator<T>.produceStateWithLifecycle(
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    context: CoroutineContext = EmptyCoroutineContext,
): T {
    val scope = rememberCoroutineScope()

    DisposableEffect(this, scope, lifecycle, minActiveState, context) {
        val job = scope.launch {
            lifecycle.repeatOnLifecycle(minActiveState) {
                if (context == EmptyCoroutineContext) collect()
                else withContext(context) { collect() }
            }
        }
        onDispose(job::cancel)
    }

    return state
}
