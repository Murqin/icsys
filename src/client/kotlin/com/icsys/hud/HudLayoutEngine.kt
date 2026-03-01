package com.icsys.hud

import com.icsys.config.HudConfig
import com.icsys.speed.SpeedTracker
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import java.util.Locale

object HudLayoutEngine {

    data class MetricItem(
        val text: String,
        val label: String,
        val speed: Double,
        val showText: Boolean,
        val hasBar: Boolean
    )

    data class LayoutResult(
        val items: List<MetricItem>,
        val width: Int,
        val height: Int
    )

    fun buildMetricItems(): List<MetricItem> {
        val items: MutableList<MetricItem> = ArrayList(3)

        val s3d: Double = SpeedTracker.currentSpeed
        if (HudConfig.showSpeed || HudConfig.showSpeedBar) {
            items.add(
                MetricItem(
                    "Speed: ${String.format(Locale.US, "%.3f", s3d)} b/s",
                    "S", s3d, HudConfig.showSpeed, HudConfig.showSpeedBar
                )
            )
        }

        val sH: Double = SpeedTracker.currentHorizontalSpeed
        if (HudConfig.showHSpeed || HudConfig.showHSpeedBar) {
            items.add(
                MetricItem(
                    "H-Speed: ${String.format(Locale.US, "%.3f", sH)} b/s",
                    "H", sH, HudConfig.showHSpeed, HudConfig.showHSpeedBar
                )
            )
        }

        val sV: Double = SpeedTracker.currentVerticalSpeed
        val absV: Double = kotlin.math.abs(sV)
        if (HudConfig.showVSpeed || HudConfig.showVSpeedBar) {
            val dir: String = if (sV >= 0) "↑" else "↓"
            items.add(
                MetricItem(
                    "V-Speed: $dir ${String.format(Locale.US, "%.3f", absV)} b/s",
                    "V$dir", absV, HudConfig.showVSpeed, HudConfig.showVSpeedBar
                )
            )
        }

        return items
    }

    fun computeLayout(items: List<MetricItem>, textRenderer: TextRenderer): LayoutResult {
        if (items.isEmpty()) return LayoutResult(items, 0, 0)

        var maxTextWidth: Int = 0
        for (item in items) {
            if (item.showText) {
                val w: Int = textRenderer.getWidth(item.text)
                if (w > maxTextWidth) maxTextWidth = w
            }
        }
        val labelBarWidth: Int = HudConstants.BAR_MIN_WIDTH + HudConstants.LABEL_WIDTH + HudConstants.PADDING
        val width: Int = maxOf(maxTextWidth, labelBarWidth) + HudConstants.PADDING * 2

        var height: Int = HudConstants.PADDING
        for (item in items) {
            if (item.showText) height += HudConstants.LINE_HEIGHT
            if (item.hasBar) {
                if (item.showText) height += HudConstants.BAR_HEIGHT + 5
                else height += HudConstants.LINE_HEIGHT
            }
        }
        height += HudConstants.PADDING

        return LayoutResult(items, width, height)
    }

    fun drawItems(
        context: DrawContext,
        textRenderer: TextRenderer,
        items: List<MetricItem>,
        hudX: Int,
        hudY: Int,
        hudWidth: Int,
        fillBars: Boolean,
        colorProvider: (Double) -> Int
    ) {
        var yOffset: Int = hudY + HudConstants.PADDING
        for (item in items) {
            val color: Int = colorProvider(item.speed)

            if (item.showText) {
                val textWidth: Int = textRenderer.getWidth(item.text)
                val textX: Int = hudX + (hudWidth - textWidth) / 2
                context.drawTextWithShadow(textRenderer, item.text, textX, yOffset, color)
                yOffset += HudConstants.LINE_HEIGHT
            }

            if (item.hasBar) {
                if (!item.showText) {
                    context.drawTextWithShadow(textRenderer, item.label, hudX + HudConstants.PADDING, yOffset + 1, HudConstants.COLOR_LABEL)
                    val barX: Int = hudX + HudConstants.PADDING + HudConstants.LABEL_WIDTH
                    val barWidth: Int = hudWidth - HudConstants.PADDING * 2 - HudConstants.LABEL_WIDTH
                    val barY: Int = yOffset + (HudConstants.LINE_HEIGHT - HudConstants.BAR_HEIGHT) / 2
                    context.fill(barX, barY, barX + barWidth, barY + HudConstants.BAR_HEIGHT, HudConstants.COLOR_BAR_BG)
                    if (fillBars) {
                        val fill: Double = (item.speed / MAX_BAR_SPEED).coerceIn(0.0, 1.0)
                        val filledWidth: Int = (barWidth * fill).toInt()
                        if (filledWidth > 0) {
                            context.fill(barX, barY, barX + filledWidth, barY + HudConstants.BAR_HEIGHT, color)
                        }
                    }
                    yOffset += HudConstants.LINE_HEIGHT
                } else {
                    val barX: Int = hudX + HudConstants.PADDING
                    val barWidth: Int = hudWidth - HudConstants.PADDING * 2
                    context.fill(barX, yOffset, barX + barWidth, yOffset + HudConstants.BAR_HEIGHT, HudConstants.COLOR_BAR_BG)
                    if (fillBars) {
                        val fill: Double = (item.speed / MAX_BAR_SPEED).coerceIn(0.0, 1.0)
                        val filledWidth: Int = (barWidth * fill).toInt()
                        if (filledWidth > 0) {
                            context.fill(barX, yOffset, barX + filledWidth, yOffset + HudConstants.BAR_HEIGHT, color)
                        }
                    }
                    yOffset += HudConstants.BAR_HEIGHT + 5
                }
            }
        }
    }

    private const val MAX_BAR_SPEED: Double = 30.0
}
