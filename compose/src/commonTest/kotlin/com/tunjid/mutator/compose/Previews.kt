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

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tunjid.mutator.coroutines.mapToMutation
import com.tunjid.mutator.coroutines.stateFlowMutator
import com.tunjid.mutator.coroutines.suspendingStateMutator
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow

@Preview
@Composable
private fun StateMutatorProduceStateDemo() {
    val scope = rememberCoroutineScope()
    val mutator = remember(scope) {
        scope.stateFlowMutator(
            initialState = State.Immutable(),
            started = SharingStarted.WhileSubscribed(),
            inputs = listOf(
                IndexFlow.mapToMutation {
                    copy(index = it)
                },
            ),
        )
    }
    val state = mutator.produceState()
    ColoredBox(
        colorIndex = state.index,
    )
}

@Preview
@Composable
private fun StateMutatorProduceStateWithLifecycleDemo() {
    val scope = rememberCoroutineScope()
    val mutator = remember(scope) {
        scope.stateFlowMutator(
            initialState = State.Immutable(),
            started = SharingStarted.WhileSubscribed(),
            inputs = listOf(
                IndexFlow.mapToMutation {
                    copy(index = it)
                },
            ),
        )
    }
    val state = mutator.produceStateWithLifecycle()
    ColoredBox(
        colorIndex = state.index,
    )
}

@Preview
@Composable
private fun SuspendingStateMutatorProduceStateDemo() {
    val scope = rememberCoroutineScope()
    val mutator = remember(scope) {
        scope.suspendingStateMutator(
            initialState = State.SnapshotMutable(),
            started = SharingStarted.WhileSubscribed(),
            producer = { state ->
                IndexFlow.collect { index ->
                    state.index = index
                }
            },
        )
    }
    val state = mutator.produceState()
    ColoredBox(
        colorIndex = state.index,
    )
}

@Preview
@Composable
private fun SuspendingStateMutatorProduceStateWithLifecycleDemo() {
    val scope = rememberCoroutineScope()
    val mutator = remember(scope) {
        scope.suspendingStateMutator(
            initialState = State.SnapshotMutable(),
            started = SharingStarted.WhileSubscribed(),
            producer = { state ->
                IndexFlow.collect { index ->
                    state.index = index
                }
            },
        )
    }
    val state = mutator.produceStateWithLifecycle()
    ColoredBox(
        colorIndex = state.index,
    )
}

@Composable
private fun ColoredBox(
    colorIndex: Int,
) {
    val color by animateColorAsState(
        targetValue = PastelColors[colorIndex % PastelColors.size],
    )
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(color),
    )
}

private val IndexFlow = flow {
    var count = 0
    while (true) {
        emit(count++)
        delay(1.seconds)
    }
}

private val PastelColors = listOf(
    Color(0xFFB39DDB), // Deep Purple 200
    Color(0xFF90CAF9), // Blue 200
    Color(0xFF80CBC4), // Teal 200
    Color(0xFFA5D6A7), // Green 200
    Color(0xFFFFF59D), // Yellow 200
    Color(0xFFFFCC80), // Orange 200
    Color(0xFFEF9A9A), // Red 200
)

@Stable
sealed interface State {

    val index: Int

    data class Immutable(
        override val index: Int = 0,
    ) : State

    class SnapshotMutable(
        index: Int = 0,
    ) : State {
        override var index by mutableIntStateOf(index)
    }
}
