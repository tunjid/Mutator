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
import com.tunjid.mutator.demo.editor.CallToAction
import com.tunjid.mutator.demo.editor.EditorView
import com.tunjid.mutator.demo.editor.Markdown
import com.tunjid.mutator.demo.editor.SectionLayout
import com.tunjid.mutator.demo.snails.Snail4
import com.tunjid.mutator.demo.snails.Snail5

@Composable
fun Section4() = SectionLayout {
    Markdown(oneMarkdown)
    CallToAction(twoCta)
    Markdown(threeMarkdown)
    EditorView(fourCode)
    Markdown(fiveMarkdown)
    EditorView(sixCode)
    Snail4()
    CallToAction("Snail4 is identical to Snail3; just with different state production semantics.")
    Markdown(sevenMarkdown)
    EditorView(eightCode)
    Snail5()
    CallToAction("Drag the snail to place it anywhere on its track.")
    Markdown(nineMarkdown)
}

private val oneMarkdown = """
# Merging as a method of state production

We've established that state production fundamentally is introducing changes to state over time. That is the new state is the old state plus the change in state. Expressing this mathematically becomes:    
""".trimIndent()

private val twoCta = """
newState = oldState + Δstate    
""".trimIndent()

private val threeMarkdown = """
 Where Δstate is the change in state.

How do we express the functional declaration above in code though? Fortunately Kotlin has functional literals that enable us to do just that. First we define Δstate as   
""".trimIndent()

private val fourCode = """
data class Mutation<State : Any>(
    val mutate: State.() -> State
)    
""".trimIndent()

private val fiveMarkdown = """
That is, the unit of state change (Δstate) for any state `T` is a `Mutation`; a data class carrying a lambda with `T` as the receiver and when invoked, returns `T`. The above is extremely powerful as we can represent any state change for any state declaration with a single type.

To produce state then, we simply have to start with an initial state, and incrementally apply state `Mutation`s to it over time to create our state production pipeline. This is sometimes called reducing changes into state.

Expressing this in code with `Flows` is very concise and requires just two operators:


* `merge`: Used to merge all sources of `Mutation<State>` (Δstate) into a single stream
* `scan`: Used to reduce the stream of `Mutation<State>` into an initial state.

In our snail example, we can express the same state production pipeline with user actions as:    
""".trimIndent()

private val sixCode = """
data class Snail4State(
    ...,
)

class Snail4StateHolder(
    private val scope: CoroutineScope
) {

    private val speed: Flow<Speed> = …

    private val speedChanges: Flow<Mutation<Snail4State>> = speed
        .map { Mutation { copy(speed = it) } }

    private val progressChanges: Flow<Mutation<Snail4State>> = intervalFlow
        .map { Mutation { copy(progress = (progress + 1) % 100) } }

    private val userChanges = MutableSharedFlow<Mutation<Snail4State>>()

    val state: StateFlow<Snail4State> = merge(
        progressChanges,
        speedChanges,
        userChanges,
    )
        .scan(Snail4State()) { state, mutation -> mutation.mutate(state) }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = Snail4State()
        )

    fun setSnailColor(color: Color) {
        scope.launch {
            userChanges.emit { copy(color = color) }
        }
    }
}    
""".trimIndent()

private val sevenMarkdown = """
This example has all the functionality the combine approach did, but with a slight complexity cost.

It however brings the following advantages:



* All user actions that change state can share the same source flow `userChanges`
* All source flows can independently contribute their state mutations
* All source flows at the time of introducing the state mutation can read the existing state.

This advantage can be easily illustrated with an example. If the user wanted to manually move the snail to a certain position, it would simply be:    
""".trimIndent()

private val eightCode = """
class Snail5StateHolder(
    private val scope: CoroutineScope
) {

   private val progressChanges: Flow<Mutation<Snail5State>> = …
    …

    private val userChanges = MutableSharedFlow<Mutation<Snail5State>>()

   fun setProgress(progress: Float) {
        scope.launch {
            userChanges.emit { copy(progress = progress) }
        }
    }
}    
""".trimIndent()

private val nineMarkdown = """
That is, we can simply introduce a state change to the `progress` property of the state despite the `progessChanges` flow also contributing to a change of the same property. This is something that would be non trivial with the `combine` approach.    
""".trimIndent()