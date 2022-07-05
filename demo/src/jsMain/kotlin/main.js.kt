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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import com.tunjid.mutator.demo.App
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.WheelEvent
import kotlin.math.max

fun main() {
    var scrollOffset by mutableStateOf(0)
    window.onload = {
        resize()
        onWasmReady {
            Window("The state production pipeline") {
                App(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned {
                            it.size
                            println("WIDTH: ${it.size.width}")
                            println("HEIGHT: ${it.size.height}")

                        }
//                        .offset(y = scrollOffset.dp)
                )
            }
        }
    }
//    window.addEventListener(type = "resize", callback = {
//        resize()
//    })

//    window.addEventListener(type = "wheel", callback = { event ->
//        event.preventDefault()
//        val wheelEvent = event as WheelEvent
////        println("dy: ${wheelEvent.deltaY}; y: ${wheelEvent.y}")
////        scrollOffset = max(0, scrollOffset + wheelEvent.deltaY.toInt())
//        scrollOffset += wheelEvent.deltaY.toInt()
//        println("scrollOffset: $scrollOffset")
//
//    })
}

private val canvas get() = document.getElementById("ComposeTarget") as HTMLCanvasElement
private fun resize() {
    println("window.innerWidth: ${window.innerWidth}")
    println("window.innerHeight: ${window.innerHeight}")
    println("Have canvas: ${canvas != null}")

    canvas.width = window.innerWidth
    canvas.height = 10_000
}