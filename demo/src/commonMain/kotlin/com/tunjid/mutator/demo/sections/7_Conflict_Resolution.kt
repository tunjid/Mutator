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
import com.tunjid.mutator.demo.snails.Snail10
import com.tunjid.mutator.demo.snails.Snail4
import com.tunjid.mutator.demo.snails.Snail5
import com.tunjid.mutator.demo.snails.Snail6
import com.tunjid.mutator.demo.snails.Snail7
import com.tunjid.mutator.demo.snails.Snail8
import com.tunjid.mutator.demo.snails.Snail9

@Composable
fun Section7() = SectionLayout {
    Markdown(oneMarkdown)
    EditorView(twoCode)
    Snail8()
    Markdown(threeMarkdown)
    EditorView(fourCode)
    Snail9()
    Markdown(fiveMarkdown)
    EditorView(sixCode)
    Markdown(sevenMarkdown)
    EditorView(eightCode)
    Snail10()
    Markdown(threeMarkdown)
    EditorView(fourCode)
    Markdown(nineMarkdown)
}

private val oneMarkdown = """
# Dealing with conflicts in state production

There are two ways of dealing with the issue above:


## Lock the state

We can add a new boolean field to the state called `isInterpolating`. If `setMode` is called when `interpolateColors` is running, we return immediately.
""".trimIndent()

private val twoCode = """
class Snail8StateHolder(
    private val scope: CoroutineScope
) {

    …

   fun setMode(isDark: Boolean) {
        if(state.value.isInterpolating) return
        scope.launch {
            userChanges.emit { copy(isDark = isDark, isInterpolating = true) }
            interpolateColors(
                startColors = state.value.colors.map(Color::toArgb).toIntArray(),
                endColors = MutedColors.colors(isDark)
            ).collect {
                userChanges.emit { copy(colors = it) }
            }
            userChanges.emit { copy(isInterpolating = false) }
        }
    }

}
""".trimIndent()

private val threeMarkdown = """
This works, but it has the unfortunate side effect of making the user wait until we're done interpolating before they can change the mode again.


## Eliminate the source of conflict

Eliminating the source of conflict in the example above would require being able to stop collecting from `interpolateColors` each time `setMode` is invoked. There are two ways of doing this: \



### Cancel the job

In the above `scope.launch` returns a `Job` for the suspending function that collects from `interpolateColors`. Keeping a reference to this `Job` allows for canceling `Flow` when next the `setMode` method is invoked.

""".trimIndent()

private val fourCode = """
class Snail9StateHolder(
    private val scope: CoroutineScope
) {

   private var setModeJob: Job? = null
    …

   fun setMode(isDark: Boolean) {
        setModeJob?.cancel()
        setModeJob = scope.launch {
            userChanges.emit { copy(isDark = isDark, isInterpolating = true) }
            interpolateColors(
                startColors = state.value.colors.map(Color::toArgb).toIntArray(),
                endColors = MutedColors.colors(isDark)
            ).collect {
                userChanges.emit { copy(colors = it) }
            }
            userChanges.emit { copy(isInterpolating = false) }
        }
    }

}
""".trimIndent()

private val fiveMarkdown = """
This works, although it has the caveat of something we've seen before; it scales linearly. Each method invocation that could have potential conflicts in state by virtue of it causing multiple changes in state will need a `Job` reference.


### Model changes with flows

In this approach, the user event of setting the mode is modeled as a `Flow`, and using `Flow` operators the `interpolateColors` `Flow` is automatically canceled each time the `SetMode` event is triggered.
""".trimIndent()

private val sixCode = """
   data class SetMode(
        val isDark: Boolean,
        val startColors: List<Color>
    )

    fun Flow<SetMode>.mutations(): Flow<Mutation<Snail10State>> =
        flatMapLatest { (isDark, startColors) ->
            flow {
                emit(Mutation { copy(isDark = isDark) })
                emitAll(
                    interpolateColors(
                        startColors = startColors.map(Color::toArgb).toIntArray(),
                        endColors = MutedColors.colors(isDark)
                    )
                        .map { Mutation{ copy(colors = it) }  }
                )
            }
        }
""".trimIndent()

private val sevenMarkdown = """
In the above, `SetMode` is a summary of the user event. It encapsulates everything needed to process the `Mutation`. The use of `flatMapLatest` cancels any ongoing state changes as a result of previous events and guarantees there are no conflicts.

Scaling the above to cover state production for our snail is non trivial, and often requires a library. In the case of the snail example, applying the concept above yields:    
""".trimIndent()

private val eightCode = """
class Snail10StateHolder(
    scope: CoroutineScope
) {

    private val speedChanges: Flow<Mutation<Snail10State>> = …

    private val progressChanges: Flow<Mutation<Snail10State>> = …


    private val mutator = stateFlowMutator<Action, Snail10State>(
        scope = scope,
        initialState = Snail10State(),
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

    val state: StateFlow<Snail10State> = mutator.state

    val actions: (Action) -> Unit = mutator.accept

    private fun Flow<Action.SetColor>.colorMutations() = …

    private fun Flow<Action.SetProgress>.progressMutations() = …

    private fun Flow<Action.SetMode>.modeMutations(): Flow<Mutation<Snail10State>> =...    
""".trimIndent()

private val nineMarkdown = """
# Choosing a state production pipeline

The above highlights a common motif in this document; the state production pipeline is only as complicated as the kind of state changes that can occur. Simple states require simple pipelines, and complicated states often require abstractions that bring convenience at the cost of complexity.    
""".trimIndent()