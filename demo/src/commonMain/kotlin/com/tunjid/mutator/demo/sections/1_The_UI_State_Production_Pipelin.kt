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

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tunjid.mutator.demo.editor.EditorView
import com.tunjid.mutator.demo.editor.H1
import com.tunjid.mutator.demo.editor.P
import com.tunjid.mutator.demo.snails.Snail1
import com.tunjid.mutator.demo.snails.Snail2

@Composable
fun Section1() {
    Column {
        H1(
            text =
            "The UI State Production Pipeline"
        )
        P(
            text =
            "State is what is. A declaration of things known at a certain point in time. As time passes however, state changes as data sources are updated and events happen. In mobile apps this presents a challenge; defining a convenient and concise means to produce state over time."
        )
        H1(
            text =
            "Producing state"
        )
        P(
            text =
            "Producing state is at its core, is nothing more than combining sources of change and applying them. While a simple statement, there's a bit more to it than meets the eye. Let's start simple. In the following we have a Snail along a track. It has a single source of state change; time. Using a Flow, we can easily define the state for it."
        )
        EditorView(
            content =
            "class Snail1StateHolder(\n" +
                    "    scope: CoroutineScope\n" +
                    ") {\n" +
                    "    val progress: StateFlow<Float> = intervalFlow(500)\n" +
                    "        .map { it.toFloat() % 100 }\n" +
                    "        .stateIn(\n" +
                    "            scope = scope,\n" +
                    "            started = SharingStarted.WhileSubscribed(),\n" +
                    "            initialValue = 0f\n" +
                    "        )\n" +
                    "}\n"
        )
        Snail1(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        P(
            text =
            "This works well since there's just a single source of state change. Things get a little more complicated if we have multiple sources that attempt to change state. First we introduce a new property to the snail; it's speed:"
        )
        EditorView(
            content =
                "enum class Speed(val multiplier: Int){\n" +
                        "    One(1), Two(2), Three(3), Four(4)\n" +
                        "}\n"
        )
        P(
            text =
            "Next, we define a state for the snail, and a state holder that produces its state:"
        )
        EditorView(
            content =
            "data class Snail2State(\n" +
                    "    val progress: Float = 0f,\n" +
                    "    val speed: Speed = Speed.One,\n" +
                    ")\n" +
                    "\n" +
                    "class Snail2StateHolder(\n" +
                    "    scope: CoroutineScope\n" +
                    ") {\n" +
                    "    private val speed: Flow<Speed> = …\n" +
                    "\n" +
                    "    private val progress: Flow<Float> = …\n" +
                    "\n" +
                    "\n" +
                    "    val state: StateFlow<Snail2State> = combine(\n" +
                    "        progress,\n" +
                    "        speed,\n" +
                    "        ::Snail2State\n" +
                    "    )\n" +
                    "        .stateIn(\n" +
                    "            scope = scope,\n" +
                    "            started = SharingStarted.WhileSubscribed(),\n" +
                    "            initialValue = Snail2State()\n" +
                    "        )\n" +
                    "}\n"
        )
        Snail2(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        P(
            text =
            "In the above, we can see a general pattern emerging. For each source of state change, we can simply add its flow to the `combine` function to produce our state."
        )
    }
}