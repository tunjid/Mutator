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
import com.tunjid.mutator.demo.Color
import com.tunjid.mutator.demo.MutedColors
import com.tunjid.mutator.demo.Speed


@Composable
expect fun Illustration(
    content: @Composable () -> Unit
)

@Composable
expect fun SnailCard(
    color: Color = Color(0xFFFFFF),
    content: @Composable () -> Unit
)

@Composable
expect fun SnailText(
    color: Color,
    text: String
)

@Composable
expect fun Snail(
    progress: Float,
    speed: Speed = Speed.One,
    color: Color = MutedColors.colors(isDark = false).first(),
    onValueChange: (Float) -> Unit = {}
)

@Composable
expect fun ColorSwatch(
    colors: List<Color> = listOf(),
    onColorClicked: (Int) -> Unit = {}
)

@Composable
expect fun ToggleButton(
    progress: Float,
    onClicked: () -> Unit = {}
)