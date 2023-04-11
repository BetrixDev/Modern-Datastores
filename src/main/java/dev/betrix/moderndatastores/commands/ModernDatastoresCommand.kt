package dev.betrix.moderndatastores.commands

import dev.betrix.moderndatastores.ModernDatastores
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class ModernDatastoresCommand(private val datastores: ModernDatastores) : CommandExecutor, TabCompleter {

    private val prefix = datastores.prefix

    private fun handleListCommand(sender: Player, args: List<String>) {
        if (args.isEmpty()) {
            // Line breaks are positioned in a way to give a 1 line vertical padding between the list and other text
            val message = Component.text().append(prefix)
                    .append(Component.text()
                            .content(
                                    " Below is a list of all plugins using Datastores, click one to view its stores.\n\n")
                            .color(NamedTextColor.GOLD))

            for (registeredPlugin in ModernDatastores.registry.registeredStores.keys.toList()) {
                val miniMessage = " <dark_gray>- <dark_aqua>[<aqua>$registeredPlugin</aqua>]</dark_aqua>\n"

                message.append(MiniMessage.miniMessage().deserialize(miniMessage)
                        .clickEvent(ClickEvent.runCommand("/moderndatastores list $registeredPlugin")))
                        .hoverEvent(
                                HoverEvent.showText(Component.text("Click to show stores for $registeredPlugin")
                                        .color(NamedTextColor.AQUA)))
            }

            sender.sendMessage(message.build())
        } else {
            val stores = ModernDatastores.registry.registeredStores[args[0]]

            if (stores != null) {
                val message = Component.text().append(prefix.append(MiniMessage.miniMessage()
                        .deserialize(
                                " <gold>Below is a list of all stores used in <yellow>${args[0]}</yellow>.</gold>\n\n")))

                for (store in stores.values) {
                    message.append(MiniMessage.miniMessage().deserialize(
                            " <dark_gray>- <dark_aqua>[<aqua>${store.name}</aqua>]</dark_aqua><dark_gray>: <yellow>${store.description}\n"))
                }

                sender.sendMessage(message)
            } else {
                val validPlugin = Bukkit.getPluginManager().getPlugin(args[0])

                if (validPlugin == null) {
                    val message = prefix.append(Component.text(" ${args[0]} is not a valid plugin name. Please use ")
                            .color(NamedTextColor.RED)
                            .append(Component.text("/moderndatastores list ").color(NamedTextColor.AQUA).clickEvent(
                                    ClickEvent.runCommand("/moderndatastores list")).hoverEvent(HoverEvent.showText(
                                    Component.text("Click to run the command").color(NamedTextColor.AQUA))))
                            .append(Component.text("for a list of valid plugins.").color(NamedTextColor.RED)))


                    sender.sendMessage(message)
                } else {
                    val message = Component.text("${validPlugin.name} does not have any registered stores.")
                            .color(NamedTextColor.RED)

                    sender.sendMessage(message)
                }
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        if (args[0] == "list") {
            handleListCommand(sender, args.toList().subList(1, args.size))
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<String>): List<String>? {
        val listArgs = args.toList()

        if (listArgs.size == 1) {
            return listOf("list")
        }

        if (listArgs.contains("list")) {
            return ModernDatastores.registry.registeredStores.keys.toList()
        }

        return null
    }
}