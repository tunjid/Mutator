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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tunjid.mutator.demo.Speed

@Composable
actual fun SnailCard(
    color: Color,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(0.8f), content = { content() },
    )
}

@Composable
actual fun SnailText(
    color: Color,
    text: String
) {
    Text(
        modifier = Modifier.padding(vertical = 8.dp),
        text = text,
        color = color,
        style = MaterialTheme.typography.body1.copy(
            MaterialTheme.colors.onSurface
        )
    )
}

@Composable
actual fun Snail(
    progress: Float,
    speed: Speed,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Slider(
        valueRange = 0f..100f,
        value = progress,
        colors = SliderDefaults.colors(
            thumbColor = color,
            activeTrackColor = color
        ),
        onValueChange = onValueChange
    )
}

@Composable
actual fun ColorSwatch(
    colors: List<Color>,
    onColorClicked: (index: Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center
    ) {
        colors.forEachIndexed { index, color ->
            Button(
                modifier = Modifier.padding(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = color),
                onClick = {
                    onColorClicked(index)
                },
                content = {
                },
            )
        }
    }
}

@Composable
actual fun ToggleButton(
    progress: Float,
    onClicked: () -> Unit
) {
    Button(
        modifier = Modifier.padding(horizontal = 8.dp),
        onClick = {
            onClicked()
        },
        content = {
            Text(text = "Toggle mode")
        },
    )
}