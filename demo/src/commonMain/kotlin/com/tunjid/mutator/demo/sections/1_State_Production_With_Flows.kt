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
import com.tunjid.mutator.demo.snails.Snail1
import com.tunjid.mutator.demo.snails.Snail2

@Composable
fun Section1() {
    SectionLayout {
        Markdown(introMarkdown)
        EditorView(snail1Code)
        Snail1()
        Markdown(threeMarkdown)
        EditorView(fourCode)
        Markdown(fiveMarkdown)
        EditorView(sixCode)
        Snail2()
        Markdown(sevenMarkdown)
    }
}

private val introMarkdown = """
# Producing State with Flows

State is what is. A declaration of things known at a certain point in time. As time passes however, state changes as data sources are updated and events happen. In mobile apps this presents a challenge; defining a convenient and concise means to produce state over time.


# Producing state

Producing state is at its core, is nothing more than combining sources of change and applying them. While a simple statement, there's a bit more to it than meets the eye. Let's start simple. In the following we have a Snail along a track. It has a single source of state change; time. Using a Flow, we can easily define the state for it.
""".trimIndent()

private val snail1Code = """
class Snail1StateHolder(
    scope: CoroutineScope
) {
    val progress: StateFlow<Float> = intervalFlow(500)
        .map { it.toFloat() % 100 }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = 0f
        )
}
""".trimIndent()

private val threeMarkdown = """
This works well since there's just a single source of state change. Things get a little more complicated if we have multiple sources that attempt to change state. First we introduce a new property to the snail; it's speed:
""".trimIndent()

private val fourCode = """
enum class Speed(val multiplier: Int){
    One(1), Two(2), Three(3), Four(4)
}
""".trimIndent()

private val fiveMarkdown = """
Next, we define a state for the snail, and a state holder that produces its state:
""".trimIndent()

private val sixCode = """
data class Snail2State(
    val progress: Float = 0f,
    val speed: Speed = Speed.One,
)

class Snail2StateHolder(
    scope: CoroutineScope
) {
    private val speed: Flow<Speed> = …

    private val progress: Flow<Float> = …


    val state: StateFlow<Snail2State> = combine(
        progress,
        speed,
        ::Snail2State
    )
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = Snail2State()
        )
}
""".trimIndent()

private val sevenMarkdown = """
In the above, we can see a general pattern emerging. For each source of state change, we can simply add its flow to the `combine` function to produce our state.    
""".trimIndent()