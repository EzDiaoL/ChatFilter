package com.diaoling.Listener;

import com.diaoling.ChatFilter;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class ChatListener implements Listener, CommandExecutor {

    private static final String ADMIN_CHAT_PERMISSION = "chatfilter.admin";

    private ChatFilter plugin;
    private Set<String> blockedWords = new HashSet<>();
    private HashMap<Player, Integer> warnings = new HashMap<>();

    public ChatListener(ChatFilter plugin) {
        this.plugin = plugin;
        loadBlockedWords();
    }

    private void loadBlockedWords() {
        List<String> words = plugin.getConfig().getStringList("blockedWords");
        for (String word : words) {
            blockedWords.add(word);
        }
    }

    private void initBlockedWords() {
        // 这里已删除，将违禁词添加到配置文件
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("此命令仅限玩家使用！");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("chatfilter.addwords")) {
            player.sendMessage(ChatColor.RED + "您没有权限执行此命令。");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "请指定一个要添加的违禁词。");
            return true;
        }

        String word = args[0];
        List<String> currentBlockedWords = plugin.getConfig().getStringList("blockedWords");
        currentBlockedWords.add(word);
        plugin.getConfig().set("blockedWords", currentBlockedWords);
        plugin.saveConfig();
        blockedWords.add(word);
        player.sendMessage(ChatColor.GREEN + "违禁词 " + ChatColor.YELLOW + word + ChatColor.GREEN + " 已添加到列表。");

        return true;
    }

        @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
            if (player.hasPermission(ADMIN_CHAT_PERMISSION)) {
                return;
            }
            String message = event.getMessage();
            blockedWords = new HashSet<>(plugin.getConfig().getStringList("blockedWords"));


        for (String word : blockedWords) {
            if (message.contains(word)) {
                message = message.replaceAll(word, "我爱你~");
                event.setMessage(message);
                if (warnings.containsKey(player)) {
                    warnings.put(player, warnings.get(player) + 1);
                } else {
                    warnings.put(player, 1);
                }
                player.sendMessage(ChatColor.RED + "警告：" + ChatColor.RESET + " 请不要在游戏内使用不当言论,多次违规将会被禁止登录!");
                if (warnings.get(player) >= 3) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        handleBan(player);
                    });
                }
                break;
            }
        }
    }

    private void handleBan(Player player) {
        String BAN_REASON = ChatColor.RED + "您已被禁止登录到此服务器\n" + ChatColor.YELLOW + "原因" + ChatColor.DARK_RED + "[不当言论]\n" + ChatColor.RED + "封禁时间:" + ChatColor.GREEN + "请文明游戏,谢谢!";
        int BAN_DURATION = plugin.getConfig().getInt("banDurationInMinutes"); // 可在配置文件中指定禁止登录的时长

        player.kickPlayer(BAN_REASON);

        final BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        final BanEntry banEntry = banList.addBan(player.getName(), BAN_REASON, Date.from(Instant.now().plus(Duration.ofMinutes(BAN_DURATION))), "Server");

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            banList.pardon(player.getName());
            warnings.remove(player);
        }, 20L * 60L * BAN_DURATION);
    }
}
