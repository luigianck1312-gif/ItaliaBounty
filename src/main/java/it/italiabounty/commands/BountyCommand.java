package it.italiabounty.commands;

import it.italiabounty.ItaliaBounty;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BountyCommand implements CommandExecutor {

    private final ItaliaBounty plugin;

    public BountyCommand(ItaliaBounty plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo i giocatori possono usare questo comando!");
            return true;
        }

        if (args.length == 0) {
            plugin.getBountyGUI().openBountyList(player, 0, null);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "set" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Uso: /bounty set <player> <importo>");
                    return true;
                }

                String targetName = args[1];
                double amount;
                try {
                    amount = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Importo non valido!");
                    return true;
                }

                if (amount <= 0) {
                    player.sendMessage(ChatColor.RED + "L'importo deve essere maggiore di 0!");
                    return true;
                }

                // Non puoi mettere taglia su te stesso
                if (targetName.equalsIgnoreCase(player.getName())) {
                    player.sendMessage(ChatColor.RED + "Non puoi mettere una taglia su te stesso!");
                    return true;
                }

                // Controlla se il player ha abbastanza soldi
                if (!plugin.getEconomy().has(player, amount)) {
                    player.sendMessage(ChatColor.RED + "Non hai abbastanza soldi!");
                    return true;
                }

                // Cerca il player
                Player target = plugin.getServer().getPlayer(targetName);
                if (target == null) {
                    // Prova con offline player
                    org.bukkit.OfflinePlayer offlineTarget = plugin.getServer().getOfflinePlayer(targetName);
                    if (!offlineTarget.hasPlayedBefore()) {
                        player.sendMessage(ChatColor.RED + "Giocatore non trovato!");
                        return true;
                    }
                    // Preleva soldi
                    plugin.getEconomy().withdrawPlayer(player, amount);
                    plugin.getBountyManager().addBounty(offlineTarget.getUniqueId(), offlineTarget.getName(), amount);
                    player.sendMessage(ChatColor.GREEN + "Taglia di $" + String.valueOf(amount) + " messa su " + offlineTarget.getName() + "!");
                    plugin.getServer().broadcastMessage(ChatColor.RED + "[Taglie] " + ChatColor.YELLOW + player.getName() +
                            ChatColor.WHITE + " ha messo una taglia di " + ChatColor.GREEN + "$" + String.valueOf(amount) +
                            ChatColor.WHITE + " su " + ChatColor.RED + offlineTarget.getName() + "!");
                    return true;
                }

                // Preleva soldi
                plugin.getEconomy().withdrawPlayer(player, amount);
                plugin.getBountyManager().addBounty(target.getUniqueId(), target.getName(), amount);

                player.sendMessage(ChatColor.GREEN + "Taglia di $" + String.valueOf(amount) + " messa su " + target.getName() + "!");
                plugin.getServer().broadcastMessage(ChatColor.RED + "[Taglie] " + ChatColor.YELLOW + player.getName() +
                        ChatColor.WHITE + " ha messo una taglia di " + ChatColor.GREEN + "$" + String.valueOf(amount) +
                        ChatColor.WHITE + " su " + ChatColor.RED + target.getName() + "!");

                target.sendMessage(ChatColor.RED + "Attenzione! " + player.getName() + " ha messo una taglia di $" + String.valueOf(amount) + " su di te!");
            }

            case "cerca" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /bounty cerca <nome>");
                    return true;
                }
                String query = args[1];
                plugin.getBountyGUI().openBountyList(player, 0, query);
            }

            default -> {
                player.sendMessage(ChatColor.RED + "=== ItaliaBounty ===");
                player.sendMessage(ChatColor.WHITE + "/bounty " + ChatColor.GRAY + "- Apri lista taglie");
                player.sendMessage(ChatColor.WHITE + "/bounty set <player> <importo> " + ChatColor.GRAY + "- Metti una taglia");
                player.sendMessage(ChatColor.WHITE + "/bounty cerca <nome> " + ChatColor.GRAY + "- Cerca un giocatore");
            }
        }

        return true;
    }
}
