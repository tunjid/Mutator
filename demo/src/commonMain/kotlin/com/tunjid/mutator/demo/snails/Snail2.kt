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
import com.tunjid.mutator.demo.SPEED
import com.tunjid.mutator.demo.Speed
import com.tunjid.mutator.demo.editor.Paragraph
import com.tunjid.mutator.demo.editor.Snail
import com.tunjid.mutator.demo.editor.SnailCard
import com.tunjid.mutator.demo.editor.VerticalLayout
import com.tunjid.mutator.demo.intervalFlow
import com.tunjid.mutator.demo.toProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn


data class Snail2State(
    val progress: Float = 0f,
    val speed: Speed = Speed.One,
)

class Snail2StateHolder(
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


    val state: StateFlow<Snail2State> = combine(
        progress,
        speed,
        ::Snail2State
    )
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = Snail2State()
        )
}

@Composable
fun Snail2() {
    val scope = rememberCoroutineScope()
    val stateHolder = remember { Snail2StateHolder(scope) }
    val state by stateHolder.state.collectAsState()

    SnailCard {
        VerticalLayout {
            Paragraph(
                text = "Snail2"
            )
            Snail(
                progress = state.progress,
            )
            Paragraph(
                text = "Progress: ${state.progress}; Speed: ${state.speed}"
            )
        }
    }
}