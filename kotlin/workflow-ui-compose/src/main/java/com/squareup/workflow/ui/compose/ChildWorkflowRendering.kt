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
package com.squareup.workflow.ui.compose

import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.Composable
import androidx.compose.Effect
import androidx.compose.ambient
import androidx.compose.memo
import androidx.compose.unaryPlus
import com.squareup.workflow.ui.ContainerHints
import com.squareup.workflow.ui.ViewBinding
import com.squareup.workflow.ui.ViewRegistry
import com.squareup.workflow.ui.WorkflowViewStub
import com.squareup.workflow.ui.compose.ComposableViewStubWrapper.Update
import kotlin.reflect.KClass

/**
 * Displays a rendering using the current [ViewRegistry][com.squareup.workflow.ui.ViewRegistry] to
 * generate the view.
 *
 * ## Example
 *
 * ```
 * data class FramedRendering(
 *   val borderColor: Color,
 *   val child: Any
 * )
 *
 * val FramedContainerBinding = bindCompose { rendering: FramedRendering ->
 *   Surface(border = Border(rendering.borderColor, 8.dp)) {
 *     ChildWorkflowRendering(rendering.child)
 *   }
 * }
 * ```
 *
 * @throws IllegalStateException If not called from a [bindCompose] context.
 */
@Composable
@Suppress("UNUSED_VARIABLE", "UNUSED_PARAMETER")
fun <RenderingT : Any> ChildWorkflowRendering(rendering: RenderingT) {
  val containerHints = +ambient(ContainerHintsAmbient)

  // If the child binding is also a Composable, we don't need to go through the legacy view
  // system and can just invoke the binding's composable function directly.
  val binding = +viewBinding(rendering::class)
  if (binding is ComposeViewBinding) {
    binding.showRendering(rendering)
    return
  }

  // IntelliJ currently complains very loudly about this function call, but it actually compiles.
  // The IDE tooling isn't currently able to recognize that the Compose compiler accepts this code.
  // **Note that this will probably fail, since Compose doesn't currently support rendering legacy
  // views inside any other composable functions (they must be the root composable).
  ComposableViewStubWrapper(update = Update(rendering, containerHints))
}

/**
 * Memoize on the container hints value (which should never change) and the rendering type,
 * which should also never change, so that we don't do these two map lookups on every composition.
 */
private fun <RenderingT : Any> viewBinding(
  renderingType: KClass<out RenderingT>
): Effect<ViewBinding<RenderingT>?> {
  val containerHints = +ambient(ContainerHintsAmbient)
  return memo(containerHints, renderingType) {
    containerHints[ViewRegistry].getBindingFor(renderingType)
  }
}

/**
 * Wraps a [WorkflowViewStub] with an API that is more Compose-friendly.
 *
 * In particular, Compose will only generate `Emittable`s for views with a single constructor
 * that takes a [Context].
 *
 * See [this slack message](https://kotlinlang.slack.com/archives/CJLTWPH7S/p1576264533012000?thread_ts=1576262311.008800&cid=CJLTWPH7S).
 */
private class ComposableViewStubWrapper(context: Context) : FrameLayout(context) {

  data class Update(
    val rendering: Any,
    val containerHints: ContainerHints
  )

  private val viewStub = WorkflowViewStub(context)

  init {
    addView(viewStub, LayoutParams(MATCH_PARENT, MATCH_PARENT))
  }

  // Compose turns this into a parameter when you invoke this class as a Composable.
  fun setUpdate(update: Update) {
    println("Updating WorkflowViewStub with $update…")
    viewStub.update(update.rendering, update.containerHints)
  }
}
