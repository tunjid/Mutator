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
import com.tunjid.mutator.coroutines.stateFlowProducer
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map


data class Snail10State(
    val progress: Float = 0f,
    val speed: Speed = Speed.One,
    val isDark: Boolean = false,
    val colorIndex: Int = 0,
    val colorInterpolationProgress: Float = 0F,
    val colors: List<Color> = MutedColors.colors(false)
)

val Snail10State.color get() = colors[colorIndex]

val Snail10State.cardColor: Color get() = colors.last()

val Snail10State.textColor: Color get() = if (cardColor.isBright()) Color.Black else Color.LightGray

class Snail10StateHolder(
    private val scope: CoroutineScope
) {

    private var setModeJob: Job? = null

    private val speed: Flow<Speed> = scope.speedFlow()

    private val speedChanges: Flow<Mutation<Snail10State>> = speed
        .map { mutationOf { copy(speed = it) } }

    private val progressChanges: Flow<Mutation<Snail10State>> = speed
        .toInterval()
        .map { mutationOf { copy(progress = (progress + 1) % 100) } }

    private val stateProducer = scope.stateFlowProducer(
        initialState = Snail10State(),
        started = SharingStarted.WhileSubscribed(),
        mutationFlows = listOf(
            speedChanges,
            progressChanges,
        )
    )
    val state: StateFlow<Snail10State> = stateProducer.state

    fun setSnailColor(index: Int) = stateProducer.launch {
        mutate { copy(colorIndex = index) }
    }

    fun setProgress(progress: Float) = stateProducer.launch {
        mutate { copy(progress = progress) }
    }

    fun setMode(isDark: Boolean) = stateProducer.launch {
        setModeJob?.cancel()
        setModeJob = currentCoroutineContext()[Job]
        mutate { copy(isDark = isDark) }
        interpolateColors(
            startColors = state.value.colors.map(Color::argb).toIntArray(),
            endColors = MutedColors.colors(isDark).map(Color::argb).toIntArray()
        ).collect { (progress, colors) ->
            mutate {
                copy(
                    colorInterpolationProgress = progress,
                    colors = colors
                )
            }
        }
    }
}


@Composable
fun Snail10() {
    val scope = rememberCoroutineScope()
    val stateHolder = remember { Snail10StateHolder(scope) }
    val udfStateHolder = remember { udfVisualizerStateHolder(scope) }
    val state by stateHolder.state.collectAsState()

    LaunchedEffect(state) {
        udfStateHolder.accept(
            Event.StateChange(
                color = state.color,
                metadata = Marble.Metadata.Text(state.progress.toString())
            )
        )
    }

    Illustration {
        SnailCard(state.cardColor) {
            VerticalLayout {
                SnailText(
                    color = state.textColor,
                    text = "Snail10"
                )
                Snail(
                    progress = state.progress,
                    color = state.color,
                    onValueChange = {
                        stateHolder.setProgress(it)
                        udfStateHolder.accept(Event.UserTriggered(metadata = Marble.Metadata.Text(it.toString())))
                    }
                )
                ColorSwatch(
                    colors = state.colors,
                    onColorClicked = {
                        stateHolder.setSnailColor(it)
                        udfStateHolder.accept(Event.UserTriggered(metadata = Marble.Metadata.Tint(state.colors[it])))
                    }
                )
                SnailText(
                    color = state.textColor,
                    text = "Progress: ${state.progress}; Speed: ${state.speed.text}"
                )
                ToggleButton(
                    progress = state.colorInterpolationProgress,
                    onClicked = {
                        stateHolder.setMode(!state.isDark)
                        udfStateHolder.accept(
                            Event.UserTriggered(
                                Marble.Metadata.Text(
                                    if (state.isDark) "☀️"
                                    else "\uD83C\uDF18"
                                )
                            )
                        )
                    }
                )
            }
        }
        UDFVisualizer(udfStateHolder)
    }
}