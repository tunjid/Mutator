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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.css.maxHeight
import org.jetbrains.compose.web.css.overflow
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.pt
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Code
import org.jetbrains.compose.web.dom.Pre
import org.w3c.dom.HTMLElement

@Composable
actual fun EditorView(content: String) = key(content) {
    FormattedCodeSnippet(content)
}

@Composable
fun FormattedCodeSnippet(code: String, language: String = "kotlin") {
    Pre({
        style {
            maxHeight(25.em)
            width(100.percent)
            overflow("auto")
            height(auto)
        }
    }) {
        Code({
            classes("language-$language", "hljs")
            style {
                property("font-family", "'JetBrains Mono', monospace")
                property("tab-size", 4)
                fontSize(10.pt)
                backgroundColor(Color("transparent"))
            }
        }) {
            @Suppress("DEPRECATION")
            DomSideEffect(code) {
                it.setHighlightedCode(code)
            }
        }
    }
}

private fun HTMLElement.setHighlightedCode(code: String) {
    innerText = code
    HighlightJs.highlightElement(this)
}

@JsName("hljs")
@JsModule("highlight.js")
@JsNonModule
external class HighlightJs {
    companion object {
        fun highlightElement(block: HTMLElement)
    }
}
