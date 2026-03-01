package com.icsys.screen

import com.icsys.config.HudConfig
import com.icsys.hud.HudConstants
import com.icsys.hud.HudLayoutEngine
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.input.KeyInput
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import java.util.Locale
import kotlin.math.abs

class HudConfigScreen : Screen(
    net.minecraft.client.MinecraftClient.getInstance(),
    net.minecraft.client.MinecraftClient.getInstance().textRenderer,
    Text.translatable("screen.icsys.hud_config")
) {

    private var dragging: Boolean = false
    private var dragOffsetX: Int = 0
    private var dragOffsetY: Int = 0

    private var hudX: Int = 0
    private var hudY: Int = 0
    private var hudWidth: Int = 0
    private var hudHeight: Int = 0

    private var configDirty: Boolean = false

    companion object {
        private const val SNAP_THRESHOLD: Int = 8
        private const val NUDGE_NORMAL: Int = 1
        private const val NUDGE_FAST: Int = 10

        private const val COLOR_BG: Int = 0x80000000.toInt()
        private const val COLOR_BORDER: Int = 0xFFFFFFFF.toInt()
        private const val COLOR_BORDER_DRAG: Int = 0xFF00FF00.toInt()
        private const val COLOR_TEXT: Int = 0xFFFFFFFF.toInt()
        private const val COLOR_HINT: Int = 0x80FFFFFF.toInt()
        private const val COLOR_GUIDE_CENTER: Int = 0x60FF6600
        private const val COLOR_GUIDE_SNAP: Int = 0x8000AAFF.toInt()
    }

    override fun init() {
        super.init()
        recalculateHudDimensions()
        hudX = HudConfig.getScreenX(width, hudWidth)
        hudY = HudConfig.getScreenY(height, hudHeight)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)

        if (dragging) {
            hudX = (mouseX - dragOffsetX).coerceIn(0, width - hudWidth)
            hudY = (mouseY - dragOffsetY).coerceIn(0, height - hudHeight)
            applySnapping()
            drawGuideLines(context)
        }

        val borderColor: Int = if (dragging) COLOR_BORDER_DRAG else COLOR_BORDER
        drawHudBox(context, borderColor)

        drawHints(context)
        drawToggleStatus(context)
    }

    private fun drawHints(context: DrawContext) {
        val hints: List<String> = if (dragging) {
            listOf("Release to place")
        } else {
            listOf(
                "Drag / WASD / Arrows: move (Ctrl: 10px)",
                "[1/2/3] Metrics | Ctrl+[1/2/3] Bars",
                "R: Smoothing | O/Ctrl+O: Opacity | T: Theme | ESC: save"
            )
        }
        val startY: Int = height - 12 - hints.size * HudConstants.LINE_HEIGHT
        hints.forEachIndexed { i, text ->
            val w: Int = textRenderer.getWidth(text)
            context.drawTextWithShadow(textRenderer, text, (width - w) / 2, startY + i * HudConstants.LINE_HEIGHT, COLOR_HINT)
        }
    }

    private fun drawToggleStatus(context: DrawContext) {
        val y0: Int = height - 118
        val x0: Int = 8
        val on: Int = COLOR_TEXT
        val off: Int = 0xFF555555.toInt()

        context.drawTextWithShadow(textRenderer, "[1] Speed: ${if (HudConfig.showSpeed) "ON" else "OFF"}", x0, y0, if (HudConfig.showSpeed) on else off)
        context.drawTextWithShadow(textRenderer, "[2] H-Speed: ${if (HudConfig.showHSpeed) "ON" else "OFF"}", x0, y0 + HudConstants.LINE_HEIGHT, if (HudConfig.showHSpeed) on else off)
        context.drawTextWithShadow(textRenderer, "[3] V-Speed: ${if (HudConfig.showVSpeed) "ON" else "OFF"}", x0, y0 + HudConstants.LINE_HEIGHT * 2, if (HudConfig.showVSpeed) on else off)
        context.drawTextWithShadow(textRenderer, "Ctrl+1 Bar: ${if (HudConfig.showSpeedBar) "ON" else "OFF"}", x0, y0 + HudConstants.LINE_HEIGHT * 3, if (HudConfig.showSpeedBar) on else off)
        context.drawTextWithShadow(textRenderer, "Ctrl+2 Bar: ${if (HudConfig.showHSpeedBar) "ON" else "OFF"}", x0, y0 + HudConstants.LINE_HEIGHT * 4, if (HudConfig.showHSpeedBar) on else off)
        context.drawTextWithShadow(textRenderer, "Ctrl+3 Bar: ${if (HudConfig.showVSpeedBar) "ON" else "OFF"}", x0, y0 + HudConstants.LINE_HEIGHT * 5, if (HudConfig.showVSpeedBar) on else off)
        val windowTicks: Int = HudConfig.smoothingWindow
        val windowSec: String = String.format(Locale.US, "%.1f", windowTicks / 20.0)
        context.drawTextWithShadow(textRenderer, "[R] Smoothing: ${windowTicks}t (${windowSec}s)", x0, y0 + HudConstants.LINE_HEIGHT * 7, on)
        context.drawTextWithShadow(textRenderer, "[O] Opacity: ${HudConfig.bgOpacity}%", x0, y0 + HudConstants.LINE_HEIGHT * 8, on)
        context.drawTextWithShadow(textRenderer, "[T] Theme: ${HudConfig.colorTheme}", x0, y0 + HudConstants.LINE_HEIGHT * 9, on)
    }

    override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
        val mx: Int = click.x().toInt()
        val my: Int = click.y().toInt()

        if (click.button() == 0 && isInsideHud(mx, my)) {
            dragging = true
            dragOffsetX = mx - hudX
            dragOffsetY = my - hudY
            return true
        }
        return super.mouseClicked(click, doubled)
    }

    override fun mouseReleased(click: Click): Boolean {
        if (click.button() == 0 && dragging) {
            dragging = false
            savePosition()
            return true
        }
        return super.mouseReleased(click)
    }

    override fun keyPressed(keyInput: KeyInput): Boolean {
        val key: Int = keyInput.key()
        val ctrl: Boolean = keyInput.modifiers() and GLFW.GLFW_MOD_CONTROL != 0
        val step: Int = if (ctrl) NUDGE_FAST else NUDGE_NORMAL

        when (key) {
            GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A -> {
                hudX = (hudX - step).coerceAtLeast(0)
                configDirty = true
                return true
            }
            GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D -> {
                hudX = (hudX + step).coerceAtMost(width - hudWidth)
                configDirty = true
                return true
            }
            GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_W -> {
                hudY = (hudY - step).coerceAtLeast(0)
                configDirty = true
                return true
            }
            GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_S -> {
                hudY = (hudY + step).coerceAtMost(height - hudHeight)
                configDirty = true
                return true
            }
            GLFW.GLFW_KEY_1 -> {
                if (ctrl) HudConfig.toggleSpeedBar() else HudConfig.toggleSpeed()
                configDirty = true
                recalculateHudDimensions()
                return true
            }
            GLFW.GLFW_KEY_2 -> {
                if (ctrl) HudConfig.toggleHSpeedBar() else HudConfig.toggleHSpeed()
                configDirty = true
                recalculateHudDimensions()
                return true
            }
            GLFW.GLFW_KEY_3 -> {
                if (ctrl) HudConfig.toggleVSpeedBar() else HudConfig.toggleVSpeed()
                configDirty = true
                recalculateHudDimensions()
                return true
            }
            GLFW.GLFW_KEY_R -> {
                HudConfig.cycleSmoothingWindow(true)
                configDirty = true
                return true
            }
            GLFW.GLFW_KEY_O -> {
                HudConfig.adjustBgOpacity(if (ctrl) 1 else 5)
                configDirty = true
                return true
            }
            GLFW.GLFW_KEY_T -> {
                HudConfig.cycleColorTheme()
                configDirty = true
                return true
            }
        }
        return super.keyPressed(keyInput)
    }

    override fun close() {
        if (configDirty) {
            savePosition()
        }
        super.close()
    }

    override fun shouldPause(): Boolean = false

    private fun drawHudBox(context: DrawContext, borderColor: Int) {
        if (hudWidth == 0 || hudHeight == 0) return

        context.fill(hudX, hudY, hudX + hudWidth, hudY + hudHeight, COLOR_BG)
        context.drawHorizontalLine(hudX, hudX + hudWidth - 1, hudY, borderColor)
        context.drawHorizontalLine(hudX, hudX + hudWidth - 1, hudY + hudHeight - 1, borderColor)
        context.drawVerticalLine(hudX, hudY, hudY + hudHeight - 1, borderColor)
        context.drawVerticalLine(hudX + hudWidth - 1, hudY, hudY + hudHeight - 1, borderColor)

        val items: List<HudLayoutEngine.MetricItem> = HudLayoutEngine.buildMetricItems()
        HudLayoutEngine.drawItems(
            context, textRenderer, items,
            hudX, hudY, hudWidth,
            fillBars = false,
            colorProvider = { COLOR_TEXT }
        )
    }

    private fun drawGuideLines(context: DrawContext) {
        val centerX: Int = width / 2
        val centerY: Int = height / 2
        val hudCenterX: Int = hudX + hudWidth / 2
        val hudCenterY: Int = hudY + hudHeight / 2

        context.drawVerticalLine(centerX, 0, height, COLOR_GUIDE_CENTER)
        context.drawHorizontalLine(0, width, centerY, COLOR_GUIDE_CENTER)

        if (abs(hudCenterX - centerX) <= SNAP_THRESHOLD) {
            context.drawVerticalLine(hudCenterX, 0, height, COLOR_GUIDE_SNAP)
        }
        if (abs(hudCenterY - centerY) <= SNAP_THRESHOLD) {
            context.drawHorizontalLine(0, width, hudCenterY, COLOR_GUIDE_SNAP)
        }
        if (abs(hudX) <= SNAP_THRESHOLD) {
            context.drawVerticalLine(0, 0, height, COLOR_GUIDE_SNAP)
        }
        if (abs(hudX + hudWidth - width) <= SNAP_THRESHOLD) {
            context.drawVerticalLine(width - 1, 0, height, COLOR_GUIDE_SNAP)
        }
        if (abs(hudY) <= SNAP_THRESHOLD) {
            context.drawHorizontalLine(0, width, 0, COLOR_GUIDE_SNAP)
        }
        if (abs(hudY + hudHeight - height) <= SNAP_THRESHOLD) {
            context.drawHorizontalLine(0, width, height - 1, COLOR_GUIDE_SNAP)
        }
    }

    private fun applySnapping() {
        val centerX: Int = width / 2
        val centerY: Int = height / 2
        val hudCenterX: Int = hudX + hudWidth / 2
        val hudCenterY: Int = hudY + hudHeight / 2

        if (abs(hudCenterX - centerX) <= SNAP_THRESHOLD) hudX = centerX - hudWidth / 2
        if (abs(hudCenterY - centerY) <= SNAP_THRESHOLD) hudY = centerY - hudHeight / 2
        if (abs(hudX) <= SNAP_THRESHOLD) hudX = 0
        if (abs(hudX + hudWidth - width) <= SNAP_THRESHOLD) hudX = width - hudWidth
        if (abs(hudY) <= SNAP_THRESHOLD) hudY = 0
        if (abs(hudY + hudHeight - height) <= SNAP_THRESHOLD) hudY = height - hudHeight
    }

    private fun isInsideHud(mx: Int, my: Int): Boolean {
        return mx in hudX..(hudX + hudWidth) && my in hudY..(hudY + hudHeight)
    }

    private fun recalculateHudDimensions() {
        val items: List<HudLayoutEngine.MetricItem> = HudLayoutEngine.buildMetricItems()
        val layout: HudLayoutEngine.LayoutResult = HudLayoutEngine.computeLayout(items, textRenderer)
        hudWidth = layout.width
        hudHeight = layout.height
    }

    private fun savePosition() {
        if (hudWidth <= 0 || hudHeight <= 0) return
        if (width <= hudWidth || height <= hudHeight) return
        val xPercent: Double = hudX.toDouble() / (width - hudWidth)
        val yPercent: Double = hudY.toDouble() / (height - hudHeight)
        HudConfig.setPosition(xPercent, yPercent)
        HudConfig.save()
    }
}
