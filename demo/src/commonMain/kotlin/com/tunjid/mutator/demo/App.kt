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

package com.tunjid.mutator.demo

import androidx.compose.runtime.Composable
import com.tunjid.mutator.demo.editor.VerticalLayout
import com.tunjid.mutator.demo.sections.Section1
import com.tunjid.mutator.demo.snails.Snail10
import com.tunjid.mutator.demo.snails.Snail2
import com.tunjid.mutator.demo.snails.Snail3
import com.tunjid.mutator.demo.snails.Snail4
import com.tunjid.mutator.demo.snails.Snail5
import com.tunjid.mutator.demo.snails.Snail6
import com.tunjid.mutator.demo.snails.Snail7
import com.tunjid.mutator.demo.snails.Snail8
import com.tunjid.mutator.demo.snails.Snail9
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

@Composable
fun App(
) {
    VerticalLayout {
        Section1()
        Snail2()
        Snail3()
        Snail4()
        Snail5()
        Snail6()
        Snail7()
        Snail8()
        Snail9()
        Snail10()
    }
}


data class LargeState(
    val property1: Int,
    val property2: Int,
    val property3: Int,
    val property4: Int,
    val property5: Int,
    val property6: Int,
    val property7: Int,
    val property8: Int,
)

data class IntermediateState1(
    val property1: Int,
    val property2: Int,
    val property3: Int,
    val property4: Int,
)

data class IntermediateState2(
    val property5: Int,
    val property6: Int,
    val property7: Int,
    val property8: Int,
)

fun intFlow() = flowOf(1)

class LargeStateHolder {
    private val intermediateState1 = combine(
        intFlow(),
        intFlow(),
        intFlow(),
        intFlow(),
        ::IntermediateState1
    )

    private val intermediateState2 = combine(
        intFlow(),
        intFlow(),
        intFlow(),
        intFlow(),
        ::IntermediateState2
    )

    val state = combine(
        intermediateState1,
        intermediateState2
    ) { state1, state2 ->
        LargeState(
            state1.property1,
            state1.property2,
            state1.property3,
            state1.property4,
            state2.property5,
            state2.property5,
            state2.property6,
            state2.property7,
        )
    }
}