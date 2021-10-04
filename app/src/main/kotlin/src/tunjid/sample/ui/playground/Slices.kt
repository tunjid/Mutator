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

package src.tunjid.sample.ui.playground

import android.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import com.tunjid.mutator.Mutation
import src.tunjid.sample.globalui.InsetFlags
import src.tunjid.sample.globalui.UiState

data class Slice<T : Any>(
    val name: String,
    val options: List<T>,
    val nameTransformer: (T) -> String = Any?::toString,
    val selectedText: String,
    val setter: (T) -> Mutation<UiState>
) {
    val select: (Int) -> Mutation<UiState> = { index -> setter(options[index]) }
    val optionName: (Int) -> String = { index -> nameTransformer(options[index]) }
}

fun UiState.slices() = listOf(
    Slice(
        name = "Status bar color",
        nameTransformer = Int::stringHex,
        options = listOf(
            Color.TRANSPARENT,
            Color.parseColor("#80000000"),
            Color.BLACK,
            Color.WHITE,
            Color.RED,
            Color.GREEN,
            Color.BLUE
        ),
        selectedText = statusBarColor.stringHex,
    ) {
        Mutation { copy(statusBarColor = it) }
    },
    Slice(
        name = "Is immersive",
        options = listOf(true, false),
        selectedText = isImmersive.toString(),
    ) {
        Mutation { copy(isImmersive = it) }
    },
    Slice(
        name = "Has light status bar icons",
        options = listOf(true, false),
        selectedText = lightStatusBar.toString(),
    ) {
        Mutation { copy(lightStatusBar = it) }
    },
    Slice(
        name = "Toolbar title",
        options = listOf(
            "Ui State Playground",
            "Reality can be whatever I want",
            "I am inevitable",
        ),
        selectedText = toolbarTitle.toString(),
    ) {
        Mutation { copy(toolbarTitle = it) }
    },
    Slice(
        name = "Tool bar shows",
        options = listOf(true, false),
        selectedText = toolbarShows.toString(),
    ) {
        Mutation { copy(toolbarShows = it) }
    },
    Slice(
        name = "Tool bar overlaps",
        options = listOf(true, false),
        selectedText = toolbarOverlaps.toString(),
    ) {
        Mutation { copy(toolbarOverlaps = it) }
    },
    Slice(
        name = "FAB shows",
        options = listOf(true, false),
        selectedText = fabShows.toString(),
    ) {
        Mutation { copy(fabShows = it) }
    },
    Slice(
        name = "FAB icon",
        nameTransformer = { "Icon" },
        options = listOf(
            Icons.Default.Done,
        ),
        selectedText = "Umm",
    ) {
        Mutation { copy(fabIcon = it) }
    },
    Slice(
        name = "FAB text",
        options = listOf("Hello", "Hi", "How do you do"),
        selectedText = fabText.toString(),
    ) {
        Mutation { copy(fabText = it) }
    },
    Slice(
        name = "FAB extended",
        options = listOf(true, false),
        selectedText = fabExtended.toString(),
    ) {
        Mutation { copy(fabExtended = it) }
    },
    Slice(
        name = "Bottom nav shows",
        options = listOf(true, false),
        selectedText = showsBottomNav.toString(),
    ) {
        Mutation { copy(showsBottomNav = it) }
    },
    Slice(
        name = "Nav bar color",
        nameTransformer = Int::stringHex,
        options = listOf(
            Color.TRANSPARENT,
            Color.parseColor("#80000000"),
            Color.BLACK,
            Color.WHITE,
            Color.RED,
            Color.GREEN,
            Color.BLUE
        ),
        selectedText = navBarColor.toString(),
    ) {
        Mutation { copy(navBarColor = it) }
    },
    Slice(
        name = "Inset Flags",
        options = listOf(
            InsetFlags.ALL,
            InsetFlags.NO_TOP,
            InsetFlags.NO_BOTTOM,
            InsetFlags.NONE
        ),
        selectedText = insetFlags.toString(),
    ) {
        Mutation { copy(insetFlags = it) }
    }
)

private val Int.stringHex: String get() = "⦿" + "#${Integer.toHexString(this)}"
