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
import com.tunjid.mutator.coroutines.emit
import com.tunjid.mutator.coroutines.produceState
import com.tunjid.mutator.demo.MutedColors
import com.tunjid.mutator.demo.SPEED
import com.tunjid.mutator.demo.Speed
import com.tunjid.mutator.demo.interpolateColors
import com.tunjid.mutator.demo.intervalFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch


data class Snail8State(
    val progress: Float = 0f,
    val speed: Speed = Speed.One,
    val isDark: Boolean = false,
    val colorIndex: Int = 0,
    val isInterpolating: Boolean = false,
    val colors: List<Color> = MutedColors.colors(false).map(::Color)
)

val Snail8State.color get() = colors[colorIndex]

class Snail8StateHolder(
    private val scope: CoroutineScope
) {

    private val speed: Flow<Speed> = intervalFlow(2000)
        .map { Speed.values().random() }
        .shareIn(scope, SharingStarted.WhileSubscribed())

    private val speedChanges: Flow<Mutation<Snail8State>> = speed
        .map { Mutation { copy(speed = it) } }

    private val progressChanges: Flow<Mutation<Snail8State>> = speed
        .flatMapLatest {
            intervalFlow(SPEED * it.multiplier)
        }
        .map { Mutation { copy(progress = (progress + 1) % 100) } }

    private val userChanges = MutableSharedFlow<Mutation<Snail8State>>()

    val state: StateFlow<Snail8State> = scope.produceState(
        initial = Snail8State(),
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
        if (state.value.isInterpolating) return
        scope.launch {
            userChanges.emit { copy(isDark = isDark, isInterpolating = true) }
            interpolateColors(
                startColors = state.value.colors.map(Color::toArgb).toIntArray(),
                endColors = MutedColors.colors(isDark)
            ).collect {
                userChanges.emit { copy(colors = it) }
            }
            userChanges.emit { copy(isInterpolating = false) }
        }
    }
}

@Composable
fun Snail8(
modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val stateHolder = remember { Snail8StateHolder(scope) }

    val state by stateHolder.state.collectAsState()

    Surface(
        modifier = modifier.fillMaxWidth(0.8f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = "Snail8"
            )
            Slider(
                valueRange = 0f..100f,
                value = state.progress,
                colors = SliderDefaults.colors(
                    thumbColor = state.color,
                    activeTrackColor = state.color
                ),
                onValueChange = { stateHolder.setProgress(it) }
            )

            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                state.colors.forEachIndexed { index, color ->
                    Button(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = color),
                        onClick = {
                            stateHolder.setSnailColor(index)
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
                    stateHolder.setMode(!state.isDark)
                },
                content = {
                    Text(text = "Toggle mode")
                },
            )
        }
    }
}