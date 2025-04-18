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

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
}

kotlin {
    js(IR) {
        nodejs()
        browser()
    }

    jvm()

    sourceSets {
        @Suppress("UnusedPrivateMember")
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        all {
            languageSettings.apply {
                optIn("androidx.compose.animation.ExperimentalAnimationApi")
                optIn("androidx.compose.foundation.ExperimentalFoundationApi")
                optIn("androidx.compose.ui.ExperimentalComposeUiApi")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlinx.coroutines.FlowPreview")
            }
        }
    }
    targets.withType(KotlinNativeTarget::class.java) {
        binaries.all {
            binaryOptions["memoryModel"] = "experimental"
        }
    }
    configureKotlinJvm()
}
