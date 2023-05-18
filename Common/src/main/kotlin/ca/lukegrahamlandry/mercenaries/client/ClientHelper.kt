package ca.lukegrahamlandry.mercenaries.client

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen

object ClientHelper {
    val level
        get() = Minecraft.getInstance().level

    val player
        get() = Minecraft.getInstance().player

    fun setScreen(screen: () -> Screen) = Minecraft.getInstance().setScreen(screen.invoke())

    fun getEntity(networkId: Int) = level?.getEntity(networkId)
}
