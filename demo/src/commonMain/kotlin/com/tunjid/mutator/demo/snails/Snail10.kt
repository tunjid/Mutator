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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.tunjid.mutator.Mutation
import com.tunjid.mutator.coroutines.stateFlowMutator
import com.tunjid.mutator.coroutines.toMutationStream
import com.tunjid.mutator.demo.MutedColors
import com.tunjid.mutator.demo.SPEED
import com.tunjid.mutator.demo.Speed
import com.tunjid.mutator.demo.interpolateColors
import com.tunjid.mutator.demo.intervalFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn


data class Snail10State(
    val progress: Float = 0f,
    val speed: Speed = Speed.One,
    val isDark: Boolean = false,
    val colorIndex: Int = 0,
    val colors: List<Color> = MutedColors.colors(false).map(::Color)
)

val Snail10State.color get() = colors[colorIndex]

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

class Snail10StateHolder(
    scope: CoroutineScope
) {

    private val speed: Flow<Speed> = intervalFlow(2000)
        .map { Speed.values().random() }
        .shareIn(scope, SharingStarted.WhileSubscribed())

    private val speedChanges: Flow<Mutation<Snail10State>> = speed
        .map { Mutation { copy(speed = it) } }

    private val progressChanges: Flow<Mutation<Snail10State>> = speed
        .flatMapLatest {
            intervalFlow(SPEED * it.multiplier)
        }
        .map { Mutation { copy(progress = (progress + 1) % 100) } }


    private val mutator = stateFlowMutator<Action, Snail10State>(
        scope = scope,
        initialState = Snail10State(),
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

    val state: StateFlow<Snail10State> = mutator.state

    val actions: (Action) -> Unit = mutator.accept

    private fun Flow<Action.SetColor>.colorMutations() = mapLatest {
        Mutation<Snail10State> { copy(colorIndex = it.index) }
    }

    private fun Flow<Action.SetProgress>.progressMutations() = mapLatest {
        Mutation<Snail10State> { copy(progress = it.progress) }
    }

    private fun Flow<Action.SetMode>.modeMutations(): Flow<Mutation<Snail10State>> =
        flatMapLatest { (isDark, startColors) ->
            flow {
                emit(Mutation { copy(isDark = isDark) })
                emitAll(
                    interpolateColors(
                        startColors = startColors.map(Color::toArgb).toIntArray(),
                        endColors = MutedColors.colors(isDark)
                    )
                        .map { Mutation { copy(colors = it) } }
                )
            }
        }
}

@Composable
fun Snail10() {
    val scope = rememberCoroutineScope()
    val stateHolder = remember { Snail10StateHolder(scope) }

    val state by stateHolder.state.collectAsState()

    Surface(
        modifier = Modifier.fillMaxWidth(0.8f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = "Snail10"
            )
            Slider(
                valueRange = 0f..100f,
                value = state.progress,
                colors = SliderDefaults.colors(
                    thumbColor = state.color,
                    activeTrackColor = state.color
                ),
                onValueChange = { stateHolder.actions(Action.SetProgress(it)) }
            )

            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                state.colors.forEachIndexed { index, color ->
                    Button(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = color),
                        onClick = {
                            stateHolder.actions(Action.SetColor(index))
                        },
                        content = {
                        },
                    )
                }
            }

            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = "Progress: ${state.progress}; Speed: ${state.speed}"
            )

            Button(
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = {
                    stateHolder.actions(
                        Action.SetMode(
                            isDark = !state.isDark,
                            startColors = state.colors
                        )
                    )
                },
                content = {
                    Text(text = "Toggle mode")
                },
            )
        }
    }
}