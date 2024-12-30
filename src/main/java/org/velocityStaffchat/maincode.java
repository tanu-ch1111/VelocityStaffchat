package org.velocityStaffchat;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

@Plugin(
        id = "(プラグインid)",
        name = "(プラグインの名前)",
        version = "1.0"
)
public class maincode {

    @Inject
    private Logger logger;

    private final Set<Player> staffChatUsers = new HashSet<>();
    private static final String STAFF_CHAT_PERMISSION = "(権限)";

    private final ProxyServer server;

    @Inject
    public maincode(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // /sc コマンドの登録
        server.getCommandManager().register("sc", new SimpleCommand() {
            @Override
            public void execute(Invocation invocation) {
                CommandSource source = invocation.source();

                if (!(source instanceof Player player)) {
                    source.sendMessage(Component.text("§cこのコマンドはプレイヤー専用です。"));
                    return;
                }

                if (!player.hasPermission(STAFF_CHAT_PERMISSION)) {
                    player.sendMessage(Component.text("§c権限がありません。"));
                    return;
                }

                if (staffChatUsers.contains(player)) {
                    staffChatUsers.remove(player);
                    player.sendMessage(Component.text("§cスタッフチャットモードを無効化しました。"));
                } else {
                    staffChatUsers.add(player);
                    player.sendMessage(Component.text("§aスタッフチャットモードを有効化しました。"));
                }
            }
        });

        logger.info("プラグインが有効化されました！");
    }

    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();

        // スタッフチャットモードが有効の場合
        if (staffChatUsers.contains(player)) {
            event.setResult(PlayerChatEvent.ChatResult.denied()); // 通常チャットをキャンセル

            // スタッフチャットメッセージを送信
            Component staffMessage = Component.text("§7[StaffChat§8]" + player.getUsername() + ":" + event.getMessage());
            server.getAllPlayers().stream()
                    .filter(p -> p.hasPermission(STAFF_CHAT_PERMISSION))
                    .forEach(p -> p.sendMessage(staffMessage));
        }
    }
}