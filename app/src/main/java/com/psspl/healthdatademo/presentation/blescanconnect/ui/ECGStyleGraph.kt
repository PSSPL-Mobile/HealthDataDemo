package com.psspl.healthdatademo.presentation.blescanconnect.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp

/***
 * Name : ECGStyleGraph.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : Used to show ECG graph.
 * @param dataPoints : Stores bpm points.
 **/
@Composable
fun ECGStyleGraph(dataPoints: List<Float>) {
    val stepX = 6f // Horizontal distance (in pixels) between each ECG data point
    val speedPxPerSecond = 60f // Scrolling speed in pixels per second

    // Mutable state to track the current horizontal offset for scrolling animation
    val offsetX = remember { mutableFloatStateOf(0f) }

    // Time tracking to calculate delta between frames
    val lastTime = remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Launch a coroutine to update offset every frame (smooth scrolling)
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                val now = System.currentTimeMillis()
                val delta = now - lastTime.longValue // Time difference in ms
                lastTime.longValue = now

                // Update the horizontal offset based on elapsed time
                offsetX.floatValue = (offsetX.floatValue + speedPxPerSecond * delta / 1000f) % stepX
            }
        }
    }

    // Canvas for drawing the ECG grid and waveform
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val height = size.height
        val width = size.width

        // 🔹 Draw background grid
        val smallGridSpacing = 20f // Grid spacing in pixels
        val gridColor = Color.LightGray.copy(alpha = 0.2f) // Light gray grid lines

        // Draw vertical grid lines, adjusted for scrolling offset
        var x = -offsetX.floatValue % smallGridSpacing
        while (x < width) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f
            )
            x += smallGridSpacing
        }

        // Draw horizontal grid lines
        var y = 0f
        while (y < height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            y += smallGridSpacing
        }

        // 🔹 Draw the ECG waveform

        // Number of data points that can fit within the current visible width
        val visiblePoints = (width / stepX).toInt() + 2

        // Get the latest subset of data points to display
        val startIndex = (dataPoints.size - visiblePoints).coerceAtLeast(0)
        val subList = dataPoints.subList(startIndex, dataPoints.size)

        // Translate the entire waveform to the left to simulate scrolling
        translate(left = -offsetX.floatValue) {
            for (i in 1 until subList.size) {
                val x1 = (i - 1) * stepX
                val x2 = i * stepX

                // Convert normalized Y values [0f, 1f] into canvas coordinates
                val y1 = height * (1f - subList[i - 1].coerceIn(0f, 1f))
                val y2 = height * (1f - subList[i].coerceIn(0f, 1f))

                // Draw the ECG waveform line segment
                drawLine(
                    color = Color.Green,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 3f
                )
            }
        }
    }
}