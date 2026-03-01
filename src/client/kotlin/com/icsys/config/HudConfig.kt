package com.icsys.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

object HudConfig {

    interface HudConfigView {
        val posXPercent: Double
        val posYPercent: Double
        val showSpeed: Boolean
        val showHSpeed: Boolean
        val showVSpeed: Boolean
        val showSpeedBar: Boolean
        val showHSpeedBar: Boolean
        val showVSpeedBar: Boolean
        val smoothingWindow: Int
        val bgOpacity: Int
        val colorTheme: String
    }

    private val logger = LoggerFactory.getLogger("icsys-hud-config")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val configPath: Path = FabricLoader.getInstance().configDir.resolve("icsys-speed-hud.json")

    private var data: ConfigData = ConfigData()

    val view: HudConfigView get() = data

    val posXPercent: Double get() = data.posXPercent
    val posYPercent: Double get() = data.posYPercent
    val showSpeed: Boolean get() = data.showSpeed
    val showHSpeed: Boolean get() = data.showHSpeed
    val showVSpeed: Boolean get() = data.showVSpeed
    val showSpeedBar: Boolean get() = data.showSpeedBar
    val showHSpeedBar: Boolean get() = data.showHSpeedBar
    val showVSpeedBar: Boolean get() = data.showVSpeedBar
    val smoothingWindow: Int get() = data.smoothingWindow
    val bgOpacity: Int get() = data.bgOpacity
    val colorTheme: String get() = data.colorTheme

    fun toggleSpeed() { data.showSpeed = !data.showSpeed }
    fun toggleHSpeed() { data.showHSpeed = !data.showHSpeed }
    fun toggleVSpeed() { data.showVSpeed = !data.showVSpeed }
    fun toggleSpeedBar() { data.showSpeedBar = !data.showSpeedBar }
    fun toggleHSpeedBar() { data.showHSpeedBar = !data.showHSpeedBar }
    fun toggleVSpeedBar() { data.showVSpeedBar = !data.showVSpeedBar }

    private val smoothingPresets: List<Int> = listOf(1, 5, 10, 20, 40, 60)

    fun cycleSmoothingWindow(forward: Boolean) {
        val current: Int = data.smoothingWindow
        val idx: Int = smoothingPresets.indexOf(current)
        data.smoothingWindow = if (forward) {
            if (idx < 0 || idx >= smoothingPresets.lastIndex) smoothingPresets.first() else smoothingPresets[idx + 1]
        } else {
            if (idx <= 0) smoothingPresets.last() else smoothingPresets[idx - 1]
        }
    }

    fun adjustBgOpacity(step: Int) {
        val next: Int = data.bgOpacity + step
        data.bgOpacity = if (next > 100) 0 else if (next < 0) 100 else next
    }

    private val themes: List<String> = listOf("default", "mono", "ocean", "neon", "sunset")

    fun cycleColorTheme() {
        val idx: Int = themes.indexOf(data.colorTheme)
        data.colorTheme = if (idx < 0 || idx >= themes.lastIndex) themes.first() else themes[idx + 1]
    }

    fun setPosition(xPercent: Double, yPercent: Double) {
        data.posXPercent = xPercent.coerceIn(0.0, 1.0)
        data.posYPercent = yPercent.coerceIn(0.0, 1.0)
    }

    fun getScreenX(screenWidth: Int, hudWidth: Int): Int {
        return ((screenWidth - hudWidth) * data.posXPercent).toInt()
    }

    fun getScreenY(screenHeight: Int, hudHeight: Int): Int {
        return ((screenHeight - hudHeight) * data.posYPercent).toInt()
    }

    fun load() {
        if (!Files.exists(configPath)) {
            save()
            return
        }
        try {
            val json: String = Files.readString(configPath)
            data = gson.fromJson(json, ConfigData::class.java) ?: ConfigData()
            sanitize()
            logger.info("Config loaded: x={}%, y={}%", data.posXPercent, data.posYPercent)
        } catch (e: Exception) {
            logger.error("Failed to load config", e)
            data = ConfigData()
        }
    }

    private fun sanitize() {
        data.smoothingWindow = data.smoothingWindow.coerceIn(1, 60)
        data.bgOpacity = data.bgOpacity.coerceIn(0, 100)
        data.posXPercent = data.posXPercent.coerceIn(0.0, 1.0)
        data.posYPercent = data.posYPercent.coerceIn(0.0, 1.0)
        if (data.colorTheme !in themes) {
            data.colorTheme = "default"
        }
    }

    fun save() {
        try {
            Files.createDirectories(configPath.parent)
            val tmpPath: Path = configPath.resolveSibling(configPath.fileName.toString() + ".tmp")
            Files.writeString(tmpPath, gson.toJson(data))
            Files.move(tmpPath, configPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: Exception) {
            logger.error("Failed to save config", e)
        }
    }

    private data class ConfigData(
        override var posXPercent: Double = 0.0,
        override var posYPercent: Double = 0.0,
        override var showSpeed: Boolean = true,
        override var showHSpeed: Boolean = true,
        override var showVSpeed: Boolean = true,
        override var showSpeedBar: Boolean = false,
        override var showHSpeedBar: Boolean = false,
        override var showVSpeedBar: Boolean = false,
        override var smoothingWindow: Int = 20,
        override var bgOpacity: Int = 50,
        override var colorTheme: String = "default"
    ) : HudConfigView
}
