package it.italiabounty.listeners;

import it.italiabounty.ItaliaBounty;
import it.italiabounty.gui.BountyGUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BountyListener implements Listener {

    private final ItaliaBounty plugin;
    public static final Set<UUID> waitingSearch = new HashSet<>();

    public BountyListener(ItaliaBounty plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) return;
        if (killer.equals(victim)) return;

        UUID victimUUID = victim.getUniqueId();
        if (!plugin.getBountyManager().hasBounty(victimUUID)) return;

        double amount = plugin.getBountyManager().getBounty(victimUUID);
        plugin.getEconomy().depositPlayer(killer, amount);
        plugin.getBountyManager().removeBounty(victimUUID);

        // Annuncio globale
        String msg = ChatColor.RED + "[Taglie] " + ChatColor.YELLOW + killer.getName() +
                ChatColor.WHITE + " ha eliminato " + ChatColor.RED + victim.getName() +
                ChatColor.WHITE + " e incassato " + ChatColor.GREEN + "$" + String.valueOf(amount) + "!";
        plugin.getServer().broadcastMessage(msg);

        killer.sendMessage(ChatColor.GREEN + "Hai incassato la taglia di $" + String.valueOf(amount) + "!");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        if (!waitingSearch.contains(player.getUniqueId())) return;
        e.setCancelled(true);
        waitingSearch.remove(player.getUniqueId());
        String query = e.getMessage();
        plugin.getServer().getScheduler().runTask(plugin, () ->
                plugin.getBountyGUI().openBountyList(player, 0, query));
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();
        if (!BountyGUI.openGUI.contains(uuid)) return;

        e.setCancelled(true);
        if (e.getCurrentItem() == null) return;

        int slot = e.getSlot();

        if (slot == 53) { // Chiudi
            player.closeInventory();
            return;
        }

        if (slot == 46) { // Cerca
            player.closeInventory();
            BountyGUI.openGUI.remove(uuid);
            waitingSearch.add(uuid);
            player.sendMessage(ChatColor.YELLOW + "Scrivi in chat il nome del giocatore da cercare:");
            return;
        }

        if (slot == 48) { // Pagina precedente
            int page = BountyGUI.currentPage.getOrDefault(uuid, 0);
            String search = BountyGUI.searchQuery.get(uuid);
            player.closeInventory();
            plugin.getBountyGUI().openBountyList(player, page - 1, search);
            return;
        }

        if (slot == 50) { // Pagina successiva
            int page = BountyGUI.currentPage.getOrDefault(uuid, 0);
            String search = BountyGUI.searchQuery.get(uuid);
            player.closeInventory();
            plugin.getBountyGUI().openBountyList(player, page + 1, search);
            return;
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;
        BountyGUI.openGUI.remove(player.getUniqueId());
        BountyGUI.currentPage.remove(player.getUniqueId());
    }
}
