package it.italiabounty.managers;

import it.italiabounty.ItaliaBounty;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class BountyManager {

    private final ItaliaBounty plugin;
    // UUID target -> importo totale taglia
    private final Map<UUID, Double> bounties = new HashMap<>();
    // UUID target -> nome player
    private final Map<UUID, String> playerNames = new HashMap<>();

    public BountyManager(ItaliaBounty plugin) {
        this.plugin = plugin;
        load();
    }

    public double getBounty(UUID uuid) {
        return bounties.getOrDefault(uuid, 0.0);
    }

    public void addBounty(UUID target, String targetName, double amount) {
        bounties.put(target, bounties.getOrDefault(target, 0.0) + amount);
        playerNames.put(target, targetName);
        save();
    }

    public void removeBounty(UUID uuid) {
        bounties.remove(uuid);
        playerNames.remove(uuid);
        save();
    }

    public boolean hasBounty(UUID uuid) {
        return bounties.containsKey(uuid) && bounties.get(uuid) > 0;
    }

    public String getPlayerName(UUID uuid) {
        return playerNames.getOrDefault(uuid, uuid.toString().substring(0, 8));
    }

    // Ritorna lista ordinata per importo decrescente
    public List<Map.Entry<UUID, Double>> getTopBounties() {
        return bounties.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toList());
    }

    // Cerca per nome
    public List<Map.Entry<UUID, Double>> searchBounties(String query) {
        String q = query.toLowerCase();
        return bounties.entrySet().stream()
                .filter(e -> playerNames.getOrDefault(e.getKey(), "").toLowerCase().contains(q))
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toList());
    }

    public void save() {
        File file = new File(plugin.getDataFolder(), "bounties.yml");
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, Double> entry : bounties.entrySet()) {
            String base = "bounties." + entry.getKey().toString();
            config.set(base + ".amount", entry.getValue());
            config.set(base + ".name", playerNames.get(entry.getKey()));
        }
        try { config.save(file); } catch (Exception e) { e.printStackTrace(); }
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "bounties.yml");
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.isConfigurationSection("bounties")) return;
        for (String key : config.getConfigurationSection("bounties").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                double amount = config.getDouble("bounties." + key + ".amount");
                String name = config.getString("bounties." + key + ".name", key);
                bounties.put(uuid, amount);
                playerNames.put(uuid, name);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}
