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
import com.tunjid.mutator.demo.snails.Snail11
import com.tunjid.mutator.demo.snails.Snail9
import com.tunjid.mutator.demo.snails.Snail10

@Composable
fun Section8() = SectionLayout {
    Markdown(oneMarkdown)
    CodeBlock(twoCode)
    Snail9()
    CallToAction(Snail9Cta)
    Markdown(threeMarkdown)
    CodeBlock(fourCode)
    Snail10()
    CallToAction(Snail10Cta)
    Markdown(fiveMarkdown)
    CodeBlock(sixCode)
    Markdown(sevenMarkdown)
    CodeBlock(eightCode)
    Snail11()
    CallToAction(Snail11Cta)
    Markdown(nineMarkdown)
}

private val oneMarkdown = """
# Conflict resolution in state production

There are two ways of dealing with the issue above:


## Lock the state

We can add a new boolean field to the state called `isInterpolating`. If `setMode` is called when `interpolateColors` is running, we return immediately.
""".trimIndent()

private val twoCode = """
data class Snail9State(
    ...
    val isInterpolating: Boolean = false,
)
    
class Snail9StateHolder(
    private val scope: CoroutineScope
) {

    …

    fun setMode(isDark: Boolean) {
        if (state.value.isInterpolating) return
        scope.launch {
            userChanges.emit { copy(isDark = isDark, isInterpolating = true) }
            interpolateColors(
                ...
            ).collect { (progress, colors) ->
                userChanges.emit { 
                    copy(...) 
                }
            }
            userChanges.emit { copy(isInterpolating = false) }
        }
    }
}
""".trimIndent()

private val Snail9Cta = """
Tap the toggle button many times again. Notice that it ignores the toggle event while the animation is running.    
""".trimIndent()

private val threeMarkdown = """
This works, but it has the unfortunate side effect of making the user wait until we're done interpolating before they can change the mode again.

## Eliminate the source of conflict

Eliminating the source of conflict in the example above would require being able to stop collecting from `interpolateColors` each time `setMode` is invoked. There are two ways of doing this:

### Cancel the job

In the above `scope.launch()` returns a `Job` for the suspending function that collects from `interpolateColors`. Keeping a reference to this `Job` allows for canceling the `Flow` when next the `setMode` method is invoked.

""".trimIndent()

private val fourCode = """
class Snail10StateHolder(
    private val scope: CoroutineScope
) {

   private var setModeJob: Job? = null
    …

    fun setMode(isDark: Boolean) {
        setModeJob?.cancel()
        setModeJob = scope.launch {
            userChanges.emit { copy(isDark = isDark) }
            interpolateColors(
                ...
            ).collect { (progress, colors) ->
                userChanges.emit { 
                    copy(...) 
                }
            }
        }
    }
}
""".trimIndent()

private val Snail10Cta = """
Tap the toggle button many times. Notice that with each tap, the colors reverse their changes.   
""".trimIndent()

private val fiveMarkdown = """
This works, although it has the caveat of something we've seen before; it scales linearly. Each method invocation that could have potential conflicts in state by virtue of it causing multiple changes in state will need a `Job` reference to allow for cancelling collection.


### Model changes with flows

In this approach, the user event of setting the mode is modeled as a `Flow`, and by using the `flatMapLatest` `Flow` operator, the `interpolateColors` `Flow` is automatically canceled each time the `SetMode` `Flow` emits.
""".trimIndent()

private val sixCode = """
   data class SetMode(
        val isDark: Boolean,
        val startColors: List<Color>
    )

    private fun Flow<SetMode>.mutations(): Flow<Mutation<Snail11State>> =
        flatMapLatest { (isDark, startColors) ->
            flow {
                emit(mutation { copy(isDark = isDark) })
                emitAll(
                    interpolateColors(
                        startColors = startColors.map(Color::toArgb).toIntArray(),
                        endColors = MutedColors.colors(isDark)
                    )
                        .map { mutation { copy(colors = it) }  }
                )
            }
        }
""".trimIndent()

private val sevenMarkdown = """
In the above, `SetMode` is a summary of the user event. It encapsulates everything needed to process the `Mutation`. The use of `flatMapLatest` cancels any ongoing state changes as a result of previous events and guarantees there are no conflicts.

Scaling the above to cover state production for our snail is non trivial, and often requires a library. In the case of the snail example, applying the concept above yields:    
""".trimIndent()

private val eightCode = """
class Snail11StateHolder(
    scope: CoroutineScope
) {

    private val speedChanges: Flow<Mutation<Snail11State>> = …

    private val progressChanges: Flow<Mutation<Snail11State>> = …

    private val mutator = scope.stateFlowMutator<Action, Snail11State>(
        initialState = Snail11State(),
        started = SharingStarted.WhileSubscribed(),
        mutationFlows = listOf(
            speedChanges,
            progressChanges
        ),
        actionTransform = { actions ->
            actions.toMutationStream {
                when (val type = type()) {
                    is Action.SetColor -> type.flow.colorMutations()
                    is Action.SetMode -> type.flow.modeMutations()
                    is Action.SetProgress -> type.flow.progressMutations()
                }
            }
        }
    )

    val state: StateFlow<Snail11State> = mutator.state

    val actions: (Action) -> Unit = mutator.accept

    private fun Flow<Action.SetColor>.colorMutations(): Flow<Mutation<Snail11State>> =
        mapLatest {
            mutation { copy(colorIndex = it.index) }
        }

    private fun Flow<Action.SetProgress>.progressMutations(): Flow<Mutation<Snail11State>> =
        mapLatest {
            mutation { copy(progress = it.progress) }
        }

    private fun Flow<Action.SetMode>.modeMutations(): Flow<Mutation<Snail11State>> =
        flatMapLatest { (isDark, startColors) ->
            flow {
                emit(mutation { copy(isDark = isDark) })
                emitAll(
                    interpolateColors(
                        startColors = startColors.map(Color::argb).toIntArray(),
                        endColors = MutedColors.colors(isDark).map(Color::argb).toIntArray()
                    )
                        .map { (progress, colors) ->
                            mutation {
                                copy(
                                    colorInterpolationProgress = progress,
                                    colors = colors
                                )
                            }
                        }
                )
            }
        }  
""".trimIndent()

private val Snail11Cta = """
Snail11 is identical to Snail10; just with different state production semantics.    
""".trimIndent()

private val nineMarkdown = """
    
In the above, all there are two sources of state `Mutation`s:

* Data sources in the `mutationFlows` argument; defined as `Flow<Mutation<State>>`
* User events in the `actionTransform` argument; defined as `(Flow<Action>) -> Flow<Mutation<State>>`
   
Crucially the `actionTransform` takes a `Flow` of all `Action` instances, splits them out into individual `Flow`s for each `Action`, and finally applies `Flow` transformations to each `Action` `Flow` to turn them into `Flow<Mutation<State>>`:

* `colorMutations` and `progressMutations` both use `mapLatest` to guarantee no conflicts because `mapLatest` automatically cancels any suspending function invoked in its lambda.
* `modeMutations` does the same as the above but uses `flatMapLatest` and the `flow { }` builder because it collects from a `Flow` instead of just invoking a `suspend` function.

# Choosing a state production pipeline

The above highlights a common motif in this document; the state production pipeline is only as complicated as the kind of state produced, and the state changes that can occur. Simple states require simple pipelines, and complicated states often require higher level abstractions that enforce guarantees in state production. These guarantees often come with a complexity cost.
   
Depending on the particulars of your state production pipeline, a rule of thumb that can be applied is:

### Simple state production pipelines
For small and simple states, `combine` sources that contribute to your state. No library is required.

### Intermediate state production pipelines
For intermediate states that have lots of different kinds of user events, `merge` changes to state so you do not have to create multiple `MutableStateFlow` instances to manage each user event. You may opt to use a library, or roll out a small custom implementation of the techniques described in the *merge* section above.

 ### Large and complex state production pipelines 
 For state production pipelines that:
 * Have multiple contributors to the same state property that may conflict
 * Have user events that can cause multiple mutations of state to occur
 * Have state changes that involve collecting from other `Flow`s
 
 Model your state production pipeline as a `Flow` to allow for tighter control of sources of change, and use a library that offers ergonomic APIs for your transformations.
""".trimIndent()
