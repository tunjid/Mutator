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

package com.tunjid.sample.activityflow

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import com.tunjid.mutator.Mutator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformWhile

private data class PermissionsCache(
    val activeActivity: ComponentActivity? = null,
    val map: Map<ComponentActivity, Map<String, Mutator<Unit, StateFlow<Boolean>>>> = mapOf()
)

fun Context.permissionsMutator(
    scope: CoroutineScope,
    permissions: List<String>
): Mutator<String, StateFlow<Map<String, Boolean>>> {
    val original = object : Mutator<String, StateFlow<PermissionsCache>> {
        override val state: StateFlow<PermissionsCache> = callbackFlow {
            var permissionCache = PermissionsCache()

            activityCache { activityCache ->
                val activityList = activityCache.map[activityCache.status]
                if (activityList == null || activityList.isEmpty()) return@activityCache

                val activity = activityList.last()

                permissionCache = when (activityCache.status) {
                    Lifecycle.Event.ON_CREATE -> permissionCache.copy(
                        activeActivity = activity,
                        map = permissionCache.map + (activity to permissions.map { permission ->
                            permission to activity.permissionStateHolder(permission)
                        }.toMap())
                    )
                    Lifecycle.Event.ON_DESTROY -> permissionCache.copy(
                        activeActivity = if (activity == permissionCache.activeActivity) null else activity,
                        map = permissionCache.map - activity
                    )
                    Lifecycle.Event.ON_RESUME -> permissionCache.copy(activeActivity = activity)
                    Lifecycle.Event.ON_START -> permissionCache.copy(activeActivity = activity)
                    else -> permissionCache
                }

                trySend(permissionCache)
            }
            awaitClose { }
        }
            .stateIn(
                scope = scope,
                initialValue = PermissionsCache(),
                started = SharingStarted.Eagerly
            )

        override val accept: (String) -> Unit = { permission ->
            val cache = state.value
            cache.activeActivity
                ?.let(cache.map::get)
                ?.get(permission)
                ?.accept?.invoke(Unit)
        }
    }

    return object : Mutator<String, StateFlow<Map<String, Boolean>>> {
        override val state: StateFlow<Map<String, Boolean>> = original.state
            .flatMapLatest { cache ->
                val permissionsToMutators = cache.activeActivity?.let(cache.map::get)
                val flows = permissionsToMutators?.entries?.map { (permission, stateHolder) ->
                    stateHolder.state.map { permission to it }
                }
                println("Flat mapping. Size: ${flows?.size}")
                if (flows != null) combine(flows) { it.toMap() }
                else emptyFlow()
            }
            .stateIn(
                scope = scope,
                initialValue = mapOf(),
                started = SharingStarted.Eagerly
            )

        override val accept: (String) -> Unit = original.accept
    }
}


private fun ComponentActivity.permissionStateHolder(permissionString: String): Mutator<Unit, StateFlow<Boolean>> {

    val stateFlow = MutableStateFlow(hasPermission(permissionString))

    lifecycle.addObserver(LifecycleEventObserver { source, event ->
        stateFlow.value = hasPermission(permissionString)
    })

    val isActive = callbackFlow {
        val observer = LifecycleEventObserver { source, event ->
            if (event == Lifecycle.Event.ON_DESTROY) trySend(false)
        }
        lifecycle.addObserver(observer)
        awaitClose { }
    }
        .onStart { emit(true) }

    val launcher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        stateFlow::value::set
    )

    return object : Mutator<Unit, StateFlow<Boolean>> {
        override val state: StateFlow<Boolean> = combine(
            stateFlow,
            isActive,
            ::Pair
        )
            .transformWhile { (hasPermission, isActive) ->
                emit(hasPermission)
                isActive
            }
            .stateIn(
                initialValue = hasPermission(permissionString),
                scope = lifecycleScope,
                started = SharingStarted.Eagerly
            )


        override val accept: (Unit) -> Unit = { launcher.launch(permissionString) }
    }
}

private fun ComponentActivity.hasPermission(permissionString: String) =
    ContextCompat.checkSelfPermission(
        this,
        permissionString
    ) == PackageManager.PERMISSION_GRANTED