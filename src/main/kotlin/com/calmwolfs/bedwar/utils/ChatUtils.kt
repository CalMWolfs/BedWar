package com.calmwolfs.bedwar.utils

import com.calmwolfs.bedwar.BedWarMod
import com.calmwolfs.bedwar.mixins.transformers.AccessorChatComponentText
import com.calmwolfs.bedwar.utils.StringUtils.matchMatcher
import com.calmwolfs.bedwar.utils.StringUtils.unformat
import com.calmwolfs.bedwar.utils.computer.SimpleTimeMark
import net.minecraft.client.Minecraft
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import java.util.function.Predicate
import kotlin.time.Duration.Companion.seconds

object ChatUtils {
    private val playerChatPattern = ".*§[f7]: .*".toPattern()
    private val chatUsernamePattern = "^(?:\\[\\d+] )?(?:\\S )?(?:\\[\\w.+] )?(?<username>\\w+)(?: \\[.+?])?\$".toPattern()

    private var lastMessageSent = SimpleTimeMark.farPast()

    fun String.getPlayerName(): String {
        if (!playerChatPattern.matcher(this).matches()) return "-"

        var username = this.unformat().split(":")[0]

        if (username.contains(">")) {
            username = username.substring(username.indexOf('>') + 1).trim()
        }
        username = username.removePrefix("From ")
        username = username.removePrefix("To ")

        chatUsernamePattern.matchMatcher(username) {
            return group("username")
        }
        return "-"
    }

    private fun sendMessageToServer(message: String) {
        if (lastMessageSent.passedSince() > 2.seconds) {
            lastMessageSent = SimpleTimeMark.now()
            val thePlayer = Minecraft.getMinecraft().thePlayer
            thePlayer.sendChatMessage(message)
        }
    }

    fun chat(message: String) {
        internalChat(message)
    }

    fun sendCommandToServer(command: String) {
        sendMessageToServer("/$command")
    }

    fun internalChat(message: String): Boolean {
        val minecraft = Minecraft.getMinecraft()
        if (minecraft == null) {
            BedWarMod.consoleLog(message.unformat())
            return false
        }

        val thePlayer = minecraft.thePlayer
        if (thePlayer == null) {
            BedWarMod.consoleLog(message.unformat())
            return false
        }

        messagePlayer(ChatComponentText(message))
        return true
    }

    fun clickableChat(message: String, command: String) {
        val text = ChatComponentText(message)
        val fullCommand = "/" + command.removePrefix("/")
        text.chatStyle.chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, fullCommand)
        text.chatStyle.chatHoverEvent =
            HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText("§eExecute $fullCommand"))
        messagePlayer(text)
    }

    fun messagePlayer(message: ChatComponentText) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(message)
    }

    fun makeClickableChat(message: String, command: String, description: String): ChatComponentText {
        val textComponent = ChatComponentText(message)
        val fullCommand = "/" + command.removePrefix("/")
        textComponent.chatStyle.chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, fullCommand)
        textComponent.chatStyle.chatHoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(description))

        return textComponent
    }

    fun makeHoverChat(message: String, description: String): ChatComponentText {
        val textComponent = ChatComponentText(message)
        textComponent.chatStyle.chatHoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(description))

        return textComponent
    }

    fun makeMultiLineHoverChat(message: String, description: List<String>): ChatComponentText {
        val textComponent = ChatComponentText(message)
        val hoverText = ChatComponentText("")
        val lastIndex = description.size - 1

        for ((index, line) in description.withIndex()) {
            hoverText.appendSibling(ChatComponentText(line))
            if (index < lastIndex) {
                hoverText.appendSibling(ChatComponentText("\n"))
            }
        }

        textComponent.chatStyle.chatHoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)
        return textComponent
    }

    private fun modifyFirstChatComponent(chatComponent: IChatComponent, action: Predicate<IChatComponent>): Boolean {
        if (action.test(chatComponent)) {
            return true
        }
        for (sibling in chatComponent.siblings) {
            if (modifyFirstChatComponent(sibling, action)) {
                return true
            }
        }
        return false
    }

    fun replaceFirstChatText(chatComponent: IChatComponent, toReplace: String, replacement: String): IChatComponent {
        modifyFirstChatComponent(chatComponent) { component ->
            if (component is ChatComponentText) {
                component as AccessorChatComponentText
                if (component.bedwar_text().contains(toReplace)) {
                    component.bedwar_setText(component.bedwar_text().replace(toReplace, replacement))
                    return@modifyFirstChatComponent true
                }
                return@modifyFirstChatComponent false
            }
            return@modifyFirstChatComponent false
        }
        return chatComponent
    }
}