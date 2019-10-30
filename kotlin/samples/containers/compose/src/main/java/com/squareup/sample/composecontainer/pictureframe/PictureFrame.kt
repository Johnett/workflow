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
package com.squareup.sample.composecontainer.pictureframe

import androidx.compose.Composable
import androidx.ui.core.Dp
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.foundation.shape.border.Border
import androidx.ui.foundation.shape.border.DrawBorder
import androidx.ui.foundation.shape.corner.CutCornerShape
import androidx.ui.graphics.Color
import androidx.ui.layout.Container
import androidx.ui.layout.EdgeInsets
import androidx.ui.material.MaterialTheme
import androidx.ui.material.surface.Surface
import androidx.ui.tooling.preview.Preview

/**
 * Composable function that draws a fancy picture frame around its children.
 */
@Composable
fun PictureFrame(
  thickness: Dp = 10.dp,
  children: @Composable() () -> Unit
) {
  Container(padding = EdgeInsets(thickness)) {
    DrawBorder(
        shape = CutCornerShape(percent = 5),
        border = Border(Color.Green.copy(green = .75f), thickness)
    )
    DrawBorder(
        shape = CutCornerShape(percent = 25),
        border = Border(Color.Red.copy(alpha = .75f), thickness)
    )
    DrawBorder(
        shape = CutCornerShape(percent = 50),
        border = Border(Color.Blue.copy(alpha = .75f), thickness)
    )
    Surface {
      children()
    }
  }
}

@Preview
@Composable
fun PictureFramePreview() {
  MaterialTheme {
    PictureFrame(thickness = 10.dp) {
      Text("Hello")
    }
  }
}
