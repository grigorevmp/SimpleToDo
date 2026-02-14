package com.grigorevmp.simpletodo.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object SimpleIcons {
    val ArrowUp: ImageVector = ImageVector.Builder(
        name = "ArrowUp",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black), pathFillType = PathFillType.NonZero) {
            moveTo(7f, 14f)
            lineTo(12f, 9f)
            lineTo(17f, 14f)
            lineTo(15.5f, 15.5f)
            lineTo(12f, 12f)
            lineTo(8.5f, 15.5f)
            close()
        }
    }.build()

    val ArrowDown: ImageVector = ImageVector.Builder(
        name = "ArrowDown",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black), pathFillType = PathFillType.NonZero) {
            moveTo(7f, 10f)
            lineTo(12f, 15f)
            lineTo(17f, 10f)
            lineTo(15.5f, 8.5f)
            lineTo(12f, 12f)
            lineTo(8.5f, 8.5f)
            close()
        }
    }.build()

    val Flame: ImageVector = ImageVector.Builder(
        name = "Flame",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black), pathFillType = PathFillType.NonZero) {
            moveTo(12f, 3f)
            lineTo(16f, 9f)
            lineTo(14.5f, 12.5f)
            lineTo(16f, 15f)
            lineTo(12f, 21f)
            lineTo(8f, 15f)
            lineTo(9.5f, 12.5f)
            lineTo(8f, 9f)
            close()
        }
    }.build()

    val Visibility: ImageVector = ImageVector.Builder(
        name = "Visibility",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black), pathFillType = PathFillType.NonZero) {
            moveTo(3f, 12f)
            lineTo(6f, 8f)
            lineTo(12f, 6f)
            lineTo(18f, 8f)
            lineTo(21f, 12f)
            lineTo(18f, 16f)
            lineTo(12f, 18f)
            lineTo(6f, 16f)
            close()
            moveTo(12f, 9f)
            lineTo(14f, 12f)
            lineTo(12f, 15f)
            lineTo(10f, 12f)
            close()
        }
    }.build()

    val VisibilityOff: ImageVector = ImageVector.Builder(
        name = "VisibilityOff",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black), pathFillType = PathFillType.NonZero) {
            moveTo(4f, 5f)
            lineTo(5.5f, 3.5f)
            lineTo(20.5f, 18.5f)
            lineTo(19f, 20f)
            close()
            moveTo(3f, 12f)
            lineTo(6f, 8f)
            lineTo(12f, 6f)
            lineTo(14f, 6.5f)
            lineTo(8f, 12.5f)
            lineTo(6f, 16f)
            close()
        }
    }.build()

    val Star: ImageVector = ImageVector.Builder(
        name = "Star",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black), pathFillType = PathFillType.NonZero) {
            moveTo(12f, 3.5f)
            lineTo(14.9f, 9f)
            lineTo(21f, 9.8f)
            lineTo(16.5f, 14f)
            lineTo(17.8f, 20f)
            lineTo(12f, 16.9f)
            lineTo(6.2f, 20f)
            lineTo(7.5f, 14f)
            lineTo(3f, 9.8f)
            lineTo(9.1f, 9f)
            close()
        }
    }.build()


    val Save: ImageVector = ImageVector.Builder(
        name = "Save",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black), pathFillType = PathFillType.NonZero) {
            moveTo(5f, 4f)
            lineTo(17f, 4f)
            lineTo(20f, 7f)
            lineTo(20f, 20f)
            lineTo(5f, 20f)
            close()
            moveTo(7f, 6f)
            lineTo(15f, 6f)
            lineTo(15f, 10f)
            lineTo(7f, 10f)
            close()
        }
    }.build()

    val Check: ImageVector = ImageVector.Builder(
        name = "Check",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black), pathFillType = PathFillType.NonZero) {
            moveTo(6f, 12.5f)
            lineTo(10f, 16.5f)
            lineTo(18f, 8.5f)
            lineTo(16.5f, 7f)
            lineTo(10f, 13.5f)
            lineTo(7.5f, 11f)
            close()
        }
    }.build()
}
