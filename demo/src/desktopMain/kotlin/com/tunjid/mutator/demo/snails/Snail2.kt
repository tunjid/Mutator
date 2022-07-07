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
@file:JvmName("Snail2Jvm")

package com.tunjid.mutator.demo.snails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope

@Composable
actual fun Snail2() {
    val scope = rememberCoroutineScope()
    val stateHolder = remember { Snail2StateHolder(scope) }
    val state by stateHolder.state.collectAsState()

    Surface(
        modifier = Modifier.fillMaxWidth(0.8f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = "Snail2"
            )
            Slider(
                valueRange = 0f..100f,
                value = state.progress,
                onValueChange = {}
            )
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = "Progress: ${state.progress}; Speed: ${state.speed}"
            )
        }
    }
}