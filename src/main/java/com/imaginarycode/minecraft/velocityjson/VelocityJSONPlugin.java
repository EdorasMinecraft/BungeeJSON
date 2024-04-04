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

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.imaginarycode.minecraft.velocityjson.api.AuthenticationProvider;
import com.imaginarycode.minecraft.velocityjson.api.RequestManager;
import com.imaginarycode.minecraft.velocityjson.impl.auth.ApiKeyAuthenticationProvider;
import com.imaginarycode.minecraft.velocityjson.impl.auth.DummyAuthenticationProvider;
import com.imaginarycode.minecraft.velocityjson.impl.auth.IpBasedAuthenticationProvider;
import com.imaginarycode.minecraft.velocityjson.impl.httpserver.NettyBootstrap;
import com.imaginarycode.minecraft.velocityjson.impl.VelocityJSONRequestManager;
import com.imaginarycode.minecraft.velocityjson.impl.handlers.velocity.*;
import com.imaginarycode.minecraft.velocityjson.impl.handlers.velocityjson.IsAuthenticated;
import com.imaginarycode.minecraft.velocityjson.impl.handlers.velocityjson.Version;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Path;

@Plugin(id = "velocityjson", name = "VelocityJSON", version = "0.3-EDORAS", description = "A JSON API for Velocity", authors = {"tuxed", "Cadiducho", "Kikisito"})
public class VelocityJSONPlugin {
    private NettyBootstrap nb = new NettyBootstrap();
    @Getter
    protected static VelocityJSONPlugin plugin;

    /**
     * -- GETTER --
     * Fetch the {@link com.imaginarycode.minecraft.velocityjson.api.RequestManager}.
     */
    @Getter
    private static RequestManager requestManager = new VelocityJSONRequestManager();

    @Getter
    public AuthenticationProvider authenticationProvider = new ApiKeyAuthenticationProvider();

    @Getter
    public Gson gson = new Gson();

    @Getter
    public YamlFile config = null;

    @Getter
    final ProxyServer server;

    @Getter
    final Logger logger;

    final Path dataDirectory;

    @Inject
    public VelocityJSONPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        // Load the configuration
        this.loadConfig();
    }

    public void loadConfig() {
        File dataFolder = dataDirectory.toFile();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        File configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream is = this.getClass().getResourceAsStream("config.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }
        try {
            config = new YamlFile(configFile);
            config.createOrLoadWithComments();
        } catch (IOException e) {
            throw new RuntimeException("Unable to load configuration file", e);
        }
        plugin = this;
        requestManager.registerEndpoint("/velocity/player_count", new PlayerCount());
        requestManager.registerEndpoint("/velocity/players_online", new PlayersOnline());
        requestManager.registerEndpoint("/velocity/server_list", new ServerList());
        requestManager.registerEndpoint("/velocityjson/version", new Version());
        requestManager.registerEndpoint("/velocityjson/is_authenticated", new IsAuthenticated());
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        switch (config.getString("auth-type", "api-key")) {
            case "none":
            case "dummy":
                authenticationProvider = new DummyAuthenticationProvider();
                break;
            case "ipbased":
            case "ip":
            case "ip-based":
                authenticationProvider = new IpBasedAuthenticationProvider();
                break;
            case "key":
            case "apikey":
            case "api-key":
                break;
            default:
                this.getLogger().info(config.getString("auth-type") + " authentication is not known to this plugin, using api-key auth.");
                break;
        }
        authenticationProvider.onEnable();

        this.getServer().getScheduler().buildTask(this, () -> nb.initialize()).schedule();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        nb.getChannelFuture().channel().close().syncUninterruptibly();
        nb.getGroup().shutdownGracefully();
    }

    public void saveConfig() {
        try {
            config.save();
            //ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            throw new RuntimeException("Unable to save configuration file", e);
        }
    }
}
