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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.tunjid.mutator.demo.platform.Font

object Fonts {
    @Composable
    fun jetbrainsMono() = FontFamily(
        Font(
            "JetBrains Mono",
            "jetbrainsmono_regular",
            FontWeight.Normal,
            FontStyle.Normal,
        ),
        Font(
            "JetBrains Mono",
            "jetbrainsmono_italic",
            FontWeight.Normal,
            FontStyle.Italic,
        ),

        Font(
            "JetBrains Mono",
            "jetbrainsmono_bold",
            FontWeight.Bold,
            FontStyle.Normal,
        ),
        Font(
            "JetBrains Mono",
            "jetbrainsmono_bold_italic",
            FontWeight.Bold,
            FontStyle.Italic,
        ),

        Font(
            "JetBrains Mono",
            "jetbrainsmono_extrabold",
            FontWeight.ExtraBold,
            FontStyle.Normal,
        ),
        Font(
            "JetBrains Mono",
            "jetbrainsmono_extrabold_italic",
            FontWeight.ExtraBold,
            FontStyle.Italic,
        ),

        Font(
            "JetBrains Mono",
            "jetbrainsmono_medium",
            FontWeight.Medium,
            FontStyle.Normal,
        ),
        Font(
            "JetBrains Mono",
            "jetbrainsmono_medium_italic",
            FontWeight.Medium,
            FontStyle.Italic,
        ),
    )
}
