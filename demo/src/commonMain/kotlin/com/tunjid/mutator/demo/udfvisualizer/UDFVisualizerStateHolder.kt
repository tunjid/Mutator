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

package com.tunjid.mutator.demo.udfvisualizer

import androidx.compose.runtime.Composable
import com.tunjid.mutator.Mutation
import com.tunjid.mutator.ActionStateProducer
import com.tunjid.mutator.coroutines.actionStateFlowProducer
import com.tunjid.mutator.demo.Color
import com.tunjid.mutator.demo.intervalFlow
import com.tunjid.mutator.mutation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

val eventColor = Color(0xEF685C)
val stateColor = Color(0x3080E9)

typealias UDFVisualizerStateHolder = ActionStateProducer<Event, StateFlow<State>>

sealed class Marble {

    sealed class Metadata {
        object Nothing : Metadata()
        data class Text(val text: String) : Metadata()
        data class Tint(val color: Color) : Metadata()
    }

    abstract val percentage: Int
    abstract val metadata: Metadata

    data class Event(
        override val percentage: Int = 0,
        override val metadata: Metadata = Metadata.Nothing,
    ) : Marble()

    data class State(
        val index: Int = -1,
        val color: Color = stateColor,
        override val percentage: Int = 0,
        override val metadata: Metadata = Metadata.Nothing,
    ) : Marble()
}

sealed class Event {
    data class StateChange(
        val color: Color = stateColor,
        val metadata: Marble.Metadata = Marble.Metadata.Nothing
    ) : Event()

    data class UserTriggered(
        val metadata: Marble.Metadata = Marble.Metadata.Nothing
    ) : Event()
}

data class State(
    val index: Int = -1,
    val marbles: List<Marble> = listOf()
)

fun udfVisualizerStateHolder(
    scope: CoroutineScope
): ActionStateProducer<Event, StateFlow<State>> = scope.actionStateFlowProducer(
    initialState = State(),
    mutationFlows = listOf(frames()),
    actionTransform = { it.stateMutations() },
)

private fun frames(): Flow<Mutation<State>> = intervalFlow(10)
    .map { mutation { advanceFrame() } }

private fun Flow<Event>.stateMutations(): Flow<Mutation<State>> =
    map { mutation { this + it } }

/**
 * Reduces the [action] into the [State]
 */
private operator fun State.plus(action: Event) = copy(
    index = index + 1,
    marbles = marbles + when (action) {
        is Event.UserTriggered -> Marble.Event(metadata = action.metadata)
        is Event.StateChange -> Marble.State(
            index = index + 1,
            metadata = action.metadata,
            color = action.color
        )
    }
)

/**
 * Advances the frame for the UDF visualizer
 */
private fun State.advanceFrame() = copy(marbles = marbles.mapNotNull {
    if (it.percentage > 100) null
    else when (it) {
        is Marble.Event -> it.copy(percentage = it.percentage + 1)
        is Marble.State -> it.copy(percentage = it.percentage + 1)
    }
})

@Composable
expect fun UDFVisualizer(stateHolder: UDFVisualizerStateHolder)