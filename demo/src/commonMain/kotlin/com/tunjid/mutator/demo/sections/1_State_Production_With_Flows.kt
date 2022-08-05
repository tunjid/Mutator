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
import com.tunjid.mutator.demo.snails.Snail0
import com.tunjid.mutator.demo.snails.Snail1
import com.tunjid.mutator.demo.snails.Snail2

@Composable
fun Section1() {
    SectionLayout {
        Markdown(headingMarkdown)
        Snail0()
        Markdown(introMarkdown)
        CallToAction(disclaimerCta)
        Markdown(producingStateIntro)
        CodeBlock(snail1Code)
        Snail1()
        CallToAction(snail1Cta)
        Markdown(threeMarkdown)
        CodeBlock(fourCode)
        Markdown(fiveMarkdown)
        CodeBlock(sixCode)
        Snail2()
        CallToAction(snail2Cta)
        Markdown(sevenMarkdown)
    }
}

private val headingMarkdown = """
# State production with unidirectional data flow and Kotlin Flows
""".trimIndent()

private val introMarkdown = """
By [Adetunji Dahunsi](https://twitter.com/Tunji_D).

State is what is. A declaration of things known at a certain point in time. As time passes however, state changes as data sources backing the state are updated and events happen. In mobile apps this presents a challenge; defining a convenient and concise means to produce state over time.

This page is a [Jetpack Compose](https://developer.android.com/jetpack/compose?gclid=Cj0KCQjwzqSWBhDPARIsAK38LY-nnY_1sTpVvpENJZD5ek-tE18e3MvzE1hXlILdw7uYx1Y47zsvcXkaAlGJEALw_wcB&gclsrc=aw.ds) for [web](https://compose-web.ui.pages.jetbrains.team/) powered interactive experiment that highlights various ways of producing state with a [Flow](https://kotlinlang.org/docs/flow.html). At the end of it, you should have a mental framework to help choose a state production pipeline that is most beneficial to your use cases.

Code for the examples demonstrated, along with the source of this page and coordinates to a [kotlin multiplatform library](https://kotlinlang.org/docs/multiplatform.html) for the techniques shown can be found [here](https://github.com/tunjid/Mutator).

""".trimIndent()

private val disclaimerCta = """
The following is my personal opinion and not of my employer.    
""".trimIndent()

private val producingStateIntro = """
# Producing state

Producing state is at its core, is nothing more than consolidating sources of changes to state. A generally sound way of doing this is with [unidirectional data flow](https://developer.android.com/topic/architecture/ui-layer#udf) (UDF), therefore all techniques covered on this page are implementations of UDF. Each illustration will also have an UDF visual aid to help convey the "state goes down and events go up" maxim.    

While the tenets of UDF are simple, there's a bit more to implementing it properly especially with `Flows`. Let's start simple. In the following we have a snail along a track. It has a single source of state change; time. Using a `Flow`, we can easily define the state for it.

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

private val snail1Cta = """
The snail's progress is dependent only on time.
""".trimMargin()

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
    ...,
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
        .stateIn(...)
}
""".trimIndent()

private val snail2Cta = """
The snail's state is now dependent on its progress and its speed.    
""".trimIndent()

private val sevenMarkdown = """
In the above, we can see a general pattern emerging. For each source of state change, we can simply add its flow to the `combine` function to produce our state.    
""".trimIndent()