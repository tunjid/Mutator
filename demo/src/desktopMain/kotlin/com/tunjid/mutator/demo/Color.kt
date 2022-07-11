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

package com.tunjid.mutator.demo

import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color as ComposeColor

actual class Color actual constructor(color: Long) {
    internal val composeColor = ComposeColor(color)

    actual val argb: Int = composeColor.toArgb()
    actual val r: Int = composeColor.red.toRgbInt()
    actual val g: Int = composeColor.green.toRgbInt()
    actual val b: Int = composeColor.blue.toRgbInt()

    actual fun isBright(): Boolean = composeColor.luminance() >= 0.5

    private fun Float.toRgbInt() = (this * 255).toInt()

    actual companion object {
        actual val Black: Color = Color(ComposeColor.Black.toArgb().toLong())
        actual val LightGray: Color = Color(ComposeColor.LightGray.toArgb().toLong())
    }
}

val Color.toComposeColor get() = composeColor