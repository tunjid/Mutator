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

import androidx.compose.ui.window.Window
import com.tunjid.mutator.demo.App
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

fun main() {
    window.onload = {
        val canvas = document.getElementById("ComposeTarget")

        println("window.innerWidth: ${window.innerWidth}")
        println("window.innerHeight: ${window.innerHeight}")
        println("Have canvas: ${canvas != null}")

        (canvas as HTMLElement)?.style.apply {
            width = "${window.innerWidth}px"
        }
//        println("canvas.clientWidth: ${canvas.clientWidth}")
//        println("canvas.clientHeight: ${canvas.clientHeight}")

        onWasmReady {
            Window("The state production pipeline") {
                App()
            }
        }
    }
}
