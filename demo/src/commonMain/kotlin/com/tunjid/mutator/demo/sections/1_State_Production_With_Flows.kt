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
import com.tunjid.mutator.demo.snails.Snail3

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
        CallToAction(snail1cta)
        Markdown(snail2Prose)
        CallToAction(snail2UnitOfChangeCta)
        Markdown(snail2Prose2)
        CodeBlock(snail2Code)
        Snail2()
        CallToAction(snail1Cta)
        Markdown(threeMarkdown)
        CodeBlock(fourCode)
        Markdown(fiveMarkdown)
        CodeBlock(sixCode)
        Snail3()
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

Producing state is at its core, is nothing more than consolidating sources of changes to state. A generally sound way of doing this is with [unidirectional data flow](https://developer.android.com/topic/architecture/ui-layer#udf) (UDF), therefore all techniques covered on this page are implementations of UDF. Each illustration will also have an UDF visual aid to help convey the "state goes down and events go up" idiom.    

While the tenets of UDF are simple, there's a bit more to implementing it properly especially with `Flows`. Let's start simple. In the following we have a snail along a track. The snail has the following properties:

* It's progress along the track.
* It's color.

An easy way to represent the state production pipeline for the snail is with a single `MutableStateFlow`: 
""".trimIndent()

private val snail1Code = """
data class Snail1State(
    val progress: Float = 0f,
    val color: Color = MutedColors.colors(false).first(),
    val colors: List<Color> = MutedColors.colors(false)
)

class Snail1StateHolder {

    private val _state = MutableStateFlow(Snail1State())
    val state: StateFlow<Snail1State> = _state.asStateFlow()

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

private val snail1cta = """
Interact with the  snail by dragging it around and changing its color. 
""".trimIndent()

private val snail2Prose = """
In the above, the [`update`](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/update.html) function takes a function with the signature `(State) -> State` as its only argument. This the fundamental unit of state change, a function that takes the current state and changes it to the next state.
""".trimIndent()

private val snail2UnitOfChangeCta = """
The fundamental unit of state change is  a function literal with the signature `(State) -> State`.
""".trimIndent()

private val snail2Prose2 = """ 
The above works provided the only sources of state change are from user actions that call synchronous or `suspend` functions. Things get  a little more interesting when sources of state change include:
* `Flows`.
* `Flows` and `suspend` functions.

Let's start with just `Flows`. Consider the snail again, except this time, it has a single source of state change; time. As time passes, the snail slowly progresses on  its track. Using a `Flow`, we can easily define the state for it.
    
""".trimIndent()

private val snail2Code = """
class Snail2StateHolder(
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
data class Snail3State(
    val progress: Float = 0f,
    val speed: Speed = Speed.One,
)

class Snail3StateHolder(
    scope: CoroutineScope
) {
    private val speed: Flow<Speed> = …

    private val progress: Flow<Float> = …

    val state: StateFlow<Snail3State> = combine(
        progress,
        speed,
        ::Snail3State
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