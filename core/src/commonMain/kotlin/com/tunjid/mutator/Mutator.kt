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

package com.tunjid.mutator

typealias StateChange<T> = Mutation<T>

interface Mutator<Action : Any, State : Any> {
    val state: State
    val accept: (Action) -> Unit
}

/**
 * Data class holding a change transform for a type [T].
 */
data class Mutation<T : Any>(
    val mutate: T.() -> T
) {
    companion object {
        /**
         * Identity [Mutation] function; semantically a no op [Mutation]
         */
        fun <T : Any> identity(): Mutation<T> = Mutation { this }
    }
}

/**
 * Combines two [Mutation] instances into a single [Mutation]
 */
operator fun <T : Any> Mutation<T>.plus(other: Mutation<T>) = Mutation<T> inner@{
    val result = this@plus.mutate(this@inner)
    other.mutate.invoke(result)
}

fun <State : Any> Mutator<Mutation<State>, *>.accept(
    mutator: State.() -> State
) = accept(Mutation(mutator))
