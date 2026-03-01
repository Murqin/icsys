package com.icsys

import com.icsys.config.HudConfig
import com.icsys.config.ModConfig
import com.icsys.hud.SpeedHudRenderer
import com.icsys.speed.SpeedTracker
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import org.slf4j.LoggerFactory

object ICanShowYourSpeedClient : ClientModInitializer {

    private val logger = LoggerFactory.getLogger("icsys-client")

    override fun onInitializeClient() {
        logger.info("I Can Show Your Speed — client initializing")

        HudConfig.load()
        ModConfig.registerTickHandler()
        SpeedHudRenderer.register()

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            SpeedTracker.tick(client.player)
        }

        logger.info("I Can Show Your Speed — client initialized")
    }
}