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
package com.squareup.workflow.internal

import com.squareup.workflow.Worker
import com.squareup.workflow.WorkflowAction
import com.squareup.workflow.internal.InlineLinkedList.InlineListNode
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * TODO kdoc
 *
 *
 * Holds the channel representing the outputs of a worker, as well as a tombstone flag that is
 * true after the worker has finished and we've reported that fact to the workflow. This is to
 * prevent the workflow from entering an infinite loop of getting `Finished` events if it
 * continues to listen to the worker after it finishes.
 *
 * @param channel TODO
 * @param tombstone TODO
 */
/* ktlint-disable parameter-list-wrapping */
internal class WorkerChildNode<T, StateT, OutputT : Any>(
  val worker: Worker<T>,
  val key: String,
  val channel: ReceiveChannel<ValueOrDone<*>>,
  var tombstone: Boolean = false,
  private var handler: (T) -> WorkflowAction<StateT, OutputT>
) : InlineListNode<WorkerChildNode<*, *, *>> {
/* ktlint-enable parameter-list-wrapping */

  override var nextListNode: WorkerChildNode<*, *, *>? = null

  /**
   * Updates the handler function that will be invoked by [acceptUpdate].
   */
  fun <T2, S, O : Any> setHandler(newHandler: (T2) -> WorkflowAction<S, O>) {
    @Suppress("UNCHECKED_CAST")
    handler = newHandler as (T) -> WorkflowAction<StateT, OutputT>
  }

  @Suppress("UNCHECKED_CAST")
  fun acceptUpdate(value: Any?): WorkflowAction<StateT, OutputT> =
    handler(value as T)

  /**
   * Returns true if this worker does the same work as [otherWorker] and has the same key.
   */
  fun matches(
    otherWorker: Worker<*>,
    key: String
  ): Boolean = worker.doesSameWorkAs(otherWorker) && this.key == key
}
