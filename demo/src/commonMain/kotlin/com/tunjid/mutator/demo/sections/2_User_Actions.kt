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

package com.tunjid.mutator.demo.sections

import androidx.compose.runtime.Composable
import com.tunjid.mutator.demo.editor.EditorView
import com.tunjid.mutator.demo.editor.Markdown
import com.tunjid.mutator.demo.editor.SectionLayout
import com.tunjid.mutator.demo.snails.Snail3

@Composable
fun Section2() {
    SectionLayout {
        Markdown(oneMarkdown)
        EditorView(twoCode)
        Snail3()
        Markdown(threeMarkdown)
    }
}

private val oneMarkdown = """
# User actions

These sources of change mentioned above can be from anything that can be modeled as a flow, be it reading data from a database or user actions. Imagine if we want to be able to change the snail's color on a whim. We can add a `MutableStateFlow` for color of the snail to our state holder class, and then `combine` it with the rest of the flows to produce the state.    
""".trimIndent()

private val twoCode = """
data class Snail3State(
    val progress: Float = 0f,
    val speed: Speed = Speed.One,
    val color: Color = Color.Blue,
)

class Snail3StateHolder(
    scope: CoroutineScope
) {

    private val speed: Flow<Speed> = …

    private val progress: Flow<Float> = …

    private val color: MutableStateFlow<Color> = MutableStateFlow(Color.Cyan)
    
    val state: StateFlow<Snail3State> = combine(
        progress,
        speed,
        color,
        ::Snail3State
    )
        .stateIn(...)

    fun setSnailColor(color: Color) {
        this.color.value = color
    }
}    
""".trimIndent()

private val threeMarkdown = """
When the user wants to change the color, the `setColor` method is called, the `color` `MutableStateFlow` has its value updated, and it is emitted into the stream produced by the `combine` function to produce the new state.    
""".trimIndent()