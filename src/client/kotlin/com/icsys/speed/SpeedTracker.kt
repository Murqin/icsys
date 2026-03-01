package com.icsys.speed

import com.icsys.config.HudConfig
import net.minecraft.client.network.ClientPlayerEntity
import kotlin.math.sqrt

object SpeedTracker {

    private const val TICKS_PER_SECOND: Double = 20.0
    private const val MAX_WINDOW: Int = 200
    private const val DRIFT_CORRECTION_INTERVAL: Int = 1000

    private var prevX: Double = 0.0
    private var prevY: Double = 0.0
    private var prevZ: Double = 0.0
    private var initialized: Boolean = false

    private val speedHistory: ArrayDeque<Double> = ArrayDeque()
    private val horizontalSpeedHistory: ArrayDeque<Double> = ArrayDeque()
    private val verticalSpeedHistory: ArrayDeque<Double> = ArrayDeque()

    private var speedSum: Double = 0.0
    private var hSpeedSum: Double = 0.0
    private var vSpeedSum: Double = 0.0
    private var tickCounter: Int = 0

    var currentSpeed: Double = 0.0
        private set

    var currentHorizontalSpeed: Double = 0.0
        private set

    var currentVerticalSpeed: Double = 0.0
        private set

    fun tick(player: ClientPlayerEntity?) {
        if (player == null) {
            reset()
            return
        }

        val x: Double = player.x
        val y: Double = player.y
        val z: Double = player.z

        if (!initialized) {
            prevX = x
            prevY = y
            prevZ = z
            initialized = true
            return
        }

        val dx: Double = x - prevX
        val dy: Double = y - prevY
        val dz: Double = z - prevZ

        prevX = x
        prevY = y
        prevZ = z

        val speed3d: Double = sqrt(dx * dx + dy * dy + dz * dz) * TICKS_PER_SECOND
        val speedHorizontal: Double = sqrt(dx * dx + dz * dz) * TICKS_PER_SECOND
        val speedVertical: Double = dy * TICKS_PER_SECOND

        val window: Int = HudConfig.smoothingWindow
        speedSum = pushToHistory(speedHistory, speed3d, window, speedSum)
        hSpeedSum = pushToHistory(horizontalSpeedHistory, speedHorizontal, window, hSpeedSum)
        vSpeedSum = pushToHistory(verticalSpeedHistory, speedVertical, window, vSpeedSum)

        currentSpeed = if (speedHistory.isEmpty()) 0.0 else speedSum / speedHistory.size
        currentHorizontalSpeed = if (horizontalSpeedHistory.isEmpty()) 0.0 else hSpeedSum / horizontalSpeedHistory.size
        currentVerticalSpeed = if (verticalSpeedHistory.isEmpty()) 0.0 else vSpeedSum / verticalSpeedHistory.size

        tickCounter++
        if (tickCounter >= DRIFT_CORRECTION_INTERVAL) {
            tickCounter = 0
            speedSum = speedHistory.sumOf { it }
            hSpeedSum = horizontalSpeedHistory.sumOf { it }
            vSpeedSum = verticalSpeedHistory.sumOf { it }
        }
    }

    fun reset() {
        initialized = false
        prevX = 0.0
        prevY = 0.0
        prevZ = 0.0
        speedHistory.clear()
        horizontalSpeedHistory.clear()
        verticalSpeedHistory.clear()
        speedSum = 0.0
        hSpeedSum = 0.0
        vSpeedSum = 0.0
        tickCounter = 0
        currentSpeed = 0.0
        currentHorizontalSpeed = 0.0
        currentVerticalSpeed = 0.0
    }

    private fun pushToHistory(history: ArrayDeque<Double>, value: Double, window: Int, currentSum: Double): Double {
        val safeWindow: Int = window.coerceIn(1, MAX_WINDOW)
        var sum: Double = currentSum
        while (history.size >= safeWindow) {
            sum -= history.removeFirst()
        }
        history.addLast(value)
        sum += value
        return sum
    }
}
