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
import com.tunjid.mutator.demo.Color
import com.tunjid.mutator.demo.MutedColors
import com.tunjid.mutator.demo.editor.Paragraph
import com.tunjid.mutator.demo.editor.VerticalLayout
import com.tunjid.mutator.demo.udfvisualizer.Event
import com.tunjid.mutator.demo.udfvisualizer.Marble
import com.tunjid.mutator.demo.udfvisualizer.UDFVisualizer
import com.tunjid.mutator.demo.udfvisualizer.udfVisualizerStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class Snail1State(
    val progress: Float = 0f,
    val color: Color = MutedColors.colors(false).first(),
    val colors: List<Color> = MutedColors.colors(false)
)

class Snail1StateHolder {

    private val _state = MutableStateFlow(Snail1State())
    val state: StateFlow<Snail1State> = _state.asStateFlow()

    fun setSnailColor(index: Int) {
        _state.update {
            it.copy(color = it.colors[index])
        }
    }

    fun setProgress(progress: Float) {
        _state.update {
            it.copy(progress = progress)
        }
    }
}

@Composable
fun Snail1() {
    val scope = rememberCoroutineScope()
    val stateHolder = remember { Snail1StateHolder() }
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
        SnailCard {
            VerticalLayout {
                Paragraph(
                    text = "Snail1"
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
                Paragraph(
                    text = "Progress: ${state.progress}"
                )
            }
        }
        UDFVisualizer(udfStateHolder)
    }
}