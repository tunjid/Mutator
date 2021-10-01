/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package src.tunjid.sample.globalui

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.graphics.Insets


data class ToolbarItem(
    val id: Int,
    val text: String,
    val imageVector: ImageVector? = null,
    val contentDescription: String? = null,
)

data class UiState(
    val toolbarItems: List<ToolbarItem> = listOf(),
    val toolbarShows: Boolean = false,
    val toolbarOverlaps: Boolean = false,
    val toolbarTitle: CharSequence = "",
    val fabIcon: ImageVector = Icons.Default.Done,
    val fabShows: Boolean = false,
    val fabExtended: Boolean = true,
    val fabText: CharSequence = "",
    @param:ColorInt
    @field:ColorInt
    @get:ColorInt
    val backgroundColor: Int = Color.TRANSPARENT,
    val snackbarText: CharSequence = "",
    @param:ColorInt
    @field:ColorInt
    @get:ColorInt
    val navBarColor: Int = Color.BLACK,
    val lightStatusBar: Boolean = false,
    val showsBottomNav: Boolean? = null,
    val statusBarColor: Int = Color.TRANSPARENT,
    val insetFlags: InsetDescriptor = InsetFlags.ALL,
    val isImmersive: Boolean = false,
    val systemUI: SystemUI = NoOpSystemUI,
    val fabClickListener: (Unit) -> Unit = emptyCallback(),
    val toolbarMenuClickListener: (ToolbarItem) -> Unit = emptyCallback(),
    val altToolbarMenuClickListener: (ToolbarItem) -> Unit = emptyCallback(),
)

private fun <T> emptyCallback(): (T) -> Unit = {}

// Internal state slices for memoizing animations.
// They aggregate the parts of Global UI they react to

internal data class ToolbarState(
    val statusBarSize: Int,
    val visible: Boolean,
    val overlaps: Boolean,
    val toolbarTitle: CharSequence,
    val items: List<ToolbarItem>,
)

internal data class SnackbarPositionalState(
    val bottomNavVisible: Boolean,
    override val ime: Insets,
    override val navBarSize: Int,
    override val insetDescriptor: InsetDescriptor
) : KeyboardAware

internal data class FabPositionalState(
    val fabVisible: Boolean,
    val bottomNavVisible: Boolean,
    val snackbarHeight: Int,
    override val ime: Insets,
    override val navBarSize: Int,
    override val insetDescriptor: InsetDescriptor
) : KeyboardAware

internal data class FragmentContainerPositionalState(
    val statusBarSize: Int,
    val toolbarOverlaps: Boolean,
    val bottomNavVisible: Boolean,
    override val ime: Insets,
    override val navBarSize: Int,
    override val insetDescriptor: InsetDescriptor
) : KeyboardAware

internal data class BottomNavPositionalState(
    val insetDescriptor: InsetDescriptor,
    val bottomNavVisible: Boolean,
    val navBarSize: Int
)


internal val UiState.toolbarState
    get() = ToolbarState(
        items = toolbarItems,
        toolbarTitle = toolbarTitle,
        visible = toolbarShows,
        overlaps = toolbarOverlaps,
        statusBarSize = systemUI.static.statusBarSize,
    )

internal val UiState.fabState
    get() = FabPositionalState(
        fabVisible = fabShows,
        snackbarHeight = systemUI.dynamic.snackbarHeight,
        bottomNavVisible = showsBottomNav == true,
        ime = systemUI.dynamic.ime,
        navBarSize = systemUI.static.navBarSize,
        insetDescriptor = insetFlags
    )

internal val UiState.snackbarPositionalState
    get() = SnackbarPositionalState(
        bottomNavVisible = showsBottomNav == true,
        ime = systemUI.dynamic.ime,
        navBarSize = systemUI.static.navBarSize,
        insetDescriptor = insetFlags
    )

internal val UiState.fabGlyphs
    get() = fabIcon to fabText

internal val UiState.toolbarPosition
    get() = systemUI.static.statusBarSize

internal val UiState.bottomNavPositionalState
    get() = BottomNavPositionalState(
        bottomNavVisible = showsBottomNav == true,
        navBarSize = systemUI.static.navBarSize,
        insetDescriptor = insetFlags
    )

internal val UiState.fragmentContainerState
    get() = FragmentContainerPositionalState(
        statusBarSize = systemUI.static.statusBarSize,
        insetDescriptor = insetFlags,
        toolbarOverlaps = toolbarOverlaps,
        bottomNavVisible = showsBottomNav == true,
        ime = systemUI.dynamic.ime,
        navBarSize = systemUI.static.navBarSize
    )

/**
 * Interface for [UiState] state slices that are aware of the keyboard. Useful for
 * keyboard visibility changes for bottom aligned views like Floating Action Buttons and Snack Bars
 */
interface KeyboardAware {
    val ime: Insets
    val navBarSize: Int
    val insetDescriptor: InsetDescriptor
}

internal val KeyboardAware.keyboardSize get() = ime.bottom - navBarSize