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
import androidx.compose.ui.graphics.Color
import com.tunjid.mutator.demo.Speed
import com.tunjid.mutator.demo.editor.HorizontalLayout
import com.tunjid.mutator.demo.editor.StyledDiv
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.max
import org.jetbrains.compose.web.attributes.min
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input

@Composable
actual fun SnailCard(
    color: Color,
    content: @Composable () -> Unit
) {
    StyledDiv(
        content = content,
        classNames = listOf("card"),
        styles = {
            backgroundColor(color.rgb())
        }
    )
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
            classes("snail", "horizontallyCentered")
            style {
                property("--snailColor", color.hex())
            }
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
    HorizontalLayout(centerOnMainAxis = true) {
        colors.forEachIndexed { index, color ->
            Div(
                attrs = {
                    classes("colorSwatch")
                    style {
                        backgroundColor(
                            color.rgb()
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
    progress: Float,
    onClicked: () -> Unit
) = Div(
    attrs = {
        classes("progress-btn", "horizontallyCentered")
        onClick { onClicked() }
    },
    content = {
        Div(
            attrs = {
                classes("btn")
            },
            content = {
                "Toggle mode"
            }
        )
        Div(
            attrs = {
                classes("progress")
                style { width(((progress % 100) * 100).toInt().percent) }
            }
        )
    }
)

private fun Color.rgb() = rgb(
    r = (red * 255).toInt(),
    g = (green * 255).toInt(),
    b = (blue * 255).toInt(),
)

private fun Color.hex(): String {
    val r = (red * 255).toInt().toHexString()
    val g = (green * 255).toInt().toHexString()
    val b = (blue * 255).toInt().toHexString()
    return "#$r$g$b"
}

private fun Int.toHexString(): String {
    val hex = this.toString(16)
    return if (hex.length == 1) "0$hex" else hex
}
