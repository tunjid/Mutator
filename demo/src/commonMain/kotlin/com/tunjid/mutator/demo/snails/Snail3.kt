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
import com.tunjid.mutator.demo.Speed
import com.tunjid.mutator.demo.editor.Paragraph
import com.tunjid.mutator.demo.editor.VerticalLayout
import com.tunjid.mutator.demo.speedFlow
import com.tunjid.mutator.demo.text
import com.tunjid.mutator.demo.toInterval
import com.tunjid.mutator.demo.toProgress
import com.tunjid.mutator.demo.udfvisualizer.Event
import com.tunjid.mutator.demo.udfvisualizer.Marble
import com.tunjid.mutator.demo.udfvisualizer.UDFVisualizer
import com.tunjid.mutator.demo.udfvisualizer.udfVisualizerStateHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class Snail3State(
    val progress: Float = 0f,
    val speed: Speed = Speed.One,
)

class Snail3StateHolder(
    scope: CoroutineScope,
) {
    private val speed: Flow<Speed> = scope.speedFlow()

    private val progress: Flow<Float> = speed
        .toInterval()
        .toProgress()

    val state: StateFlow<Snail3State> = combine(
        progress,
        speed,
        ::Snail3State,
    )
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = Snail3State(),
        )
}

@Composable
fun Snail3() {
    val scope = rememberCoroutineScope()
    val stateHolder = remember { Snail3StateHolder(scope) }
    val udfStateHolder = remember { udfVisualizerStateHolder(scope) }
    val state by stateHolder.state.collectAsState()

    LaunchedEffect(state) {
        udfStateHolder.accept(
            Event.StateChange(
                metadata = Marble.Metadata.Text(state.progress.toString()),
            ),
        )
    }

    Illustration {
        SnailCard {
            VerticalLayout {
                Paragraph(
                    text = "Snail3",
                )
                Snail(
                    progress = state.progress,
                )
                Paragraph(
                    text = "Progress: ${state.progress}; Speed: ${state.speed.text}",
                )
            }
        }
        UDFVisualizer(udfStateHolder)
    }
}
