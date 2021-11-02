package com.tunjid.sample.ui.frame

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.tunjid.sample.globalui.UiState
import com.tunjid.sample.globalui.bottomNavPositionalState
import com.tunjid.sample.globalui.fragmentContainerState
import com.tunjid.sample.globalui.toolbarState
import com.tunjid.sample.ui.AppDependencies
import com.tunjid.sample.ui.mapState

@Composable
fun Root() {
    val rootScope = rememberCoroutineScope()
    val uiStateFlow = AppDependencies.current.globalUiStateHolder.state
    val navStateFlow = AppDependencies.current.navStateHolder.state

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AppToolbar(
            stateFlow = uiStateFlow.mapState(
                scope = rootScope,
                mapper = UiState::toolbarState
            )
        )

        AppRouteContainer(
            stateFlow = uiStateFlow.mapState(
                scope = rootScope,
                mapper = UiState::fragmentContainerState
            ),
            content = {
                AppNavRouter(
                    navStateFlow = navStateFlow
                )
            }
        )
        AppBottomNav(
            stateFlow = uiStateFlow.mapState(
                scope = rootScope,
                mapper = UiState::bottomNavPositionalState
            )
        )
    }
}