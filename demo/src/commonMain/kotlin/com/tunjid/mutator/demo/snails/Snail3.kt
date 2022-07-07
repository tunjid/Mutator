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
import androidx.compose.ui.unit.dp
import com.tunjid.mutator.demo.MutedColors
import com.tunjid.mutator.demo.SPEED
import com.tunjid.mutator.demo.Speed
import com.tunjid.mutator.demo.intervalFlow
import com.tunjid.mutator.demo.toProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn


data class Snail3State(
    val progress: Float = 0f,
    val speed: Speed = Speed.One,
    val color: Color = Color.Blue,
    val colors: List<Color> = MutedColors.colors(false).map(::Color)
)

class Snail3StateHolder(
    scope: CoroutineScope
) {

    private val speed: Flow<Speed> = intervalFlow(2000)
        .map { Speed.values().random() }
        .shareIn(scope, SharingStarted.WhileSubscribed())

    private val progress: Flow<Float> = speed
        .flatMapLatest {
            intervalFlow(SPEED * it.multiplier)
        }
        .toProgress()

    private val color: MutableStateFlow<Color> = MutableStateFlow(Color.Cyan)


    val state: StateFlow<Snail3State> = combine(
        progress,
        speed,
        color,
        ::Snail3State
    )
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = Snail3State()
        )

    fun setSnailColor(color: Color) {
        this.color.value = color
    }
}

@Composable
fun Snail3(
modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val stateHolder = remember { Snail3StateHolder(scope) }

    val state by stateHolder.state.collectAsState()

    Surface(
        modifier = Modifier.fillMaxWidth(0.8f),
        ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            ) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text ="Snail3"
            )
            Slider(
                valueRange = 0f..100f,
                value = state.progress,
                colors = SliderDefaults.colors(
                    thumbColor = state.color,
                    activeTrackColor = state.color
                ),
                onValueChange = {}
            )

            Row (
                horizontalArrangement = Arrangement.Center
            ){
                state.colors.forEach { color ->
                    Button(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = color),
                        onClick = {
                            stateHolder.setSnailColor(color)
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
        }
    }
}