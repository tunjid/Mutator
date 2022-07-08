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
import androidx.compose.ui.graphics.Color
import com.tunjid.mutator.demo.Speed
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.max
import org.jetbrains.compose.web.attributes.min
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input

@Composable
actual fun SectionLayout(content: @Composable () -> Unit) {
    StyledDiv(content, "verticalLayout")
}

@Composable
actual fun VerticalLayout(content: @Composable () -> Unit) {
    StyledDiv(content, "verticalLayout")
}

@Composable
actual fun HorizontalLayout(content: @Composable () -> Unit) {
    StyledDiv(content, "horizontalLayout")
}

@Composable
actual fun SnailCard(content: @Composable () -> Unit) {
    StyledDiv(content, "card")
}

@Composable
actual fun Snail(
    progress: Float,
    speed: Speed,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Input(
        type = InputType.Range,
        attrs = {
            classes("snail")
            min("0")
            max("100")
            value(progress)
            onInput { onValueChange(it.value?.toFloat() ?: 0f) }
        }
    )
}

@Composable
actual fun ColorSwatch(
    colors: List<Color>,
    onColorClicked: (Int) -> Unit
) {
    HorizontalLayout {
        colors.forEachIndexed { index, color ->
            Div(
                attrs = {
                    style {
                        height(100.px)
                        width(100.px)
                        backgroundColor(
                            rgba(
                                r = color.red,
                                g = color.green,
                                b = color.blue,
                                a = color.alpha
                            )
                        )
                    }
                    onClick { onColorClicked(index) }
                },
                content = {
                }
            )
        }
    }
}

@Composable
actual fun ToggleButton(
    onClicked: () -> Unit
) {
    Button(
        attrs = {
            title("Toggle mode")
            onClick { onClicked() }
        },
        content = { }
    )
}

@Composable
private fun StyledDiv(
    content: @Composable () -> Unit,
    vararg classNames: String
) {
    Div(
        attrs = {
            classes(*classNames)
        },
        content = {
            content()
        }
    )
}