/**
 * This file is part of BungeeJSON.
 *
 * BungeeJSON is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BungeeJSON is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BungeeJSON.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.imaginarycode.minecraft.velocityjson;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class VelocityJSONUtilities {
    private static final Pattern UUID_PATTERN = Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");
    private static final Pattern MOJANGIAN_UUID_PATTERN = Pattern.compile("[a-fA-F0-9]{32}");

    private VelocityJSONUtilities() {}

    private static final Map<String, String> STATUS_OK = Collections.singletonMap("status", "OK");

    public static Map<String, String> error(String error) {
        return Collections.singletonMap("error", error);
    }

    public static Map<String, String> ok() {
        return STATUS_OK;
    }

    public static Component singletonChatComponent(String text) {
        return Component.text(text);
    }

    public static Optional<Player> resolvePlayer(String player) {
        ProxyServer server = VelocityJSONPlugin.getPlugin().getServer();
        Optional<Player> player1 = server.getPlayer(player);

        if (player1.isPresent())
            return player1;

        if (UUID_PATTERN.matcher(player).matches())
            return server.getPlayer(UUID.fromString(player));

        if (MOJANGIAN_UUID_PATTERN.matcher(player).matches()) {
            return server.getPlayer(UUID.fromString(player.substring(0, 8) + "-" + player.substring(8, 12) + "-" + player.substring(12, 16) + "-" + player.substring(16, 20) + "-" + player.substring(20, 32)));
        }

        return null;
    }
}
