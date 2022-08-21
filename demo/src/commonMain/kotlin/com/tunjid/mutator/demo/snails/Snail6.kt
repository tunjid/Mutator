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
import com.tunjid.mutator.demo.Color
import com.tunjid.mutator.demo.MutedColors
import com.tunjid.mutator.demo.Speed
import com.tunjid.mutator.demo.editor.Paragraph
import com.tunjid.mutator.demo.editor.VerticalLayout
import com.tunjid.mutator.demo.speedFlow
import com.tunjid.mutator.demo.text
import com.tunjid.mutator.demo.toInterval
import com.tunjid.mutator.demo.udfvisualizer.Marble
import com.tunjid.mutator.demo.udfvisualizer.Event
import com.tunjid.mutator.demo.udfvisualizer.UDFVisualizer
import com.tunjid.mutator.demo.udfvisualizer.udfVisualizerStateHolder
import com.tunjid.mutator.mutation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class Snail6State(
    val progress: Float = 0f,
    val speed: Speed = Speed.One,
    val color: Color = MutedColors.colors(false).first(),
    val colors: List<Color> = MutedColors.colors(false)
)

class Snail6StateHolder(
    private val scope: CoroutineScope
) {

    private val speed: Flow<Speed> = scope.speedFlow()

    private val speedChanges: Flow<Mutation<Snail6State>> = speed
        .map { mutation { copy(speed = it) } }

    private val progressChanges: Flow<Mutation<Snail6State>> = speed
        .toInterval()
        .map { mutation { copy(progress = (progress + 1) % 100) } }

    private val changeEvents = MutableSharedFlow<Mutation<Snail6State>>()

    val state: StateFlow<Snail6State> = merge(
        progressChanges,
        speedChanges,
        changeEvents,
    )
        .scan(Snail6State()) { state, mutation -> mutation(state) }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = Snail6State()
        )

    fun setSnailColor(index: Int) {
        scope.launch {
            changeEvents.emit { copy(color = colors[index]) }
        }
    }

    fun setProgress(progress: Float) {
        scope.launch {
            changeEvents.emit { copy(progress = progress) }
        }
    }
}

@Composable
fun Snail6() {
    val scope = rememberCoroutineScope()
    val stateHolder = remember { Snail6StateHolder(scope) }
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
                    text = "Snail6"
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
                    text = "Progress: ${state.progress}; Speed: ${state.speed.text}"
                )
            }
        }
        UDFVisualizer(udfStateHolder)
    }
}