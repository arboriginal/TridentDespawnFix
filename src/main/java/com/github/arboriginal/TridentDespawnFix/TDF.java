package com.github.arboriginal.TridentDespawnFix;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TDF extends JavaPlugin implements Listener {
    private HashMap<String, Integer> l, m;

    @Override
    public boolean onCommand(CommandSender s, Command c, String n, String[] a) {
        if (!c.getName().equalsIgnoreCase("tdf-reload")) return super.onCommand(s, c, n, a);
        reloadConfig();
        s.sendMessage("§7[§b§lTDF§7] §aConfiguration reloaded.");
        return true;
    }

    @Override
    public void onEnable() {
        super.reloadConfig();
        reloadConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    private void onProjectileHit(ProjectileHitEvent e) {
        if (e.getHitBlock() == null || e.getEntityType() != EntityType.TRIDENT || !e.getEntity().isValid()) return;

        Projectile t = e.getEntity();
        if (!(t.getShooter() instanceof Player)) return;

        String  w = t.getWorld().getName();
        Integer d = l.get(w);
        if (d == null) return;

        new BukkitRunnable() {
            private int a = 0;

            @Override
            public void run() {
                if (!t.isValid()) {
                    cancel();
                    return;
                }

                a += t.getTicksLived();
                if (a < m.get(w)) t.setTicksLived(1);
                else cancel();
            }
        }.runTaskTimer(this, 0, d);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        saveDefaultConfig();
        ConfigurationSection y = Bukkit.spigot().getConfig().getConfigurationSection("world-settings");
        FileConfiguration    c = getConfig();
        c.options().copyDefaults(true);
        saveConfig();

        l = new HashMap<String, Integer>();
        m = new HashMap<String, Integer>();
        String s = ".arrow-despawn-rate", p = "trident-despawn-rates.";
        int    d = y.getInt("default" + s), e = c.getInt(p + "default");

        Bukkit.getWorlds().forEach(w -> {
            String n = w.getName();
            int    a = y.getInt(n + s, d), t = c.getInt(p + n, e);
            if (t <= a) return;
            m.put(n, t);
            l.put(n, a - 5);
        });
    }
}
