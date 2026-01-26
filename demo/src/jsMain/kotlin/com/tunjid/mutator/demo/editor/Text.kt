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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.pt
import org.jetbrains.compose.web.dom.Code
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Pre
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement
import react.FC
import react.Props
import react.ReactElement
import react.create
import react.dom.client.createRoot
import web.dom.Element

@Composable
actual fun Paragraph(text: String) = P(
    content = {
        Text(value = text)
    },
)

@Composable
actual fun Markdown(content: String) = Div(
    attrs = {
        classes("markdown")
    },
    content = {
        UseReactEffect(
            content,
            reactMarkdown.create {
                children = content
            },
        )
    },
)

@Composable
actual fun CallToAction(
    text: String,
    centerText: Boolean,
) = Div(
    attrs = {
        classes(
            listOfNotNull(
                "cta",
                "horizontallyCentered",
                "centerText".takeIf { centerText },
            ),
        )
    },
) {
    UseReactEffect(
        text,
        reactMarkdown.create {
            children = text
        },
    )
}

@Composable
actual fun CodeBlock(content: String) = key(content) {
    Pre {
        Code({
            classes("language-kotlin", "hljs")
            style {
                property("font-family", "'JetBrains Mono', monospace")
                property("tab-size", 4)
                fontSize(10.pt)
                backgroundColor(Color("transparent"))
            }
        }) {
            @Suppress("DEPRECATION")
            DomSideEffect(content) {
                it.setHighlightedCode(content)
            }
        }
    }
}

@Composable
private fun ElementScope<HTMLElement>.UseReactEffect(
    key: Any?,
    content: ReactElement<*>,
) {
    DisposableEffect(key1 = key) {
        val root = createRoot(scopeElement as Element)
        root.render(content)
        onDispose { root.unmount() }
    }
}

@JsName("ReactMarkdown")
@JsModule("react-markdown")
@JsNonModule
external val reactMarkdown: FC<ReactMarkdownProps>

external interface ReactMarkdownProps : Props {
    var children: String
}

@JsName("hljs")
@JsModule("highlight.js")
@JsNonModule
external class HighlightJs {
    companion object {
        fun highlightElement(block: HTMLElement)
    }
}

private fun HTMLElement.setHighlightedCode(code: String) {
    innerText = code
    HighlightJs.highlightElement(this)
}
