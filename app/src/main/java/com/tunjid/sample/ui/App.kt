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

package com.tunjid.sample.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.tunjid.mutator.Mutation
import com.tunjid.mutator.Mutator
import com.tunjid.mutator.stateFlowMutator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.tunjid.sample.globalui.UiState
import com.tunjid.sample.nav.MultiStackNav
import com.tunjid.sample.nav.StackNav
import com.tunjid.sample.ui.playground.PlaygroundRoute
import kotlinx.coroutines.flow.StateFlow

class App {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val navMutator: Mutator<Mutation<MultiStackNav>, StateFlow<MultiStackNav>> = stateFlowMutator(
        scope = scope,
        initialState = MultiStackNav(
            currentIndex = 0,
            stacks = listOf(
                StackNav(
                    name = "First",
                    routes = listOf(PlaygroundRoute)
                )
            )
        ),
        transform = { it }
    )
    val globalUiMutator: Mutator<Mutation<UiState>, StateFlow<UiState>> = stateFlowMutator(
        scope = scope,
        initialState = UiState(),
        transform = { it }
    )
}

val AppDependencies = staticCompositionLocalOf { App() }
