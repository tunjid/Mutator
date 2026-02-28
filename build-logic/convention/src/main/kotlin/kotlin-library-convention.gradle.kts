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
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension

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

plugins {
    kotlin("multiplatform")
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}

kotlin {
    applyDefaultHierarchyTemplate()

    js(IR) {
        nodejs()
        browser()
    }

    jvm()

    iosArm64()
    iosSimulatorArm64()

    val isComposeMultiplatformProject = project
        // Not using project.provider deliberately.
        // See https://github.com/gradle/gradle/issues/23572
        .findProperty("isComposeMultiplatform")
        ?.toString()
        ?.toBoolean()
        ?: false

    if (!isComposeMultiplatformProject) {
        linuxX64()

        macosArm64()
        macosX64()

        mingwX64()

        tvosArm64()
        tvosSimulatorArm64()
        tvosX64()

        watchosArm32()
        watchosArm64()
        watchosDeviceArm64()
        watchosSimulatorArm64()
        watchosX64()
    }

    sourceSets {
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
    configureKotlinJvm()
}
