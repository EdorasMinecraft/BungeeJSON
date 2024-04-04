package com.imaginarycode.minecraft.velocityjson.impl.auth;

import com.imaginarycode.minecraft.velocityjson.VelocityJSONPlugin;
import com.imaginarycode.minecraft.velocityjson.api.ApiRequest;
import com.imaginarycode.minecraft.velocityjson.api.AuthenticationProvider;

public class DummyAuthenticationProvider implements AuthenticationProvider {
    @Override
    public void onEnable() {
        VelocityJSONPlugin.getPlugin().getLogger().warn("You are using the dummy authentication provider.");
        VelocityJSONPlugin.getPlugin().getLogger().warn("Unless you secure BungeeJSON by some other means, you will likely be subject to nasty annoyances.");
    }

    @Override
    public void onDisable() {
    }

    @Override
    public boolean authenticate(ApiRequest ar, String uri) {
        return true;
    }
}
