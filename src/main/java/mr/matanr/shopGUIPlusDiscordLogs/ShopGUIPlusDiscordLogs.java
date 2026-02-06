package mr.matanr.shopGUIPlusDiscordLogs;

import mr.matanr.shopGUIPlusDiscordLogs.commands.ReloadCommand;
import mr.matanr.shopGUIPlusDiscordLogs.events.ShopTransactionListener;

import org.bukkit.plugin.java.JavaPlugin;

public final class ShopGUIPlusDiscordLogs extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.getServer().getCommandMap().register("shopguiplusdiscordlogs", new ReloadCommand(this));
        this.getServer().getPluginManager().registerEvents(new ShopTransactionListener(this), this);
        this.getLogger().info("ShopGUIPlus Discord Logs has been enabled!");
    }
}
