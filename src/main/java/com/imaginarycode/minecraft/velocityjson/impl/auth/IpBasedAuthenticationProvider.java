package com.imaginarycode.minecraft.velocityjson.impl.auth;

import com.imaginarycode.minecraft.velocityjson.VelocityJSONPlugin;
import com.imaginarycode.minecraft.velocityjson.api.ApiRequest;
import com.imaginarycode.minecraft.velocityjson.api.AuthenticationProvider;

import java.util.Collections;
import java.util.List;

public class IpBasedAuthenticationProvider implements AuthenticationProvider {
    @Override
    public void onEnable() {
        if (VelocityJSONPlugin.getPlugin().getConfig().get("authenticated-ips") == null ||
                !(VelocityJSONPlugin.getPlugin().getConfig().get("authenticated-ips") instanceof List)) {
            VelocityJSONPlugin.getPlugin().getLogger().info("No IPs are defined in your configuration.");
            VelocityJSONPlugin.getPlugin().getLogger().info("I will add the section for you.");
            VelocityJSONPlugin.getPlugin().getConfig().set("authenticated-ips", Collections.singletonList("127.0.0.1"));
            VelocityJSONPlugin.getPlugin().saveConfig();
        }
    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean authenticate(ApiRequest ar, String uri) {
        return ar.getRemoteIp().isLoopbackAddress() ||
                (VelocityJSONPlugin.getPlugin().getConfig().getStringList("authenticated-ips")
                        .contains(ar.getRemoteIp().getHostAddress()));
    }
}
