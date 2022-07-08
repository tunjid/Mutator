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
import androidx.compose.ui.graphics.Color
import com.tunjid.mutator.demo.MutedColors
import com.tunjid.mutator.demo.Speed
import com.tunjid.mutator.demo.editor.Paragraph
import com.tunjid.mutator.demo.editor.VerticalLayout
import com.tunjid.mutator.demo.speedFlow
import com.tunjid.mutator.demo.toInterval
import com.tunjid.mutator.demo.toProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

    private val speed: Flow<Speed> = scope.speedFlow()

    private val progress: Flow<Float> = speed
        .toInterval()
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

    fun setSnailColor(index: Int) {
        this.color.value = state.value.colors[index]
    }
}

@Composable
fun Snail3() {
    val scope = rememberCoroutineScope()
    val stateHolder = remember { Snail3StateHolder(scope) }
    val state by stateHolder.state.collectAsState()

    SnailCard {
        VerticalLayout {
            Paragraph(
                text = "Snail3"
            )
            Snail(
                progress = state.progress,
                color = state.color,
            )
            ColorSwatch(
                colors = state.colors,
                onColorClicked = {
                    stateHolder.setSnailColor(it)
                }
            )
            Paragraph(
                text = "Progress: ${state.progress}; Speed: ${state.speed}"
            )
        }
    }
}
