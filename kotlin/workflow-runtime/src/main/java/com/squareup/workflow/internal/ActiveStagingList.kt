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

import com.squareup.workflow.internal.InlineLinkedList.InlineListNode

/**
 * TODO write documentation
 */
internal class ActiveStagingList<T : InlineListNode<T>> {

  /**
   * When not in the middle of a render pass, this list represents the active child workflows.
   * When in the middle of a render pass, this represents the list of children that may either
   * be re-rendered, or destroyed after the render pass is finished if they weren't re-rendered.
   *
   * During rendering, when a child is rendered, if it exists in this list it is removed from here
   * and added to [stagingChildren].
   */
  var active = InlineLinkedList<T>()

  /**
   * When not in the middle of a render pass, this list is empty.
   * When rendering, every child that gets rendered is added to this list (possibly moved over from
   * [activeChildren]).
   * When [commitRenderedChildren] is called, this list is swapped with
   * [activeChildren] and the old active list is cleared.
   */
  var staging = InlineLinkedList<T>()

  inline fun retainOrCreate(
    predicate: (T) -> Boolean,
    create: () -> T
  ): T {
    val staged = active.removeFirst(predicate) ?: create()
    staging += staged
    return staged
  }

  inline fun commitStaging(onRemove: (T) -> Unit) {
    // Any children left in the previous active list after the render finishes were not re-rendered
    // and must be torn down.
    active.forEach(onRemove)

    // Swap the lists and clear the staging one.
    val newStaging = active
    active = staging
    staging = newStaging
    staging.clear()
  }
}
