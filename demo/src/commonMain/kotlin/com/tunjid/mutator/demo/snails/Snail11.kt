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
import com.tunjid.mutator.coroutines.toMutationStream
import com.tunjid.mutator.demo.Color
import com.tunjid.mutator.demo.MutedColors
import com.tunjid.mutator.demo.Speed
import com.tunjid.mutator.demo.editor.VerticalLayout
import com.tunjid.mutator.demo.interpolateColors
import com.tunjid.mutator.demo.speedFlow
import com.tunjid.mutator.demo.toInterval
import com.tunjid.mutator.demo.udfvisualizer.Marble
import com.tunjid.mutator.demo.udfvisualizer.Event
import com.tunjid.mutator.demo.udfvisualizer.UDFVisualizer
import com.tunjid.mutator.demo.udfvisualizer.udfVisualizerStateHolder
import com.tunjid.mutator.mutation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest


data class Snail11State(
    val progress: Float = 0f,
    val speed: Speed = Speed.One,
    val isDark: Boolean = false,
    val colorIndex: Int = 0,
    val colorInterpolationProgress: Float = 0F,
    val colors: List<Color> = MutedColors.colors(false)
)

val Snail11State.color get() = colors[colorIndex]

val Snail11State.cardColor: Color get() = colors.last()

val Snail11State.textColor: Color get() = if (cardColor.isBright()) Color.Black else Color.LightGray

sealed class Action {
    data class SetColor(
        val index: Int
    ) : Action()

    data class SetProgress(
        val progress: Float
    ) : Action()

    data class SetMode(
        val isDark: Boolean,
        val startColors: List<Color>
    ) : Action()
}

class Snail11StateHolder(
    scope: CoroutineScope
) {

    private val speed: Flow<Speed> = scope.speedFlow()

    private val speedChanges: Flow<Mutation<Snail11State>> = speed
        .map { mutation { copy(speed = it) } }

    private val progressChanges: Flow<Mutation<Snail11State>> = speed
        .toInterval()
        .map { mutation { copy(progress = (progress + 1) % 100) } }


    private val mutator = scope.stateFlowMutator<Action, Snail11State>(
        initialState = Snail11State(),
        started = SharingStarted.WhileSubscribed(),
        mutationFlows = listOf(
            speedChanges,
            progressChanges
        ),
        actionTransform = { actions ->
            actions.toMutationStream {
                when (val type = type()) {
                    is Action.SetColor -> type.flow.colorMutations()
                    is Action.SetMode -> type.flow.modeMutations()
                    is Action.SetProgress -> type.flow.progressMutations()
                }
            }
        }
    )

    val state: StateFlow<Snail11State> = mutator.state

    val actions: (Action) -> Unit = mutator.accept

    private fun Flow<Action.SetColor>.colorMutations(): Flow<Mutation<Snail11State>> =
        mapLatest {
            mutation { copy(colorIndex = it.index) }
        }

    private fun Flow<Action.SetProgress>.progressMutations(): Flow<Mutation<Snail11State>> =
        mapLatest {
            mutation { copy(progress = it.progress) }
        }

    private fun Flow<Action.SetMode>.modeMutations(): Flow<Mutation<Snail11State>> =
        flatMapLatest { (isDark, startColors) ->
            flow {
                emit(mutation { copy(isDark = isDark) })
                emitAll(
                    interpolateColors(
                        startColors = startColors.map(Color::argb).toIntArray(),
                        endColors = MutedColors.colors(isDark).map(Color::argb).toIntArray()
                    )
                        .map { (progress, colors) ->
                            mutation {
                                copy(
                                    colorInterpolationProgress = progress,
                                    colors = colors
                                )
                            }
                        }
                )
            }
        }
}

@Composable
fun Snail11() {
    val scope = rememberCoroutineScope()
    val stateHolder = remember { Snail11StateHolder(scope) }
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
                    text = "Snail11"
                )
                Snail(
                    progress = state.progress,
                    color = state.color,
                    onValueChange = {
                        stateHolder.actions(Action.SetProgress(it))
                        udfStateHolder.accept(Event.UserTriggered(metadata = Marble.Metadata.Text(it.toString())))
                    }
                )
                ColorSwatch(
                    colors = state.colors,
                    onColorClicked = {
                        stateHolder.actions(Action.SetColor(it))
                        udfStateHolder.accept(Event.UserTriggered(metadata = Marble.Metadata.Tint(state.colors[it])))
                    }
                )
                SnailText(
                    color = state.textColor,
                    text = "Progress: ${state.progress}; Speed: ${state.speed}"
                )
                ToggleButton(
                    progress = state.colorInterpolationProgress,
                    onClicked = {
                        stateHolder.actions(
                            Action.SetMode(
                                isDark = !state.isDark,
                                startColors = state.colors
                            )
                        )
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