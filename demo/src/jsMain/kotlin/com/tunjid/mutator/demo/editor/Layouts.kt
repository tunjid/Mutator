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
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.dom.Div

@Composable
actual fun ContainerLayout(content: @Composable () -> Unit) {
    StyledDiv(content = content, classNames = listOf("container"))
}

@Composable
actual fun SectionLayout(content: @Composable () -> Unit) {
    StyledDiv(content = content, classNames = listOf("sectionLayout"))
}

@Composable
actual fun VerticalLayout(content: @Composable () -> Unit) {
    StyledDiv(content = content, classNames = listOf("verticalLayout"))
}

@Composable
actual fun HorizontalLayout(
    centerOnMainAxis: Boolean,
    content: @Composable () -> Unit,
) {
    StyledDiv(
        content = content,
        classNames = listOfNotNull(
            "horizontalLayout",
            "horizontallyCentered".takeIf { centerOnMainAxis },
        ),
    )
}

@Composable
fun StyledDiv(
    styles: StyleScope.() -> Unit = {},
    content: @Composable () -> Unit,
    classNames: List<String>,
) {
    Div(
        attrs = {
            style(styles)
            classes(*classNames.toTypedArray())
        },
        content = {
            content()
        },
    )
}
