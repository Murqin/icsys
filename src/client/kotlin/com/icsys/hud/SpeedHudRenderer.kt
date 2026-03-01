package com.icsys.hud

import com.icsys.config.HudConfig
import com.icsys.config.ModConfig
import com.icsys.speed.SpeedTracker
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.Identifier
import java.util.Locale

object SpeedHudRenderer {

    private val ELEMENT_ID: Identifier = Identifier.of("icsys", "speed_hud")

    private const val SPEED_SLOW_THRESHOLD: Double = 5.0
    private const val SPEED_FAST_THRESHOLD: Double = 15.0

    private var cachedSpeed: Double = Double.NaN
    private var cachedHSpeed: Double = Double.NaN
    private var cachedVSpeed: Double = Double.NaN
    private var cachedSpeedStr: String = ""
    private var cachedHSpeedStr: String = ""
    private var cachedVSpeedStr: String = ""

    fun register() {
        HudElementRegistry.addLast(ELEMENT_ID) { drawContext: DrawContext, _: RenderTickCounter ->
            if (!ModConfig.hudEnabled) return@addLast

            val client: MinecraftClient = MinecraftClient.getInstance()
            if (client.player == null) return@addLast
            if (client.debugHud.shouldShowDebugHud()) return@addLast

            render(drawContext, client)
        }
    }

    private fun render(drawContext: DrawContext, client: MinecraftClient) {
        val textRenderer = client.textRenderer
        val window = client.window

        updateCachedStrings()

        val items: List<HudLayoutEngine.MetricItem> = buildCachedMetricItems()
        if (items.isEmpty()) return

        val layout: HudLayoutEngine.LayoutResult = HudLayoutEngine.computeLayout(items, textRenderer)

        val screenWidth: Int = window.scaledWidth
        val screenHeight: Int = window.scaledHeight
        val hudX: Int = HudConfig.getScreenX(screenWidth, layout.width)
        val hudY: Int = HudConfig.getScreenY(screenHeight, layout.height)

        drawContext.fill(hudX, hudY, hudX + layout.width, hudY + layout.height, bgColor())

        HudLayoutEngine.drawItems(
            drawContext, textRenderer, items,
            hudX, hudY, layout.width,
            fillBars = true,
            colorProvider = ::colorForSpeed
        )
    }

    private fun updateCachedStrings() {
        val s3d: Double = SpeedTracker.currentSpeed
        if (s3d != cachedSpeed) {
            cachedSpeed = s3d
            cachedSpeedStr = String.format(Locale.US, "%.3f", s3d)
        }
        val sH: Double = SpeedTracker.currentHorizontalSpeed
        if (sH != cachedHSpeed) {
            cachedHSpeed = sH
            cachedHSpeedStr = String.format(Locale.US, "%.3f", sH)
        }
        val absV: Double = kotlin.math.abs(SpeedTracker.currentVerticalSpeed)
        if (absV != cachedVSpeed) {
            cachedVSpeed = absV
            cachedVSpeedStr = String.format(Locale.US, "%.3f", absV)
        }
    }

    private fun buildCachedMetricItems(): List<HudLayoutEngine.MetricItem> {
        val items: MutableList<HudLayoutEngine.MetricItem> = ArrayList(3)
        val sV: Double = SpeedTracker.currentVerticalSpeed

        if (HudConfig.showSpeed || HudConfig.showSpeedBar) {
            items.add(HudLayoutEngine.MetricItem("Speed: $cachedSpeedStr b/s", "S", cachedSpeed, HudConfig.showSpeed, HudConfig.showSpeedBar))
        }
        if (HudConfig.showHSpeed || HudConfig.showHSpeedBar) {
            items.add(HudLayoutEngine.MetricItem("H-Speed: $cachedHSpeedStr b/s", "H", cachedHSpeed, HudConfig.showHSpeed, HudConfig.showHSpeedBar))
        }
        if (HudConfig.showVSpeed || HudConfig.showVSpeedBar) {
            val dir: String = if (sV >= 0) "↑" else "↓"
            items.add(HudLayoutEngine.MetricItem("V-Speed: $dir $cachedVSpeedStr b/s", "V$dir", cachedVSpeed, HudConfig.showVSpeed, HudConfig.showVSpeedBar))
        }
        return items
    }

    private fun bgColor(): Int {
        val alpha: Int = (HudConfig.bgOpacity * 255 / 100).coerceIn(0, 255)
        return (alpha shl 24) or 0x000000
    }

    private fun colorForSpeed(speed: Double): Int {
        val theme: String = HudConfig.colorTheme
        val (slow, mid, fast) = themeColors(theme)
        return when {
            speed < SPEED_SLOW_THRESHOLD -> slow
            speed < SPEED_FAST_THRESHOLD -> mid
            else -> fast
        }
    }

    private val DEFAULT_COLORS: Triple<Int, Int, Int> = Triple(0xFFFFFFFF.toInt(), 0xFFFFFF00.toInt(), 0xFFFF4444.toInt())

    private val THEME_COLORS: Map<String, Triple<Int, Int, Int>> = mapOf(
        "mono"   to Triple(0xFFAAAAAA.toInt(), 0xFFCCCCCC.toInt(), 0xFFFFFFFF.toInt()),
        "ocean"  to Triple(0xFF00DDDD.toInt(), 0xFF4488FF.toInt(), 0xFFAA44FF.toInt()),
        "neon"   to Triple(0xFF00FF88.toInt(), 0xFFFF44AA.toInt(), 0xFFFF00FF.toInt()),
        "sunset" to Triple(0xFFFFCC88.toInt(), 0xFFFF8844.toInt(), 0xFFDD2244.toInt())
    )

    private fun themeColors(theme: String): Triple<Int, Int, Int> = THEME_COLORS[theme] ?: DEFAULT_COLORS
}
