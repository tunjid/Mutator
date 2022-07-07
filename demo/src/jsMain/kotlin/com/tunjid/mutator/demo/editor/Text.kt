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
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
actual fun Heading1(text: String) {
    H3(
        attrs = {
        },
        content = {
            Text(
                value = text
            )
        }
    )
}

@Composable
actual fun Heading2(text: String) {
    H2(
        attrs = {
        },
        content = {
            Text(
                value = text
            )
        }
    )
}

@Composable
actual fun Heading3(text: String) {
    H3(
        attrs = {
        },
        content = {
            Text(
                value = text
            )
        }
    )
}

@Composable
actual fun Paragraph(text: String) {
    P(
        attrs = {
        },
        content = {
            Text(
                value = text
            )
        }
    )
}

