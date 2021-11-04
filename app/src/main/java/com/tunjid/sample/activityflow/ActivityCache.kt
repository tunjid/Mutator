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

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle

data class ActivityCache(
    val status: Lifecycle.Event = Lifecycle.Event.ON_DESTROY,
    val map: Map<Lifecycle.Event, List<ComponentActivity>> = Lifecycle.Event.values()
        .fold(mutableMapOf()) { map, status ->
            map[status] = listOf()
            map
        }
)


fun Context.activityCache(onChanged: (ActivityCache) -> Unit) {
    var cache = ActivityCache()
    fun send(event: Lifecycle.Event, activity: Activity) {
        if (activity is ComponentActivity) {
            cache = cache.copy(
                status = event,
                map = cache.map.mapValues { (key, value) ->
                    if (key == event) (value - activity) + activity
                    else value - activity
                }
            )
            onChanged(cache)
        }
    }

    val callbacks = object :
        Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) =
            send(Lifecycle.Event.ON_CREATE, activity)

        override fun onActivityStarted(activity: Activity) =
            send(Lifecycle.Event.ON_START, activity)

        override fun onActivityResumed(activity: Activity) =
            send(Lifecycle.Event.ON_RESUME, activity)

        override fun onActivityPaused(activity: Activity) = send(Lifecycle.Event.ON_PAUSE, activity)

        override fun onActivityStopped(activity: Activity) = send(Lifecycle.Event.ON_STOP, activity)

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

        override fun onActivityDestroyed(activity: Activity) =
            send(Lifecycle.Event.ON_DESTROY, activity)

    }
    val app = (applicationContext as Application)
    app.registerActivityLifecycleCallbacks(callbacks)
}
