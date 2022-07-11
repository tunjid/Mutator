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

package com.tunjid.mutator.demo

import kotlin.math.sqrt

actual class Color actual constructor(color: Long) {

    actual val argb: Int = color.toInt()

    actual val r: Int = argb and 0x00FF0000 shr 16
    actual val g: Int = argb and 0x0000FF00 shr 8
    actual val b: Int = argb and 0x000000FF

    actual companion object {
        actual val Black: Color = Color(0x000000)
        actual val LightGray: Color = Color(0xFFCCCCCC)
    }

    actual fun isBright(): Boolean {
        val hsp = sqrt(
            0.299 * (r * r) +
                    0.587 * (g * g) +
                    0.114 * (b * b)
        )
        return hsp > 127.5
    }
}