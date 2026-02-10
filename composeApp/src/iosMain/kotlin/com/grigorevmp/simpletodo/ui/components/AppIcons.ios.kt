package com.grigorevmp.simpletodo.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

actual val HomeIcon: ImageVector = ImageVector.Builder(
    name = "Home",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(4f, 10f)
        lineTo(12f, 3f)
        lineTo(20f, 10f)
        lineTo(20f, 21f)
        lineTo(14f, 21f)
        lineTo(14f, 15f)
        lineTo(10f, 15f)
        lineTo(10f, 21f)
        lineTo(4f, 21f)
        close()
    }
}.build()

actual val NotesIcon: ImageVector = ImageVector.Builder(
    name = "Notes",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(6f, 4f)
        lineTo(14f, 4f)
        lineTo(18f, 8f)
        lineTo(18f, 20f)
        lineTo(6f, 20f)
        close()
        moveTo(14f, 4f)
        lineTo(14f, 8f)
        lineTo(18f, 8f)
        close()
        moveTo(8f, 11f)
        lineTo(16f, 11f)
        lineTo(16f, 12.5f)
        lineTo(8f, 12.5f)
        close()
        moveTo(8f, 15f)
        lineTo(13f, 15f)
        lineTo(13f, 16.5f)
        lineTo(8f, 16.5f)
        close()
    }
}.build()

actual val SettingsIcon: ImageVector = ImageVector.Builder(
    name = "Settings",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(12f, 7f)
        cubicTo(9.8f, 7f, 8f, 8.8f, 8f, 11f)
        cubicTo(8f, 13.2f, 9.8f, 15f, 12f, 15f)
        cubicTo(14.2f, 15f, 16f, 13.2f, 16f, 11f)
        cubicTo(16f, 8.8f, 14.2f, 7f, 12f, 7f)
        close()
    }
}.build()

actual val FilterIcon: ImageVector = ImageVector.Builder(
    name = "Filter",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(4f, 6f)
        lineTo(20f, 6f)
        lineTo(14f, 13f)
        lineTo(14f, 19f)
        lineTo(10f, 19f)
        lineTo(10f, 13f)
        close()
    }
}.build()

actual val TagIcon: ImageVector = ImageVector.Builder(
    name = "Tag",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(4f, 7f)
        lineTo(14f, 7f)
        lineTo(20f, 12f)
        lineTo(14f, 17f)
        lineTo(4f, 17f)
        close()
        moveTo(8.5f, 12f)
        cubicTo(8.5f, 11.1716f, 9.1716f, 10.5f, 10f, 10.5f)
        cubicTo(10.8284f, 10.5f, 11.5f, 11.1716f, 11.5f, 12f)
        cubicTo(11.5f, 12.8284f, 10.8284f, 13.5f, 10f, 13.5f)
        cubicTo(9.1716f, 13.5f, 8.5f, 12.8284f, 8.5f, 12f)
        close()
    }
}.build()

actual val EditIcon: ImageVector = ImageVector.Builder(
    name = "Edit",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(4f, 17f)
        lineTo(4f, 20f)
        lineTo(7f, 20f)
        lineTo(18f, 9f)
        lineTo(15f, 6f)
        close()
        moveTo(16f, 5f)
        lineTo(19f, 8f)
        lineTo(20.5f, 6.5f)
        lineTo(17.5f, 3.5f)
        close()
    }
}.build()

actual val FolderIcon: ImageVector = ImageVector.Builder(
    name = "Folder",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(4f, 7f)
        lineTo(10f, 7f)
        lineTo(12f, 9f)
        lineTo(20f, 9f)
        lineTo(20f, 18f)
        lineTo(4f, 18f)
        close()
    }
}.build()

actual val NoteIcon: ImageVector = ImageVector.Builder(
    name = "Note",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(6f, 4f)
        lineTo(18f, 4f)
        lineTo(18f, 20f)
        lineTo(6f, 20f)
        close()
        moveTo(8f, 9f)
        lineTo(16f, 9f)
        lineTo(16f, 10.5f)
        lineTo(8f, 10.5f)
        close()
        moveTo(8f, 13f)
        lineTo(13f, 13f)
        lineTo(13f, 14.5f)
        lineTo(8f, 14.5f)
        close()
    }
}.build()
