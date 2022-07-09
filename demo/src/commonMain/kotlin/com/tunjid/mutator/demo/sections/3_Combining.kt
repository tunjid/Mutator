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

@Composable
fun Section3() {
    SectionLayout {
        Markdown(oneMarkdown)
        EditorView(twoCode)
        Markdown(threeMarkdown)
    }
}

private val oneMarkdown = """
# Combining changes in state

Combining sources of state as a general means of state production is rather robust and it lends itself to a wide range of cases, and works well for simple to moderate state production pipelines. It also scales linearly, that is each source of state change will need to be added to the `combine` function. This poses a problem for states with more than 5 sources of change as the `combine` function allows for at most 5 flows. This is called the arity of the `combine` function.

One way around this is to combine the sources of state change into intermediate states, before combining them again into the final state.    
""".trimIndent()

private val twoCode = """
data class LargeState(
    val property1: Int,
    val property2: Int,
    val property3: Int,
    val property4: Int,
    val property5: Int,
    val property6: Int,
    val property7: Int,
    val property8: Int,
)

data class IntermediateState1(
    val property1: Int,
    val property2: Int,
    val property3: Int,
    val property4: Int,
)

data class IntermediateState2(
    val property5: Int,
    val property6: Int,
    val property7: Int,
    val property8: Int,
)

fun intFlow() = flowOf(1)

class LargeStateHolder {
    private val intermediateState1 = combine(
        intFlow(),
        intFlow(),
        intFlow(),
        intFlow(),
        ::IntermediateState1
    )

    private val intermediateState2 = combine(
        intFlow(),
        intFlow(),
        intFlow(),
        intFlow(),
        ::IntermediateState2
    )

    val state = combine(
        intermediateState1,
        intermediateState2
    ) { state1, state2 ->
        LargeState(
            state1.property1,
            state1.property2,
            state1.property3,
            state1.property4,
            state2.property5,
            state2.property5,
            state2.property6,
            state2.property7,
        )
    }
}    
""".trimIndent()

private val threeMarkdown = """
The above works, but can be difficult to maintain. As new sources of state are added or removed over time, the signatures of the `IntermediateState` instances will need to change to accommodate the arity of the `combine` function causings cascading changes. This brings us to another state production approach, merging sources of change.
""".trimIndent()