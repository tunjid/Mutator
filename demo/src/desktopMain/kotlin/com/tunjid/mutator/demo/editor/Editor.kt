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

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

data class Settings(
    val fontSize: TextUnit = 13.sp,
    val maxLineSymbols: Int = 120,
)

data class Line(val number: Int, val content: Content)

data class Lines(
    val data: List<String>,
) {
    val size: Int = data.size
    val lineNumberDigitCount: Int = size.toString().length

    operator fun get(index: Int): Line = Line(
        number = index,
        content = Content(data[index], true),
    )
}

data class Content(val value: String, val isCode: Boolean)

fun String.toCodeLines() = Lines(split("\n"))
