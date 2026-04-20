package it.italiabounty.gui;

import it.italiabounty.ItaliaBounty;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class BountyGUI {

    private final ItaliaBounty plugin;

    public static final Map<UUID, Integer> currentPage = new HashMap<>();
    public static final Map<UUID, String> searchQuery = new HashMap<>();
    public static final Set<UUID> openGUI = new HashSet<>();

    public BountyGUI(ItaliaBounty plugin) {
        this.plugin = plugin;
    }

    public void openBountyList(Player player, int page, String search) {
        List<Map.Entry<UUID, Double>> list = search != null ?
                plugin.getBountyManager().searchBounties(search) :
                plugin.getBountyManager().getTopBounties();

        int itemsPerPage = 45;
        int totalPages = Math.max(1, (int) Math.ceil(list.size() / (double) itemsPerPage));
        page = Math.max(0, Math.min(page, totalPages - 1));

        String title;
        if (search != null) {
            title = ChatColor.RED + "Ricerca: " + search;
        } else {
            title = ChatColor.RED + "Taglie";
        }

        Inventory inv = Bukkit.createInventory(null, 54, title);

        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, list.size());

        for (int i = start; i < end; i++) {
            Map.Entry<UUID, Double> entry = list.get(i);
            UUID targetUUID = entry.getKey();
            double amount = entry.getValue();
            String name = plugin.getBountyManager().getPlayerName(targetUUID);

            // Testa del player come icona
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            if (skullMeta != null) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(targetUUID);
                skullMeta.setOwningPlayer(op);
                skullMeta.setDisplayName(ChatColor.RED + name);

                int rank = i + 1;
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Posizione: " + ChatColor.YELLOW + "#" + String.valueOf(rank));
                lore.add(ChatColor.GRAY + "Taglia: " + ChatColor.GREEN + "$" + String.valueOf(amount));
                lore.add("");
                lore.add(ChatColor.YELLOW + "Uccidilo per incassare!");
                lore.add(ChatColor.BLACK + targetUUID.toString());
                skullMeta.setLore(lore);
                skull.setItemMeta(skullMeta);
            }

            inv.setItem(i - start, skull);
        }

        // Bottone cerca
        ItemStack cerca = createItem(Material.COMPASS, ChatColor.YELLOW + "Cerca giocatore",
                Collections.singletonList(ChatColor.GRAY + "Cerca per nome"));
        inv.setItem(46, cerca);

        // Pagina precedente
        if (page > 0) {
            ItemStack prev = createItem(Material.ARROW, ChatColor.WHITE + "Pagina precedente",
                    Collections.singletonList(ChatColor.GRAY + "Pagina " + String.valueOf(page) + "/" + String.valueOf(totalPages)));
            inv.setItem(48, prev);
        }

        // Info pagina
        String pageStr = String.valueOf(page + 1) + "/" + String.valueOf(totalPages);
        String countStr = String.valueOf(list.size()) + " taglie attive";
        ItemStack info = createItem(Material.PAPER, ChatColor.WHITE + "Pagina " + pageStr,
                Collections.singletonList(ChatColor.GRAY + countStr));
        inv.setItem(49, info);

        // Pagina successiva
        if (page < totalPages - 1) {
            ItemStack next = createItem(Material.ARROW, ChatColor.WHITE + "Pagina successiva",
                    Collections.singletonList(ChatColor.GRAY + "Pagina " + String.valueOf(page + 2) + "/" + String.valueOf(totalPages)));
            inv.setItem(50, next);
        }

        // Chiudi
        ItemStack close = createItem(Material.BARRIER, ChatColor.RED + "Chiudi", Collections.emptyList());
        inv.setItem(53, close);

        openGUI.add(player.getUniqueId());
        currentPage.put(player.getUniqueId(), page);
        if (search != null) searchQuery.put(player.getUniqueId(), search);
        else searchQuery.remove(player.getUniqueId());

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
