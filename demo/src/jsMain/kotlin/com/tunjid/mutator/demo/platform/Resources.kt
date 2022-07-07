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

package com.tunjid.mutator.demo.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlinx.browser.window

private val fontNames = listOf(
    "jetbrainsmono_bold.ttf",
    "jetbrainsmono_bold_italic.ttf",
    "jetbrainsmono_extrabold.ttf",
    "jetbrainsmono_extrabold_italic.ttf",
    "jetbrainsmono_italic.ttf",
    "jetbrainsmono_medium.ttf",
    "jetbrainsmono_medium_italic.ttf",
    "jetbrainsmono_regular.ttf",
)


private val fontMap = fontNames.zip(fonts.split(",")).toMap()

@Composable
actual fun Font(name: String, res: String, weight: FontWeight, style: FontStyle): Font {
//   val reader = FileReaderSync()
//    reader.readAsArrayBuffer(File())

    println("READING $res")
    val base64 = fontMap.getValue("$res.ttf")
    val decoded = window.atob(base64)
    decoded.map { it.code.toByte() }.toByteArray()

    return androidx.compose.ui.text.platform.Font(
        identity = name,
//        data = decoded.encodeToByteArray(),
        data = decoded.map { it.code.toByte() }.toByteArray(),
        weight,
        style
    )
}