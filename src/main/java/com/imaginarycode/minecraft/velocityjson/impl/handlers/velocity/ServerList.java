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
package com.imaginarycode.minecraft.velocityjson.impl.handlers.velocity;

import com.imaginarycode.minecraft.velocityjson.VelocityJSONPlugin;
import com.imaginarycode.minecraft.velocityjson.api.ApiRequest;
import com.imaginarycode.minecraft.velocityjson.api.RequestHandler;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.myzelyam.api.vanish.VelocityVanishAPI;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerList implements RequestHandler {
    @Override
    public Object handle(ApiRequest request) {
        ProxyServer proxy = VelocityJSONPlugin.getPlugin().getServer();

        List<Server> servers = new ArrayList<>();
        for (RegisteredServer rs : proxy.getAllServers()) {
            ServerInfo si = rs.getServerInfo();
            Server server = new Server();
            server.setName(si.getName());
            List<Player> players = new ArrayList<>();
            for (com.velocitypowered.api.proxy.Player pp : rs.getPlayersConnected()) {
                if(VelocityVanishAPI.isInvisible(pp)) continue;
                Player player = new Player();
                player.setName(pp.getUsername());
                player.setUuid(pp.getUniqueId());
                players.add(player);
            }
            server.setPlayers(players);
            servers.add(server);
        }
        return servers;
    }

    @Override
    public boolean requiresAuthentication() {
        return false;
    }

    @Data
    private class Server {
        private String name;
        private List<ServerList.Player> players;
    }

    @Data
    private class Player {
        private String name;
        private UUID uuid;
    }
}
