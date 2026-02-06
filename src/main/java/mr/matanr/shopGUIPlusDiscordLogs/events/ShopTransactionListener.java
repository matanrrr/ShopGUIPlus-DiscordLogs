package mr.matanr.shopGUIPlusDiscordLogs.events;

import mr.matanr.shopGUIPlusDiscordLogs.ShopGUIPlusDiscordLogs;
import mr.matanr.shopGUIPlusDiscordLogs.utils.Webhook;
import net.brcdev.shopgui.event.ShopPostTransactionEvent;
import net.brcdev.shopgui.shop.ShopTransactionResult;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ShopTransactionListener implements Listener {

    private final ShopGUIPlusDiscordLogs plugin;

    public ShopTransactionListener(ShopGUIPlusDiscordLogs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShopTransaction(ShopPostTransactionEvent e) {
        FileConfiguration config = plugin.getConfig();
        ShopTransactionResult result = e.getResult();
        String type = result.getShopAction().toString();
        boolean isBuy = type.equalsIgnoreCase("BUY");

        String path = isBuy ? "webhook-url.buy" : "webhook-url.sell";
        String webhookUrl = config.getString(path);
        if (!isValid(webhookUrl)) webhookUrl = config.getString("webhook-url.default");
        if (!isValid(webhookUrl)) return;

        String player = result.getPlayer().getName();
        String shopName = "Shop";
        if (result.getShopItem() != null && result.getShopItem().getShop() != null) {
            shopName = result.getShopItem().getShop().getName() != null
                    ? result.getShopItem().getShop().getName()
                    : result.getShopItem().getShop().getId();
        }

        double price = result.getPrice();
        int amount = result.getAmount();

        ItemStack item = result.getShopItem().getItem();
        String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                ? ChatColor.stripColor(item.getItemMeta().getDisplayName())
                : item.getType().toString();

        String sectionPath = isBuy ? "embed.buy" : "embed.sell";
        ConfigurationSection section = config.getConfigurationSection(sectionPath);
        if (section == null) return;

        Webhook webhook = new Webhook(webhookUrl);
        webhook.setUsername(config.getString("username", "Shop"));
        if (isValid(config.getString("avatar-url"))) webhook.setAvatarUrl(config.getString("avatar-url"));

        Webhook.EmbedObject embed = new Webhook.EmbedObject();

        if (section.getBoolean("title.enabled", true)) {
            embed.setTitle(format(section.getString("title.content"), player, type, itemName, amount, price, shopName));
        }

        if (section.getBoolean("description.enabled", true)) {
            embed.setDescription(format(section.getString("description.content"), player, type, itemName, amount, price, shopName));
        }

        try {
            embed.setColor(Color.decode(section.getString("color", isBuy ? "#00FF00" : "#FF0000")));
        } catch (NumberFormatException ex) {
            embed.setColor(Color.BLACK);
        }

        if (section.getBoolean("author.enabled", false)) {
            embed.setAuthor(
                    format(section.getString("author.name"), player, type, itemName, amount, price, shopName),
                    format(section.getString("author.url"), player, type, itemName, amount, price, shopName),
                    format(section.getString("author.icon"), player, type, itemName, amount, price, shopName)
            );
        }

        if (section.getBoolean("footer.enabled", false)) {
            embed.setFooter(
                    format(section.getString("footer.text"), player, type, itemName, amount, price, shopName),
                    format(section.getString("footer.icon"), player, type, itemName, amount, price, shopName)
            );
        }

        if (section.getBoolean("thumbnail.enabled", false)) {
            embed.setThumbnail(section.getString("thumbnail.url"));
        }

        if (section.getBoolean("image.enabled", false)) {
            embed.setImage(section.getString("image.url"));
        }

        if (section.contains("fields")) {
            List<Map<?, ?>> fields = section.getMapList("fields");
            for (Map<?, ?> field : fields) {
                Object enabledObj = field.get("enabled");
                boolean enabled = enabledObj instanceof Boolean ? (Boolean) enabledObj : true;

                if (!enabled)
                    continue;

                String nameRaw = field.containsKey("name") ? field.get("name").toString() : "";
                String valueRaw = field.containsKey("value") ? field.get("value").toString() : "";

                String name = format(nameRaw, player, type, itemName, amount, price, shopName);
                String value = format(valueRaw, player, type, itemName, amount, price, shopName);

                if (!name.isEmpty() && !value.isEmpty()) {
                    embed.addField(name, value, true);
                }
            }
        }

        webhook.addEmbed(embed);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                webhook.send();
            } catch (IOException ex) {
                plugin.getLogger().warning("Failed to send webhook: " + ex.getMessage());
            }
        });
    }

    private boolean isValid(String str) {
        return str != null && !str.isEmpty() && !str.equals("WEBHOOK_URL");
    }

    private String format(String text, String player, String action, String item, int amount, double price, String shop) {
        if (text == null) return "";
        return text
                .replace("{player}", player)
                .replace("{action}", action)
                .replace("{item}", item)
                .replace("{amount}", String.valueOf(amount))
                .replace("{price}", String.format("%.2f", price))
                .replace("{shop}", shop);
    }
}