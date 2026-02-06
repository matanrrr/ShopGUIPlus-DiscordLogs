package mr.matanr.shopGUIPlusDiscordLogs.commands;

import mr.matanr.shopGUIPlusDiscordLogs.ShopGUIPlusDiscordLogs;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ReloadCommand extends Command {
    private final ShopGUIPlusDiscordLogs plugin;

    public ReloadCommand(ShopGUIPlusDiscordLogs plugin) {
        super("shopguiplusdiscordlogs");
        this.plugin = plugin;
        setDescription("Reload plugin config");
        setUsage("/shopguiplusdiscordlogs reload");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("§cUsage: /shopguiplusdiscordlogs reload");
            return true;
        }

        if (!sender.hasPermission("shopguiplusdiscordlogs.reload")) {
            sender.sendMessage("§cNo permission!");
            return true;
        }

        this.plugin.reloadConfig();

        sender.sendMessage("§aShopGUIPlusDiscordLogs reloaded!");
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return List.of("reload");
        }
        return Collections.emptyList();
    }
}