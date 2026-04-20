package it.italiabounty;

import it.italiabounty.commands.BountyCommand;
import it.italiabounty.gui.BountyGUI;
import it.italiabounty.listeners.BountyListener;
import it.italiabounty.managers.BountyManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ItaliaBounty extends JavaPlugin {

    private BountyManager bountyManager;
    private BountyGUI bountyGUI;
    private Economy economy;

    @Override
    public void onEnable() {
        getLogger().info("ItaliaBounty avviato!");
        getDataFolder().mkdirs();

        if (!setupEconomy()) {
            getLogger().severe("Vault non trovato! ItaliaBounty si disabilita.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        bountyManager = new BountyManager(this);
        bountyGUI = new BountyGUI(this);

        getCommand("bounty").setExecutor(new BountyCommand(this));
        getServer().getPluginManager().registerEvents(new BountyListener(this), this);

        getLogger().info("ItaliaBounty caricato con successo!");
    }

    @Override
    public void onDisable() {
        if (bountyManager != null) bountyManager.save();
        getLogger().info("ItaliaBounty disattivato!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public BountyManager getBountyManager() { return bountyManager; }
    public BountyGUI getBountyGUI() { return bountyGUI; }
    public Economy getEconomy() { return economy; }
}
