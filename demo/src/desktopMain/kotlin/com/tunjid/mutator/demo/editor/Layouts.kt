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

package com.tunjid.mutator.demo.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tunjid.mutator.demo.Speed
import com.tunjid.mutator.demo.common.AppTheme

@Composable
actual fun ContainerLayout(content: @Composable () -> Unit) {
    val scrollState = rememberScrollState()
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
//                    .verticalScroll(scrollState)
                    .widthIn(0.dp, 600.dp)
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
        }
    }
}
@Composable
actual fun SectionLayout(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = { content() }
    )
}

@Composable
actual fun VerticalLayout(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        content = { content() }
    )
}

@Composable
actual fun HorizontalLayout(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.padding(horizontal = 8.dp),
        content = { content() }
    )
}

@Composable
actual fun SnailCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(0.8f), content = { content() },
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