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

package com.tunjid.mutator.coroutines

import com.tunjid.mutator.Mutation

data class State(
    val count: Double = 0.0
)

sealed class Action

sealed class IntAction : Action() {
    abstract val value: Int

    data class Add(override val value: Int) : IntAction()
    data class Subtract(override val value: Int) : IntAction()
}

sealed class DoubleAction : Action() {
    abstract val value: Double

    data class Divide(override val value: Double) : DoubleAction()
    data class Multiply(override val value: Double) : DoubleAction()
}

val Action.mutation: Mutation<State>
    get() = when (this) {
        is IntAction.Add -> Mutation { copy(count = count + value) }
        is IntAction.Subtract -> Mutation { copy(count = count - value) }
        is DoubleAction.Divide -> Mutation { copy(count = count / value) }
        is DoubleAction.Multiply -> Mutation { copy(count = count * value) }
    }
