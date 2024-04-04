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
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.myzelyam.api.vanish.VelocityVanishAPI;

import java.util.HashMap;
import java.util.Map;

public class PlayersOnline implements RequestHandler {
    @Override
    public Object handle(ApiRequest request) {
        ProxyServer server = VelocityJSONPlugin.getPlugin().getServer();

        Map<String, String> players = new HashMap<>();
        for (Player pp : server.getAllPlayers()) {
            if(VelocityVanishAPI.isInvisible(pp)) continue;
            players.put(pp.getUsername(), pp.getUniqueId().toString());
        }
        return players;
    }

    @Override
    public boolean requiresAuthentication() {
        return false;
    }
}
