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

package com.tunjid.mutator.demo.snails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.tunjid.mutator.Mutation
import com.tunjid.mutator.coroutines.stateFlowMutator
import com.tunjid.mutator.demo.Color
import com.tunjid.mutator.demo.MutedColors
import com.tunjid.mutator.demo.Speed
import com.tunjid.mutator.demo.editor.VerticalLayout
import com.tunjid.mutator.demo.interpolateColors
import com.tunjid.mutator.demo.speedFlow
import com.tunjid.mutator.demo.text
import com.tunjid.mutator.demo.toInterval
import com.tunjid.mutator.demo.udfvisualizer.Event
import com.tunjid.mutator.demo.udfvisualizer.Marble
import com.tunjid.mutator.demo.udfvisualizer.UDFVisualizer
import com.tunjid.mutator.demo.udfvisualizer.udfVisualizerStateHolder
import com.tunjid.mutator.mutationOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

data class Snail9State(
    val progress: Float = 0f,
    val speed: Speed = Speed.One,
    val isDark: Boolean = false,
    val colorIndex: Int = 0,
    val isInterpolating: Boolean = false,
    val colorInterpolationProgress: Float = 0F,
    val colors: List<Color> = MutedColors.colors(false),
)

val Snail9State.color get() = colors[colorIndex]

val Snail9State.cardColor: Color get() = colors.last()

val Snail9State.textColor: Color get() = if (cardColor.isBright()) Color.Black else Color.LightGray

class Snail9StateHolder(
    scope: CoroutineScope,
) {

    private val speed: Flow<Speed> = scope.speedFlow()

    private val speedChanges: Flow<Mutation<Snail9State>> = speed
        .map { mutationOf { copy(speed = it) } }

    private val progressChanges: Flow<Mutation<Snail9State>> = speed
        .toInterval()
        .map { mutationOf { copy(progress = (progress + 1) % 100) } }

    private val stateMutator = scope.stateFlowMutator(
        initialState = Snail9State(),
        started = SharingStarted.WhileSubscribed(),
        inputs = listOf(
            speedChanges,
            progressChanges,
        ),
    )

    val state: StateFlow<Snail9State> = stateMutator.state

    fun setSnailColor(index: Int) = stateMutator.launch {
        emit { copy(colorIndex = index) }
    }

    fun setProgress(progress: Float) = stateMutator.launch {
        emit { copy(progress = progress) }
    }

    fun setMode(isDark: Boolean) = stateMutator.launch {
        if (state.value.isInterpolating) return@launch
        emit { copy(isDark = isDark, isInterpolating = true) }
        interpolateColors(
            startColors = state.value.colors.map(Color::argb).toIntArray(),
            endColors = MutedColors.colors(isDark).map(Color::argb).toIntArray(),
        ).collect { (progress, colors) ->
            emit {
                copy(
                    colorInterpolationProgress = progress,
                    colors = colors,
                )
            }
        }
        emit { copy(isInterpolating = false) }
    }
}

@Composable
fun Snail9() {
    val scope = rememberCoroutineScope()
    val stateHolder = remember { Snail9StateHolder(scope) }
    val udfStateHolder = remember { udfVisualizerStateHolder(scope) }
    val state by stateHolder.state.collectAsState()

    LaunchedEffect(state) {
        udfStateHolder.accept(
            Event.StateChange(
                color = state.color,
                metadata = Marble.Metadata.Text(state.progress.toString()),
            ),
        )
    }

    Illustration {
        SnailCard(state.cardColor) {
            VerticalLayout {
                SnailText(
                    color = state.textColor,
                    text = "Snail9",
                )
                Snail(
                    progress = state.progress,
                    color = state.color,
                    onValueChange = {
                        stateHolder.setProgress(it)
                        udfStateHolder.accept(Event.UserTriggered(metadata = Marble.Metadata.Text(it.toString())))
                    },
                )
                ColorSwatch(
                    colors = state.colors,
                    onColorClicked = {
                        stateHolder.setSnailColor(it)
                        udfStateHolder.accept(Event.UserTriggered(metadata = Marble.Metadata.Tint(state.colors[it])))
                    },
                )
                SnailText(
                    color = state.textColor,
                    text = "Progress: ${state.progress}; Speed: ${state.speed.text}",
                )
                ToggleButton(
                    progress = state.colorInterpolationProgress,
                    onClicked = {
                        stateHolder.setMode(!state.isDark)
                        udfStateHolder.accept(
                            Event.UserTriggered(
                                Marble.Metadata.Text(
                                    if (state.isDark) "☀️"
                                    else "\uD83C\uDF18",
                                ),
                            ),
                        )
                    },
                )
            }
        }
        UDFVisualizer(udfStateHolder)
    }
}
