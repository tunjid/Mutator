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
import com.tunjid.mutator.demo.editor.CodeBlock
import com.tunjid.mutator.demo.editor.Markdown
import com.tunjid.mutator.demo.editor.SectionLayout
import com.tunjid.mutator.demo.snails.Snail6

@Composable
fun Section5() = SectionLayout {
    Markdown(oneMarkdown)
    CodeBlock(oneCode)
    Snail6()
    CallToAction(oneCta)
    Markdown(twoMarkdown)
    CodeBlock(twoCode)
    CallToAction(twoCta)
}

private val oneMarkdown = """
# Mutations are updates to state

The method of state production described above may seem unfamiliar at first, however it simply is a variation of a common state production technique. If the snail in the previous example didn't move on its own, and the only source of state change were user actions, the snail's state could be modeled with a single `MutableStateFlow`: 
""".trimIndent()

private val oneCode = """
data class Snail6State(
    ...
)

class Snail6StateHolder {

    private val _state = MutableStateFlow(Snail6State())

    val state: StateFlow<Snail6State> = _state.asStateFlow()

    fun setSnailColor(index: Int) {
        _state.update {
            it.copy(color = it.colors[index])
        }
    }

    fun setProgress(progress: Float) {
        _state.update {
            it.copy(progress = progress)
        }
    }
} 
""".trimIndent()

private val oneCta = """
The snail's only source of state change are user actions. Interact with it to drive it's state.
""".trimIndent()

private val twoMarkdown = """
In the above, the `_state` variable is updated using a lambda `(State) -> State`. This lambda has the same signature as a `Mutation` (`State.() -> State`), although expressed in way that does not require a named parameter.
That is, the following method declarations are similar:
""".trimIndent()

private val twoCode = """
class Snail5StateHolder {
    ...
    fun setSnailColor(index: Int) {
        scope.launch {
            userChanges.emit { copy(color = colors[index]) }
        }
    }
} 

class Snail6StateHolder {
    ...
    fun setSnailColor(index: Int) {
        _state.update {
            it.copy(color = it.colors[index])
        }
    }
} 
""".trimIndent()

private val twoCta = """
Merging `Mutation` instances can be thought of an extension to `MutableStateFlow.update { ... }`, where updates can come from both user actions and other `Flow` instances.
""".trimIndent()