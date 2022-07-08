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
import com.tunjid.mutator.demo.snails.Snail6

@Composable
fun Section5() = SectionLayout {
    Markdown(oneMarkdown)
    EditorView(twoCode)
    Markdown(threeMarkdown)
    EditorView(fourCode)
    Snail6()
}

private val oneMarkdown = """
# Formalizing a state production pipeline

The merge approach can be formalized into a function on the `CoroutineScope` the state is produced in.
""".trimIndent()

private val twoCode = """
fun <State : Any> CoroutineScope.mutateState(
    initial: State,
    started: SharingStarted,
    mutationFlows: List<Flow<Mutation<State>>>
): StateFlow<State>  
""".trimIndent()

private val threeMarkdown = """
Where the use of it in the snail example becomes:
""".trimIndent()

private val fourCode = """
class Snail6StateHolder(
    private val scope: CoroutineScope
) {

    private val speedChanges: Flow<Mutation<Snail6State>> = …

    private val progressChanges: Flow<Mutation<Snail6State>> = …

    private val userChanges = MutableSharedFlow<Mutation<Snail6State>>()

    val state: StateFlow<Snail6State> = scope.mutateState(
        initial = Snail6State(),
        started = SharingStarted.WhileSubscribed(),
        mutationFlows = listOf(
            speedChanges,
            progressChanges,
            userChanges,
        )
    )

    fun setSnailColor(color: Color) {
        ..
    }

    fun setProgress(progress: Float) {
        …
    }
} 
""".trimIndent()
