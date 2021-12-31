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

package com.tunjid.sample.ui.frame

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.tunjid.sample.globalui.UiState
import com.tunjid.sample.globalui.bottomNavPositionalState
import com.tunjid.sample.globalui.fragmentContainerState
import com.tunjid.sample.globalui.toolbarState
import com.tunjid.sample.AppDependencies
import com.tunjid.sample.AppDeps
import com.tunjid.sample.ui.mapState

@Composable
fun Root(appDeps: AppDeps) {
    CompositionLocalProvider(AppDependencies provides appDeps) {
        val rootScope = rememberCoroutineScope()
        val uiStateFlow = AppDependencies.current.globalUiMutator.state
        val navStateFlow = AppDependencies.current.navMutator.state

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
}
