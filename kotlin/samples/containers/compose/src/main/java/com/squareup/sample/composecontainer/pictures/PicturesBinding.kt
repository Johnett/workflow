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
package com.squareup.sample.composecontainer.pictures

import androidx.ui.core.Text
import com.squareup.sample.composecontainer.pictures.PicturesWorkflow.Rendering
import com.squareup.workflow.ui.compose.bindCompose

val PicturesBinding = bindCompose<Rendering> { rendering ->
  //  WithConstraints { constraints ->
//    val image = +image(
//        url = rendering.pictureUrl,
//        width = constraints.maxWidth,
//        height = constraints.maxHeight
//    ) ?: return@WithConstraints
//
//    Draw { canvas: Canvas, _: PxSize ->
//      canvas.nativeCanvas.drawBitmap(image, 0f, 0f, null)
//    }
//  }
  Text("{{TODO placeholder for ${rendering.pictureDescription}}}")
}

//private fun image(
//  url: String,
//  width: IntPx,
//  height: IntPx
//) = effectOf<Bitmap?> {
//  val image = +state<Bitmap?> { null }
//  val context = +ambient(ContextAmbient)
//
//  +onCommit(url, width, height, context) {
//    val requestDisposable = Coil.load(context, url) {
//      size(width.value, height.value)
//      target(
//          onSuccess = { image.value = (it as? BitmapDrawable)?.bitmap },
//          onError = { image.value = null }
//      )
//    }
//
//    onDispose { requestDisposable.dispose() }
//  }
//
//  return@effectOf image.value
//}
