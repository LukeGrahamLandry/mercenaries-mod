package ca.lukegrahamlandry.mercenaries

import net.minecraft.network.chat.Component

interface VersionApi {
    fun translatableText(key: String): Component
}

object VersionImpl: VersionApi {
    override fun translatableText(key: String): Component = Component.translatable(key)
}