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
import com.tunjid.mutator.demo.editor.Paragraph
import com.tunjid.mutator.demo.editor.VerticalLayout
import com.tunjid.mutator.demo.intervalFlow
import com.tunjid.mutator.demo.toProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class Snail1StateHolder(
    scope: CoroutineScope
) {
    val progress: StateFlow<Float> = intervalFlow(500)
        .toProgress()
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = 0f
        )
}

@Composable
fun Snail1() {
    val scope = rememberCoroutineScope()
    val stateHolder = remember { Snail1StateHolder(scope) }
    val state by stateHolder.progress.collectAsState()

    SnailCard {
        VerticalLayout {
            Paragraph(
                text = "Snail1"
            )
            Snail(
                progress = state,
            )
            Paragraph(
                text = "Progress: $state"
            )
        }
    }
}
