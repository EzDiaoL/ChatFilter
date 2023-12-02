package com.diaoling;

import com.diaoling.Listener.ChatListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatFilter extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        // 保存配置
        saveDefaultConfig();
        // 初始化监听器
        ChatListener chatListener = new ChatListener(this);

        // 注册监听器
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        // 注册指令
        this.getCommand("addword").setExecutor(chatListener);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveConfig();
    }
}
