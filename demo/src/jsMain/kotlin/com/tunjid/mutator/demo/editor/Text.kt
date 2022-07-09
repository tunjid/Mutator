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
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement
import react.FC
import react.Props
import react.ReactElement
import react.create
import react.dom.render
import react.dom.unmountComponentAtNode


@Composable
actual fun Paragraph(text: String) = P(
    content = {
        Text(value = text)
    }
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
            }
        )
    }
)

@Composable
actual fun CallToAction(
    text: String,
    centerText: Boolean,
) = Div(
    attrs = {
        classes("cta", "horizontallyCentered")
    }
) {
    P(
        attrs = {
            if (centerText) {
                style { textAlign("center") }
            }
        },
        content = {
            Text(value = text)
        }
    )
}

@Composable
private fun ElementScope<HTMLElement>.UseReactEffect(
    key: Any?,
    content: ReactElement<*>
) {
    DomSideEffect(key = key) { htmlElement ->
        render(
            element = content,
            container = htmlElement
        )
    }

    DisposableRefEffect { htmlElement ->
        onDispose {
            unmountComponentAtNode(htmlElement)
        }
    }
}

@JsName("ReactMarkdown")
@JsModule("react-markdown")
@JsNonModule
external val reactMarkdown: FC<ReactMarkdownProps>

external interface ReactMarkdownProps : Props {
    var children: String
}
