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
import com.tunjid.mutator.demo.editor.EditorView
import com.tunjid.mutator.demo.editor.Markdown
import com.tunjid.mutator.demo.editor.SectionLayout
import com.tunjid.mutator.demo.snails.Snail7

@Composable
fun Section6() = SectionLayout {
    Markdown(oneMarkdown)
    EditorView(twoCode)
    Snail7()
    CallToAction(
        "Tap the toggle button to switch between light and dark modes for the snail. " +
            "Notice that tapping in quick succession will cause the UI to flicker as state changes conflict."
    )
    Markdown(threeMarkdown)
}

private val oneMarkdown = """
# Conflicts in state production

So far, all sources of state change have been relatively harmonious. That is, they don't conflict or compete with each other. Sometimes however, especially with asynchronous data sources, state changes can clash. This most often occurs when user events trigger a set of cascading state changes.

This is best illustrated with an example. Say we wanted to expose our snail to the experience of day and night. Not only that, we want the experience to animate smoothly. We can do this by adding a new method:
""".trimIndent()

private val twoCode = """
class Snail7StateHolder(
    private val scope: CoroutineScope
) {

    …

    fun setMode(isDark: Boolean) {
        scope.launch {
            userChanges.emit { copy(isDark = isDark) }
            // Collect from a flow that animates color changes
            interpolateColors(
                startColors = state.value.colors.map(Color::toArgb).toIntArray(),
                endColors = MutedColors.colors(isDark)
            ).collect { (progress, colors) ->
                userChanges.emit { copy(colorInterpolationProgress = progress, colors = colors) }
            }
        }
    }
}   
""".trimIndent()

private val threeMarkdown = """
In the above, each time setMode is called, we first update the state to the new mode, and then crucially, begin to collect from a finite flow that updates the colors available to choose from.

The source of conflict here is the `interpolateColors` `Flow`. If `setMode` is called twice in quick succession, there will be two instances of the `interpolateColors` `Flow` running which may cause the UI to flicker.
""".trimIndent()
