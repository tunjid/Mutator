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
@file:JvmName("Snail9Jvm")

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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope

@Composable
actual fun Snail9() {
    val scope = rememberCoroutineScope()
    val stateHolder = remember { Snail9StateHolder(scope) }
    val state by stateHolder.state.collectAsState()

    Surface(
        modifier = Modifier.fillMaxWidth(0.8f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = "Snail9"
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