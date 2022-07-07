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

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.tunjid.mutator.demo.App
import java.util.Base64


fun main() {
    val fonts = listOf(
        "jetbrainsmono_bold.ttf",
        "jetbrainsmono_bold_italic.ttf",
        "jetbrainsmono_extrabold.ttf",
        "jetbrainsmono_extrabold_italic.ttf",
        "jetbrainsmono_italic.ttf",
        "jetbrainsmono_medium.ttf",
        "jetbrainsmono_medium_italic.ttf",
        "jetbrainsmono_regular.ttf",
    )
    val contextClassLoader = Thread.currentThread().contextClassLoader!!

    println(fonts.map { font ->
        val resourceName = "font/$font"
        val resource = contextClassLoader.getResourceAsStream(resourceName)
            ?: error("Can't load font from $resourceName")

        val bytes = resource.use { it.readBytes() }
        val base64 = String(Base64.getEncoder().encode(bytes))
        base64
    })

    singleWindowApplication(
        title = "State",
        state = WindowState(size = DpSize(800.dp, 800.dp))
    ) {
        val scrollState = rememberScrollState()
        App(
            modifier = Modifier
        )
    }
}

