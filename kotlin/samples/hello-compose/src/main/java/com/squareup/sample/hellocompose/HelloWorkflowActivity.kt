/*
 * Copyright 2019 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.sample.hellocompose

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.ui.core.setContent
import androidx.ui.material.MaterialTheme
import com.squareup.workflow.compose.WorkflowContainer
import com.squareup.workflow.diagnostic.SimpleLoggingDiagnosticListener

class HelloWorkflowActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        WorkflowContainer(
            workflow = HelloWorkflow,
            props = Unit,
            diagnosticListener = SimpleLoggingDiagnosticListener(),
            onOutput = {}
        ) { rendering ->
          DrawHelloRendering(rendering)
        }
      }
    }
  }
}
