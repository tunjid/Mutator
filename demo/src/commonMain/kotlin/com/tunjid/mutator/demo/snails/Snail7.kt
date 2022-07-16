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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.tunjid.mutator.Mutation
import com.tunjid.mutator.mutation
import com.tunjid.mutator.coroutines.mutateStateWith
import com.tunjid.mutator.demo.Color
import com.tunjid.mutator.demo.MutedColors
import com.tunjid.mutator.demo.Speed
import com.tunjid.mutator.demo.editor.VerticalLayout
import com.tunjid.mutator.demo.interpolateColors
import com.tunjid.mutator.demo.speedFlow
import com.tunjid.mutator.demo.toInterval
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


data class Snail7State(
    val progress: Float = 0f,
    val speed: Speed = Speed.One,
    val isDark: Boolean = false,
    val colorIndex: Int = 0,
    val colorInterpolationProgress: Float = 0F,
    val colors: List<Color> = MutedColors.colors(false)
)

val Snail7State.color get() = colors[colorIndex]

val Snail7State.cardColor: Color get() = colors.last()

val Snail7State.textColor: Color get() = if (cardColor.isBright()) Color.Black else Color.LightGray

class Snail7StateHolder(
    private val scope: CoroutineScope
) {

    private val speed: Flow<Speed> = scope.speedFlow()

    private val speedChanges: Flow<Mutation<Snail7State>> = speed
        .map { mutation { copy(speed = it) } }

    private val progressChanges: Flow<Mutation<Snail7State>> = speed
        .toInterval()
        .map { mutation { copy(progress = (progress + 1) % 100) } }

    private val userChanges = MutableSharedFlow<Mutation<Snail7State>>()

    val state: StateFlow<Snail7State> = scope.mutateStateWith(
        initialState = Snail7State(),
        started = SharingStarted.WhileSubscribed(),
        mutationFlows = listOf(
            speedChanges,
            progressChanges,
            userChanges,
        )
    )

    fun setSnailColor(index: Int) {
        scope.launch {
            userChanges.emit { copy(colorIndex = index) }
        }
    }

    fun setProgress(progress: Float) {
        scope.launch {
            userChanges.emit { copy(progress = progress) }
        }
    }

    fun setMode(isDark: Boolean) {
        scope.launch {
            userChanges.emit { copy(isDark = isDark) }
            // Collect from a flow that animates color changes
            interpolateColors(
                startColors = state.value.colors.map(Color::argb).toIntArray(),
                endColors = MutedColors.colors(isDark).map(Color::argb).toIntArray()
            ).collect { (progress, colors) ->
                userChanges.emit {
                    copy(
                        colorInterpolationProgress = progress,
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
fun Snail7() {
    val scope = rememberCoroutineScope()
    val stateHolder = remember { Snail7StateHolder(scope) }
    val state by stateHolder.state.collectAsState()

    SnailCard(state.cardColor) {
        VerticalLayout {
            SnailText(
                color = state.textColor,
                text = "Snail7"
            )
            Snail(
                progress = state.progress,
                color = state.color,
                onValueChange = { stateHolder.setProgress(it) }
            )
            ColorSwatch(
                colors = state.colors,
                onColorClicked = {
                    stateHolder.setSnailColor(it)
                }
            )
            SnailText(
                color = state.textColor,
                text = "Progress: ${state.progress}; Speed: ${state.speed}"
            )
            ToggleButton(
                progress = state.colorInterpolationProgress,
                onClicked = { stateHolder.setMode(!state.isDark) }
            )
        }
    }
}