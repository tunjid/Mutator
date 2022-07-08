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
import com.tunjid.mutator.demo.editor.Heading2
import com.tunjid.mutator.demo.editor.Markdown
import com.tunjid.mutator.demo.editor.Paragraph
import com.tunjid.mutator.demo.editor.SectionLayout

@Composable
fun Section4() = SectionLayout {
    Heading2(
        text =
        "Merging as a method of state production"
    )
    Paragraph(
        text =
        """
                We've established that state production fundamentally is introducing changes to state over time. That is the new state is the old state plus the change in state. Expressing this mathematically becomes:

newState = oldState + Δstate
Where Δstate is the change in state.

How do we express the functional declaration above in code though? Fortunately Kotlin has functional literals that enable us to do just that. First we define Δstate as
            """.trimIndent()
    )
    EditorView(
        content =
        """
data class Mutation<T : Any>(
val mutate: T.() -> T
)
        """.trimIndent()
    )
    Markdown(
        """
        `Hello` in Markdown
    """.trimIndent()
    )
}