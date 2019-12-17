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
@file:Suppress("EXPERIMENTAL_API_USAGE", "DEPRECATION", "OverridingDeprecatedMember")

package com.squareup.workflow.internal

import com.squareup.workflow.RenderContext
import com.squareup.workflow.Sink
import com.squareup.workflow.Snapshot
import com.squareup.workflow.StatefulWorkflow
import com.squareup.workflow.Worker
import com.squareup.workflow.Workflow
import com.squareup.workflow.WorkflowAction
import com.squareup.workflow.WorkflowAction.Companion.noAction
import com.squareup.workflow.WorkflowAction.Mutator
import com.squareup.workflow.applyTo
import com.squareup.workflow.internal.RealRenderContext.Renderer
import com.squareup.workflow.internal.RealRenderContext.WorkerRunner
import com.squareup.workflow.internal.RealRenderContextTest.TestRenderer.Rendering
import com.squareup.workflow.makeEventSink
import com.squareup.workflow.renderChild
import com.squareup.workflow.stateless
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.fail

class RealRenderContextTest {

  private class TestRenderer : Renderer<String, String> {

    data class Rendering(
      val child: Workflow<*, *, *>,
      val props: Any?,
      val key: String,
      val handler: (Any) -> WorkflowAction<String, String>
    )

    @Suppress("UNCHECKED_CAST")
    override fun <ChildPropsT, ChildOutputT : Any, ChildRenderingT> render(
      child: Workflow<ChildPropsT, ChildOutputT, ChildRenderingT>,
      props: ChildPropsT,
      key: String,
      handler: (ChildOutputT) -> WorkflowAction<String, String>
    ): ChildRenderingT = Rendering(
        child,
        props,
        key,
        handler as (Any) -> WorkflowAction<String, String>
    ) as ChildRenderingT
  }

  private class TestRunner : WorkerRunner<String, String> {
    override fun <T> runningWorker(
      worker: Worker<T>,
      key: String,
      handler: (T) -> WorkflowAction<String, String>
    ) {
      // No-op
    }
  }

  private class TestWorkflow : StatefulWorkflow<String, String, String, Rendering>() {
    override fun initialState(
      props: String,
      snapshot: Snapshot?
    ): String = fail()

    override fun render(
      props: String,
      state: String,
      context: RenderContext<String, String>
    ): Rendering {
      fail("This shouldn't actually be called.")
    }

    override fun snapshotState(state: String): Snapshot = fail()
  }

  private class PoisonRenderer<S, O : Any> : Renderer<S, O> {
    override fun <ChildPropsT, ChildOutputT : Any, ChildRenderingT> render(
      child: Workflow<ChildPropsT, ChildOutputT, ChildRenderingT>,
      props: ChildPropsT,
      key: String,
      handler: (ChildOutputT) -> WorkflowAction<S, O>
    ): ChildRenderingT = fail()
  }

  private class PoisonRunner<S, O : Any> : WorkerRunner<S, O> {
    override fun <T> runningWorker(
      worker: Worker<T>,
      key: String,
      handler: (T) -> WorkflowAction<S, O>
    ) {
      fail()
    }
  }

  private val eventActionsChannel = Channel<WorkflowAction<String, String>>(capacity = UNLIMITED)

  @Test fun `onEvent completes update`() {
    val context = RealRenderContext(PoisonRenderer(), PoisonRunner(), eventActionsChannel)
    val expectedUpdate = noAction<String, String>()
    val handler = context.onEvent<String> { expectedUpdate }
    assertTrue(eventActionsChannel.isEmpty)

    handler("")

    assertFalse(eventActionsChannel.isEmpty)
    val actualUpdate = eventActionsChannel.poll()
    assertSame(expectedUpdate, actualUpdate)
  }

  @Test fun `onEvent allows multiple invocations`() {
    val context = RealRenderContext(PoisonRenderer(), PoisonRunner(), eventActionsChannel)
    fun expectedUpdate(msg: String) = object : WorkflowAction<String, String> {
      override fun Mutator<String>.apply(): String? = null
      override fun toString(): String = "action($msg)"
    }

    val handler = context.onEvent<String> { expectedUpdate(it) }
    handler("one")

    // Shouldn't throw.
    handler("two")
  }

  @Test fun `makeActionSink completes update`() {
    val context = RealRenderContext(PoisonRenderer(), PoisonRunner(), eventActionsChannel)
    val stringAction = WorkflowAction<String, String>({ "stringAction" }) { }
    val sink = context.makeActionSink<WorkflowAction<String, String>>()
    // Enable sink sends.
    context.freeze()

    assertTrue(eventActionsChannel.isEmpty)

    sink.send(stringAction)

    assertFalse(eventActionsChannel.isEmpty)
    val actualAction = eventActionsChannel.poll()
    assertSame(stringAction, actualAction)
  }

  @Test fun `makeActionSink allows multiple sends`() {
    val context = RealRenderContext(PoisonRenderer(), PoisonRunner(), eventActionsChannel)
    val firstAction = object : WorkflowAction<String, String> {
      override fun Mutator<String>.apply(): String? = null
      override fun toString(): String = "firstAction"
    }
    val secondAction = object : WorkflowAction<String, String> {
      override fun Mutator<String>.apply(): String? = null
      override fun toString(): String = "secondAction"
    }
    val sink = context.makeActionSink<WorkflowAction<String, String>>()
    // Enable sink sends.
    context.freeze()

    sink.send(firstAction)

    // Shouldn't throw.
    sink.send(secondAction)
  }

  @Test fun `makeEventSink gets event`() {
    val context = RealRenderContext(PoisonRenderer(), PoisonRunner(), eventActionsChannel)
    val sink: Sink<String> = context.makeEventSink { setOutput(it) }
    // Enable sink sends.
    context.freeze()

    sink.send("foo")

    val update = eventActionsChannel.poll()!!
    val (state, output) = update.applyTo("state")
    assertEquals("state", state)
    assertEquals("foo", output)
  }

  @Test fun `makeEventSink works with OutputT of Nothing`() {
    val nothingChannel = Channel<WorkflowAction<String, Nothing>>(capacity = UNLIMITED)

    val context = RealRenderContext(PoisonRenderer(), PoisonRunner(), nothingChannel)
    val sink: Sink<String> = context.makeEventSink { }
    // Enable sink sends.
    context.freeze()

    sink.send("foo")

    val update = nothingChannel.poll()!!
    val (state, output) = update.applyTo("state")
    assertEquals("state", state)
    assertNull(output)
  }

  @Test fun `renderChild works`() {
    val context = RealRenderContext(TestRenderer(), TestRunner(), eventActionsChannel)
    val workflow = TestWorkflow()

    val (child, props, key, handler) = context.renderChild(workflow, "props", "key") { output ->
      WorkflowAction { setOutput("output:$output") }
    }

    assertSame(workflow, child)
    assertEquals("props", props)
    assertEquals("key", key)

    val (state, output) = handler.invoke("output").applyTo("state")
    assertEquals("state", state)
    assertEquals("output:output", output)
  }

  @Test fun `all methods throw after freeze`() {
    val context = RealRenderContext(TestRenderer(), TestRunner(), eventActionsChannel)
    context.freeze()

    assertFailsWith<IllegalStateException> { context.onEvent<Unit> { fail() } }
    val child = Workflow.stateless<Unit, Nothing, Unit> { fail() }
    assertFailsWith<IllegalStateException> { context.renderChild(child) }
    val worker = Worker.from { Unit }
    assertFailsWith<IllegalStateException> { context.runningWorker(worker) { fail() } }
    assertFailsWith<IllegalStateException> { context.freeze() }
  }
}
