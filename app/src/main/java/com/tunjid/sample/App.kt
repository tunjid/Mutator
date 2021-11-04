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

package com.tunjid.sample

import android.Manifest
import android.app.Application
import androidx.compose.runtime.staticCompositionLocalOf
import com.tunjid.mutator.Mutation
import com.tunjid.mutator.Mutator
import com.tunjid.mutator.stateFlowMutator
import com.tunjid.sample.activityflow.permissionsMutator
import com.tunjid.sample.globalui.UiState
import com.tunjid.sample.nav.MultiStackNav
import com.tunjid.sample.nav.StackNav
import com.tunjid.sample.ui.permissions.PermissionsRoute
import com.tunjid.sample.ui.playground.PlaygroundRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow

class App : Application() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val appDeps by lazy { createAppDependencies() }

    override fun onCreate() {
        super.onCreate()
        appDeps
    }

    private fun createAppDependencies() = object : AppDeps {
        override val navMutator: Mutator<Mutation<MultiStackNav>, StateFlow<MultiStackNav>> =
            stateFlowMutator(
                scope = scope,
                initialState = MultiStackNav(
                    currentIndex = 0,
                    stacks = listOf(
                        StackNav(
                            name = "UiState",
                            routes = listOf(PlaygroundRoute)
                        ),
                        StackNav(
                            name = "Permissions",
                            routes = listOf(PermissionsRoute)
                        )
                    )
                ),
                transform = { it }
            )
        override val globalUiMutator: Mutator<Mutation<UiState>, StateFlow<UiState>> =
            stateFlowMutator(
                scope = scope,
                initialState = UiState(),
                transform = { it }
            )
        override val permissionMutator: Mutator<String, StateFlow<Map<String, Boolean>>> =
            permissionsMutator(
                scope = scope,
                permissions = listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                )
            )
    }
}

interface AppDeps {
    val navMutator: Mutator<Mutation<MultiStackNav>, StateFlow<MultiStackNav>>
    val globalUiMutator: Mutator<Mutation<UiState>, StateFlow<UiState>>
    val permissionMutator: Mutator<String, StateFlow<Map<String, Boolean>>>
}

val AppDependencies = staticCompositionLocalOf<AppDeps> {
    object : AppDeps {
        override val navMutator: Mutator<Mutation<MultiStackNav>, StateFlow<MultiStackNav>>
            get() = TODO("Stub!")
        override val globalUiMutator: Mutator<Mutation<UiState>, StateFlow<UiState>>
            get() = TODO("Stub!")
        override val permissionMutator: Mutator<String, StateFlow<Map<String, Boolean>>>
            get() = TODO("Stub!")
    }
}