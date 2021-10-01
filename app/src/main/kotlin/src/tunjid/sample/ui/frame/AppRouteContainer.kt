package src.tunjid.sample.ui.frame

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.max
import kotlinx.coroutines.flow.StateFlow
import src.tunjid.sample.globalui.FragmentContainerPositionalState
import src.tunjid.sample.globalui.keyboardSize
import src.tunjid.sample.ui.countIf
import src.tunjid.sample.ui.uiSizes

@Composable
internal fun AppRouteContainer(
    stateFlow: StateFlow<FragmentContainerPositionalState>,
    content: @Composable BoxScope.() -> Unit
) {
    val state by stateFlow.collectAsState()

    val bottomNavHeight = uiSizes.bottomNavSize countIf state.bottomNavVisible
    val insetClearance = max(
        a = bottomNavHeight,
        b = with(LocalDensity.current) { state.keyboardSize.toDp() }
    )
    val navBarClearance = with(LocalDensity.current) {
        state.navBarSize.toDp()
    } countIf state.insetDescriptor.hasBottomInset
    val totalBottomClearance = insetClearance + navBarClearance

    val statusBarSize = with(LocalDensity.current) {
        state.statusBarSize.toDp()
    } countIf state.insetDescriptor.hasTopInset
    val toolbarHeight = uiSizes.toolbarSize countIf !state.toolbarOverlaps
    val topClearance = statusBarSize + toolbarHeight

    Box(
        modifier = Modifier.padding(
            top = topClearance,
            bottom = totalBottomClearance
        ),
        content = content
    )
}