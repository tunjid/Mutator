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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichText

@Composable
actual fun Paragraph(text: String) {
    StyledText(
        modifier = Modifier.padding(vertical = 8.dp),
        text = text,
        style = MaterialTheme.typography.body1.copy(
            MaterialTheme.colors.onSurface
        )
    )
}

@Composable
actual fun Markdown(content: String) {
    RichText(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Markdown(
            content = content
        )
    }
}

@Composable
actual fun CallToAction(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth(0.8f)
            .background(
                color = Color(0xE1F4FE),
                shape = RoundedCornerShape(size = 16.dp)
            ),
        content = {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.Center),
                color = Color(0x00589B),
                text = text
            )
        },
    )
}

@Composable
private fun StyledText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        style = style,
        text = text
    )
}

