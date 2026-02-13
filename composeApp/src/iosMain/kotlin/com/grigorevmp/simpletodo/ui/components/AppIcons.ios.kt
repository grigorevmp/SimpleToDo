package com.grigorevmp.simpletodo.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.grigorevmp.simpletodo.ui.components.SimpleIcons

actual val HomeIcon: ImageVector = ImageVector.Builder(
    name = "Home",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.8f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(4.5f, 11f)
        lineTo(12f, 4.5f)
        lineTo(19.5f, 11f)
        moveTo(6.5f, 10.5f)
        lineTo(6.5f, 19.5f)
        lineTo(10.5f, 19.5f)
        lineTo(10.5f, 14.5f)
        lineTo(13.5f, 14.5f)
        lineTo(13.5f, 19.5f)
        lineTo(17.5f, 19.5f)
        lineTo(17.5f, 10.5f)
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
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.8f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(6.5f, 4.5f)
        lineTo(14.5f, 4.5f)
        lineTo(18.5f, 8.5f)
        lineTo(18.5f, 19.5f)
        lineTo(6.5f, 19.5f)
        close()
        moveTo(14.5f, 4.5f)
        lineTo(14.5f, 8.5f)
        lineTo(18.5f, 8.5f)
        moveTo(8f, 12f)
        lineTo(16.5f, 12f)
        moveTo(8f, 15.5f)
        lineTo(13.5f, 15.5f)
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
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.8f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(5.5f, 5.5f)
        lineTo(18.5f, 5.5f)
        lineTo(18.5f, 18.5f)
        lineTo(5.5f, 18.5f)
        close()
        moveTo(9.5f, 9.5f)
        lineTo(14.5f, 9.5f)
        lineTo(14.5f, 14.5f)
        lineTo(9.5f, 14.5f)
        close()
        moveTo(12f, 3.5f)
        lineTo(12f, 5f)
        moveTo(12f, 19f)
        lineTo(12f, 20.5f)
        moveTo(3.5f, 12f)
        lineTo(5f, 12f)
        moveTo(19f, 12f)
        lineTo(20.5f, 12f)
        moveTo(6.2f, 6.2f)
        lineTo(7.3f, 7.3f)
        moveTo(16.7f, 16.7f)
        lineTo(17.8f, 17.8f)
        moveTo(16.7f, 7.3f)
        lineTo(17.8f, 6.2f)
        moveTo(6.2f, 17.8f)
        lineTo(7.3f, 16.7f)
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
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.8f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(4.5f, 6.5f)
        lineTo(19.5f, 6.5f)
        lineTo(14f, 12.5f)
        lineTo(14f, 18.5f)
        lineTo(10f, 18.5f)
        lineTo(10f, 12.5f)
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
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.8f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(5f, 7.5f)
        lineTo(14f, 7.5f)
        lineTo(19.5f, 12f)
        lineTo(14f, 16.5f)
        lineTo(5f, 16.5f)
        close()
        moveTo(9.2f, 12f)
        lineTo(10.2f, 12f)
        lineTo(10.2f, 13f)
        lineTo(9.2f, 13f)
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

actual val FlameIcon: ImageVector = ImageVector.Builder(
    name = "Flame",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.NonZero
    ) {
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

actual val VisibilityIcon: ImageVector = SimpleIcons.Visibility

actual val VisibilityOffIcon: ImageVector = SimpleIcons.VisibilityOff
