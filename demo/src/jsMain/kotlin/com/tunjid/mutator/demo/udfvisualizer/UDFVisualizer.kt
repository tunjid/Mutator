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

package com.tunjid.mutator.demo.udfvisualizer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.tunjid.mutator.demo.hex
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.background
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.bottom
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.left
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.minWidth
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.paddingBottom
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.paddingRight
import org.jetbrains.compose.web.css.paddingTop
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.top
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.css.Color as CSSColor

private val marbleSize = 22.px

@Composable
actual fun UDFVisualizer(stateHolder: UDFVisualizerStateHolder) {
    val state by stateHolder.state.collectAsState()

    Div(
        {
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                alignItems("center")
                width(100.percent)
                padding(0.px)
                margin(0.px)
            }
        },
        {
            Label("State Holder")
            Marbles(state.marbles.filteredForVisualization())
            Label("UI")
        }
    )
}

@Composable
private fun Label(text: String) = P(
    attrs = {
        style {
            width(200.px)
            borderRadius(10.px)
            paddingLeft(16.px)
            paddingTop(8.px)
            paddingRight(16.px)
            paddingBottom(8.px)
            textAlign("center")
            background("#052F41")
            color(CSSColor("#FFFFFF"))
            margin(0.px)
            property("z-index", 1)
        }
    },
    content = {
        Text(value = text)
    }
)

@Composable
private fun Marbles(marbles: List<Marble>) = Div(
    attrs = {
        style {
            width(200.px)
            height(120.px)
            position(Position.Relative)
            padding(0.px)
            margin(0.px)
        }
    },
    content = {
        marbles.forEach { marble ->
            Marble(marble)
        }
    }
)

@Composable
private fun Marble(marble: Marble) = Div(
    attrs = {
        style {
            property("z-index", 0)
            minWidth(marbleSize)
            height(marbleSize)
            borderRadius(marbleSize)
            position(Position.Absolute)
            color(CSSColor("#FFFFFF"))
            textAlign("center")
            fontSize(13.px)
            padding(0.px)
            margin(0.px)
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            background(
                when (marble) {
                    is Marble.Event -> when (val core = marble.metadata) {
                        Marble.Metadata.Nothing -> marble.hexColor()
                        is Marble.Metadata.Text -> marble.hexColor()
                        is Marble.Metadata.Tint -> core.color.hex()
                    }

                    is Marble.State -> marble.color.hex()
                }
            )
            left(
                when (marble) {
                    is Marble.Event -> 80.percent
                    is Marble.State -> 20.percent
                }
            )
            when (marble) {
                is Marble.Event -> bottom(marble.percentage.percent)
                is Marble.State -> top(marble.percentage.percent)
            }
        }
    },
    content = {
        when (val core = marble.metadata) {
            Marble.Metadata.Nothing,
            is Marble.Metadata.Tint -> Unit

            is Marble.Metadata.Text -> Text(value = core.text)
        }
    }
)

private fun Marble.color() = when (this) {
    is Marble.Event -> eventColor
    is Marble.State -> stateColor
}

private fun Marble.hexColor() = color().hex()

private fun List<Marble>.filteredForVisualization(): List<Marble> {
    val (states, events) = partition { it is Marble.State }
    val filtered = states
        .filterIsInstance<Marble.State>()
        .filter { it.index % 5 == 0 }
    return filtered + events
}