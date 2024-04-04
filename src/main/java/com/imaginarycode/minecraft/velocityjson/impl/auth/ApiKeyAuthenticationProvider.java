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
package com.imaginarycode.minecraft.velocityjson.impl.auth;

import com.google.common.hash.Hashing;
import com.imaginarycode.minecraft.velocityjson.VelocityJSONPlugin;
import com.imaginarycode.minecraft.velocityjson.api.ApiRequest;
import com.imaginarycode.minecraft.velocityjson.api.AuthenticationProvider;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ApiKeyAuthenticationProvider implements AuthenticationProvider {
    @Override
    public void onEnable() {
        if (VelocityJSONPlugin.getPlugin().getConfig().get("api-keys") == null ||
                !(VelocityJSONPlugin.getPlugin().getConfig().get("api-keys") instanceof List)) {
            VelocityJSONPlugin.getPlugin().getLogger().info("You don't seem to have any API keys, so I'm adding one for your convenience.");
            byte[] bytes = new byte[16];
            new Random().nextBytes(bytes);
            String firstKey = Hashing.sha1().hashBytes(bytes).toString();
            VelocityJSONPlugin.getPlugin().getConfig().set("api-keys", Collections.singletonList(firstKey));
            VelocityJSONPlugin.getPlugin().saveConfig();
            VelocityJSONPlugin.getPlugin().getLogger().info("Your new API key is " + firstKey + ".");
        }
    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean authenticate(ApiRequest ar, String uri) {
        // Take only the first key in the set.
        if (!ar.getParams().containsKey("key"))
            return false;
        String key = ar.getParams().get("key").get(0);
        return VelocityJSONPlugin.getPlugin().getConfig().getStringList("api-keys").contains(key);
    }
}
