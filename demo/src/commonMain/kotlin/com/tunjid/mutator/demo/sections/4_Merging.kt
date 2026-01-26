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
import com.tunjid.mutator.demo.snails.Snail5
import com.tunjid.mutator.demo.snails.Snail6

@Composable
fun Section4() = SectionLayout {
    Markdown(oneMarkdown)
    CallToAction(twoCta, centerText = true)
    Markdown(threeMarkdown)
    CodeBlock(fourCode)
    CallToAction(fourCta)
    Markdown(fiveMarkdown)
    CallToAction(inlineMutationCta)
    CodeBlock(sixCode)
    CallToAction(userActionsCta)
    Snail5()
    CallToAction(snail4To3Cta)
    Markdown(sevenMarkdown)
    CallToAction(orderCta)
    Markdown(advantages)
    CodeBlock(eightCode)
    Snail6()
    CallToAction(snail5Cta)
    Markdown(nineMarkdown)
    Markdown(tenMarkdown)
    CallToAction(tenCta)
}

private val oneMarkdown = """
# Merging changes in state

We've established that state production fundamentally is introducing changes to state over time. That is the new state is the old state plus the change in state. Expressing this mathematically becomes:    
""".trimIndent()

private val twoCta = """
newState = oldState + Δstate    
""".trimIndent()

private val threeMarkdown = """
Where Δstate is the change in state, and is how `MutableStateFlow.update()` works. We can make the Δstate more expressive and easier to read by representing it in code as a type alias:   
""".trimIndent()

private val fourCode = """
typealias Mutation<State> = State.() -> State
""".trimIndent()

private val fourCta = """
The signature of the declaration `State.() -> State` is identical to the fundamental unit of state change declared earlier `(State) -> State` with the `MutableStateFlow` `update` method.
""".trimIndent()

private val inlineMutationCta = """
To help with readability, creating a `Mutation` inside a `Flow` transformation lambda is done with an inline function: `aFlow.map { mutation { copy(...) } }`
""".trimIndent()

private val fiveMarkdown = """
The declaration above now defines the unit of state change (Δstate) for any state `T` as a `Mutation`; a lambda with `T` as the receiver that when invoked, returns `T`. The above is extremely powerful as we can represent any state change for any state declaration with a single type.

To produce state then, we simply have to start with an initial state, and incrementally apply state `Mutation`s to it over time to create our state production pipeline. This is sometimes called reducing changes into state.

Expressing this in code with `Flows` is very concise and requires just two operators:


* [`merge`](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/merge.html): Used to merge all sources of `Mutation<State>` (Δstate) into a single stream
* [`scan`](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/scan.html): Used to reduce the stream of `Mutation<State>` into the state being produced.

In our snail example, we can express the same state production pipeline with user actions as:    
""".trimIndent()

private val sixCode = """
data class Snail5State(
    ...,
)

class Snail5StateHolder(
    private val scope: CoroutineScope
) {

    private val speed: Flow<Speed> = …

    private val speedChanges: Flow<Mutation<Snail5State>> = speed
        .map { mutation { copy(speed = it) } }

    private val progressChanges: Flow<Mutation<Snail5State>> = intervalFlow
        .map { mutation { copy(progress = (progress + 1) % 100) } }

    private val changeEvents = MutableSharedFlow<Mutation<Snail5State>>()

    val state: StateFlow<Snail5State> = merge(
        progressChanges,
        speedChanges,
        changeEvents,
    )
        .scan(Snail5State()) { startState, mutation -> mutation(startState) }
        .stateIn(...)

    fun setSnailColor(index: Int) {
        scope.launch {
            changeEvents.emit { copy(color = colors[index]) }
        }
    }
}    
""".trimIndent()

private val userActionsCta = """
Notice that user actions are now propagated with a [MutableSharedFlow](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-mutable-shared-flow/) instead of a [MutableStateFlow](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-mutable-state-flow/). This is because `StateFlow` conflates emissions which can make it prioritize only the latest events when shared between methods invoked by user events.   
""".trimIndent()

private val snail4To3Cta = """
Snail5 is identical to Snail4; just with different state production semantics.    
""".trimIndent()

private val sevenMarkdown = """
## Why `merge`?
    
This example has all the functionality the `combine` approach did, but with a slight complexity cost.

It however brings the following advantages:

* `merge` is an n-ary function; it has no arity limits; you can merge as many `Flow`s as you want.
* New state can be a product of old state; a `Mutation` is an accumulating/reducing function.
* All user actions that change state can share the same source `Flow`: `changeEvents` a [`MutableSharedFlow`](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-mutable-shared-flow/).
* All source flows can independently contribute their state mutations
* All source flows at the time of introducing the state mutation can read the existing state.
* State changes can come from `Flows` or `suspend` functions easily.

## Why switch to `MutableSharedFlow` from `MutableStateFlow` for user actions?

The switch to `MutableSharedFlow` from `MutableStateFlow` for propagating user actions is because `StateFlow` conflates emissions. If two separate methods attempted to use the same `MutableStateFlow` to emit a `Mutation` of state, the `StateFlow` may only emit the latest `Mutation`. That is `MutableStateFlow` does not guarantee that every update to its `value` property is seen by its collectors.

`MutableSharedFlow` on the other hand has an `emit` method which suspends until the `Mutation` is delivered. This means that multiple coroutines can be launched across several method invocations and call `emit` on the same `MutableShared` `Flow` and none of them will cancel out the other. The order in which they are applied also doesn't matter as `Mutation` instances just describe changes to state; they are designed to be independent.
   
""".trimIndent()

private val orderCta = """
If the emission order of `Mutation` instances across multiple user events matter to you, keep reading until the *Conflicts in state production* and *Conflict resolution in state production* sections.   
""".trimIndent()

private val advantages = """
These advantages can be easily illustrated with an example. If the user wanted to manually move the snail to a certain position, it would simply be:     
""".trimIndent()

private val eightCode = """
class Snail6StateHolder(
    private val scope: CoroutineScope
) {

    private val progressChanges: Flow<Mutation<Snail6State>> = …
    …

    private val changeEvents = MutableSharedFlow<Mutation<Snail6State>>()

    fun setSnailColor(index: Int) {
        scope.launch {
            changeEvents.emit { copy(color = colors[index]) }
        }
    }
    
   fun setProgress(progress: Float) {
        scope.launch {
            changeEvents.emit { copy(progress = progress) }
        }
    }
}    
""".trimIndent()

private val snail5Cta = """
Drag the snail to place it anywhere on its track. Also, try to hold it in place and see what happens.    
""".trimIndent()

private val nineMarkdown = """
That is, we can simply introduce a state change to the `progress` property of the state despite the `progessChanges` flow also contributing to a change of the same property. This is something that would be rather difficult with the `combine` approach. This is because the combine approach only lets you set state properties of state to create new state. The `merge` approach instead lets you `mutate` or change properties of state and apply them to the existing state.  
  
Furthermore both `setSnailColor` and `setProgress` contribute their changes to state using the same  `MutableSharedFlow`: `changeEvents`. This approach scales well because no matter how many methods are added that change the `State` from user events, they don't need any more variables to be declared in the state holder class.
""".trimIndent()

private val tenMarkdown = """
## Mutations are flexible updates to state

The method of state production described above may seem unfamiliar at first, however it simply builds on the pattern established in `Snail1` with a single `MutableStateFlow`. It expands the pattern to work not just for state changes from synchronous and `suspend` methods, but for state changes from `Flow` instances as well. 
""".trimIndent()

private val tenCta = """
Merging `Mutation` instances can be thought of an extension to `MutableStateFlow.update()`, where updates can come from both `Flows` and `suspend` functions.
""".trimIndent()
