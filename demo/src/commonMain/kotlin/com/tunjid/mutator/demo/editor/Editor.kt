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

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class Editor(
    val lines: Lines,
) {

    class Line(val number: Int, val content: Content)

    interface Lines {
        val lineNumberDigitCount: Int get() = size.toString().length
        val size: Int
        operator fun get(index: Int): Line
    }


    class Content(val value: State<String>, val isCode: Boolean)
}


fun String.toCodeLines() = object : Editor.Lines {
    val split = split("\n")
    override val size: Int
        get() = split.size

    override fun get(index: Int): Editor.Line = Editor.Line(
        number = index,
        content = Editor.Content(mutableStateOf(split[index]), true)
    )
}