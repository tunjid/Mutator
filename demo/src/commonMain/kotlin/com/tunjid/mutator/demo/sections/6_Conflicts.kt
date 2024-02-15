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
import com.tunjid.mutator.demo.snails.Snail8

@Composable
fun Section6() = SectionLayout {
    Markdown(oneMarkdown)
    CodeBlock(twoCode)
    CallToAction(composeAnimationApiCta)
    Snail8()
    CallToAction(Snail8Cta)
    Markdown(threeMarkdown)
}

private val oneMarkdown = """
# Conflicts in state production

So far, all sources of state change have been relatively harmonious. That is, they don't conflict or compete with each other. Sometimes however, especially with asynchronous data sources, state changes can clash. This most often occurs when user events trigger a set of cascading state changes.

This is best illustrated with an example. Say we wanted to expose our snail to the experience of day and night. Not only that, we want the experience to animate smoothly. We can do this by adding a new method:
""".trimIndent()

private val twoCode = """
class Snail8StateHolder(
    private val scope: CoroutineScope
) {

    private val stateMutator = scope.stateFlowMutator(
        ...
    )    
    ...

    fun setMode(isDark: Boolean) = stateMutator.launch {
        emit { copy(isDark = isDark) }
        /* Collect from a flow that animates color changes */
        interpolateColors(
            startColors = state.value.colors.map(Color::argb).toIntArray(),
            endColors = MutedColors.colors(isDark).map(Color::argb).toIntArray()
        ).collect { (progress, colors) ->
            emit {
                copy(
                    colorInterpolationProgress = progress,
                    colors = colors
                )
            }
        }
    }
}   
""".trimIndent()

private val composeAnimationApiCta = """
In Jetpack Compose apps, animating color changes is best done with the [animateColorAsState](https://developer.android.com/reference/kotlin/androidx/compose/animation/package-summary#animateColorAsState(androidx.compose.ui.graphics.Color,androidx.compose.animation.core.AnimationSpec,kotlin.Function1)) APIs instead of manually as shown in the example above. The example is merely used to demonstrate long running operations that cause state changes, like uploading a file with a progress bar.   
""".trimIndent()

private val Snail8Cta = """
Tap the toggle button to switch between light and dark modes for the snail. Notice that tapping in quick succession will cause the UI to flicker as state changes conflict.  
""".trimIndent()

private val threeMarkdown = """
In the above, each time setMode is called, we first update the state to the new mode, and then crucially, begin to collect from a finite flow that updates the colors available to choose from.

The source of conflict here is the `interpolateColors` `Flow`. If `setMode` is called twice in quick succession, there will be two instances of the `interpolateColors` `Flow` running which may cause the UI to flicker.
""".trimIndent()
