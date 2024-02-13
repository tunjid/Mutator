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

/**
 * Type definition for a unit of change for a type [T].
 */
typealias Mutation<State> = State.() -> State

interface Mutator<State : Any> {
    suspend fun mutate(mutation: Mutation<State>)
}

typealias StateHolder<State> = StateProducer<State>

interface StateProducer<State : Any> {
    val state: State
}

interface ActionStateProducer<Action : Any, State : Any> : StateProducer<State> {
    val accept: (Action) -> Unit
}

/**
 * Syntactic sugar for creating a [Mutation]
 */
inline fun <State> mutationOf(noinline mutation: State.() -> State): Mutation<State> = mutation

/**
 * Identity [Mutation] function; semantically a no op [Mutation]
 */
fun <State : Any> identity(): Mutation<State> = mutationOf { this }

/**
 * Combines two [Mutation] instances into a single [Mutation]
 */
operator fun <T : Any> Mutation<T>.plus(other: Mutation<T>) = mutationOf<T> inner@{
    val result = this@plus(this@inner)
    other.invoke(result)
}
