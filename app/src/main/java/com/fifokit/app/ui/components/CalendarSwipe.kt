package com.fifokit.app.ui.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

fun Modifier.calendarHorizontalSwipe(
    onPrevious: () -> Unit,
    onNext: () -> Unit
): Modifier {
    return pointerInput(
        onPrevious,
        onNext
    ) {
        var totalDrag = 0f
        val threshold =
            48.dp.toPx()

        detectHorizontalDragGestures(
            onDragStart = {
                totalDrag = 0f
            },
            onHorizontalDrag = {
                    _,
                    dragAmount ->
                totalDrag += dragAmount
            },
            onDragEnd = {
                when {
                    totalDrag <= -threshold ->
                        onNext()

                    totalDrag >= threshold ->
                        onPrevious()
                }

                totalDrag = 0f
            },
            onDragCancel = {
                totalDrag = 0f
            }
        )
    }
}
