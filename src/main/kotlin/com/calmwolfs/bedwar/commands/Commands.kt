package com.calmwolfs.bedwar.commands

import com.calmwolfs.bedwar.BedWarMod
import com.calmwolfs.bedwar.commands.testcommands.CopyScoreboardCommand
import com.calmwolfs.bedwar.commands.testcommands.CopyTablistCommand
import com.calmwolfs.bedwar.config.gui.ConfigGuiManager
import com.calmwolfs.bedwar.config.gui.GuiEditorManager
import com.calmwolfs.bedwar.features.chat.ChatStatDisplay
import com.calmwolfs.bedwar.features.party.PartyCommands
import com.calmwolfs.bedwar.features.session.SessionDisplay
import com.calmwolfs.bedwar.utils.BedwarsStarUtils
import com.calmwolfs.bedwar.utils.PartyUtils
import net.minecraft.command.ICommandSender
import net.minecraftforge.client.ClientCommandHandler

object Commands {
    private val openConfig: (Array<String>) -> Unit = {
        if (it.isNotEmpty()) {
            if (it[0].lowercase() == "gui") {
                GuiEditorManager.openGuiPositionEditor()
            } else {
                ConfigGuiManager.openConfigGui(it.joinToString(" "))
            }
        } else {
            ConfigGuiManager.openConfigGui()
        }
    }

    fun init() {
        registerCommand("bw", openConfig)
        registerCommand("bedwar", openConfig)

        registerCommand("bws") { ChatStatDisplay.command(it) }

        registerCommand("bwresettracker") { SessionDisplay.resetTracker() }

        registerCommand("bwcopyerror") { CopyErrorCommand.command(it) }
        registerCommand("bwcopyscoreboard") { CopyScoreboardCommand.command(it) }
        registerCommand("bwcopytablist") { CopyTablistCommand.command(it) }

        registerCommand("bwpartylist") { PartyUtils.listMembers() }
        registerCommand("bwexp") { BedwarsStarUtils.testExperience(it) }

        registerCommand("pko") { PartyCommands.kickOffline() }
        registerCommand("pw") { PartyCommands.warp() }
        registerCommand("pk") { PartyCommands.kick(it) }
        registerCommand("pt") { PartyCommands.transfer(it) }
        registerCommand("pp") { PartyCommands.promote(it) }

        registerCommand("bwupdaterepo") { BedWarMod.repo.updateRepo() }
        registerCommand("bwreloadrepo") { BedWarMod.repo.reloadLocalRepo() }
        registerCommand("bwrepostatus") { BedWarMod.repo.displayRepoStatus() }
    }

    private fun registerCommand(name: String, function: (Array<String>) -> Unit) {
        ClientCommandHandler.instance.registerCommand(SimpleCommand(name, createCommand(function)))
    }

    private fun createCommand(function: (Array<String>) -> Unit) = object : SimpleCommand.ProcessCommandRunnable() {
        override fun processCommand(sender: ICommandSender?, args: Array<String>?) {
            if (args != null) function(args.asList().toTypedArray())
        }
    }
}