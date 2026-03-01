package com.icsys.config

import com.icsys.screen.HudConfigScreen
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW

object ModConfig {

    private val MOD_CATEGORY: KeyBinding.Category = KeyBinding.Category.create(
        Identifier.of("icsys", "category")
    )

    var hudEnabled: Boolean = true
        private set

    private val toggleKeyBinding: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.icsys.toggle_hud",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            MOD_CATEGORY
        )
    )

    private val configKeyBinding: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.icsys.open_config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            MOD_CATEGORY
        )
    )

    fun registerTickHandler() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.player == null) return@register

            while (toggleKeyBinding.wasPressed()) {
                hudEnabled = !hudEnabled
            }

            while (configKeyBinding.wasPressed()) {
                client.setScreen(HudConfigScreen())
            }
        }
    }
}
