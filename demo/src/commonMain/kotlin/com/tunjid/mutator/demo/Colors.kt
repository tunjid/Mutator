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

import kotlin.math.pow
import kotlin.math.roundToInt

expect class Color(color: Long) {
    val argb: Int
    val r: Int
    val g: Int
    val b: Int

    fun isBright(): Boolean

    companion object {
        val Black: Color
        val LightGray: Color
    }
}


object MutedColors {
    private val mutedColors = listOf(
        Color(0xFF2980b9), // Belize Hole
        Color(0xFF2c3e50), // Midnight Blue
        Color(0xFFc0392b), // Pomegranate
        Color(0xFF16a085), // Green Sea
        Color(0xFF7f8c8d), // Concrete
        Color(0xFFFFFF),
    )

    private val darkerMutedColors = listOf(
        Color(0xFF304233),
        Color(0xFF353b45),
        Color(0xFF392e3a),
        Color(0xFF3e2a2a),
        Color(0xFF474747),
        Color(0x292929)
    )

    fun colors(isDark: Boolean) = if (isDark) darkerMutedColors else mutedColors
}

fun interpolateColors(fraction: Float, startValue: Int, endValue: Int): Int {
    val startA = (startValue shr 24 and 0xff) / 255.0f
    var startR = (startValue shr 16 and 0xff) / 255.0f
    var startG = (startValue shr 8 and 0xff) / 255.0f
    var startB = (startValue and 0xff) / 255.0f
    val endA = (endValue shr 24 and 0xff) / 255.0f
    var endR = (endValue shr 16 and 0xff) / 255.0f
    var endG = (endValue shr 8 and 0xff) / 255.0f
    var endB = (endValue and 0xff) / 255.0f

    // convert from sRGB to linear
    startR = startR.toDouble().pow(2.2).toFloat()
    startG = startG.toDouble().pow(2.2).toFloat()
    startB = startB.toDouble().pow(2.2).toFloat()
    endR = endR.toDouble().pow(2.2).toFloat()
    endG = endG.toDouble().pow(2.2).toFloat()
    endB = endB.toDouble().pow(2.2).toFloat()

    // compute the interpolated color in linear space
    var a = startA + fraction * (endA - startA)
    var r = startR + fraction * (endR - startR)
    var g = startG + fraction * (endG - startG)
    var b = startB + fraction * (endB - startB)

    // convert back to sRGB in the [0..255] range
    a *= 255.0f
    r = r.toDouble().pow(1.0 / 2.2).toFloat() * 255.0f
    g = g.toDouble().pow(1.0 / 2.2).toFloat() * 255.0f
    b = b.toDouble().pow(1.0 / 2.2).toFloat() * 255.0f

    return a.roundToInt() shl 24 or (r.roundToInt() shl 16) or (g.roundToInt() shl 8) or b.roundToInt()
}