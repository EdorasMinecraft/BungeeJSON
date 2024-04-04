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
package com.imaginarycode.minecraft.velocityjson.impl.httpserver;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.imaginarycode.minecraft.velocityjson.VelocityJSONPlugin;
import com.imaginarycode.minecraft.velocityjson.VelocityJSONUtilities;
import com.imaginarycode.minecraft.velocityjson.api.ApiRequest;
import com.imaginarycode.minecraft.velocityjson.api.RequestHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {
    private HttpRequest request;
    private StringBuilder bodyBuilder = new StringBuilder();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        if (channelHandlerContext.channel().remoteAddress() instanceof InetSocketAddress) {
            if (o instanceof HttpRequest) {
                request = (HttpRequest) o;
            }
            if (o instanceof HttpContent) {
                bodyBuilder.append(((HttpContent) o).content().toString(Charsets.UTF_8));
                if (o instanceof LastHttpContent) {
                    HttpResponse response = getResponse(channelHandlerContext, request);
                    final boolean dontKeepAlive = disobeyKeepAlive();
                    if (dontKeepAlive) {
                        response.headers().set("Connection", "close");
                    }
                    ChannelFuture future = channelHandlerContext.channel().writeAndFlush(getResponse(channelHandlerContext, request));
                    future.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future1) throws Exception {
                            bodyBuilder.delete(0, bodyBuilder.length());
                            if (dontKeepAlive)
                                future1.channel().close();
                        }
                    });
                }
            }
        }
    }

    private boolean disobeyKeepAlive() {
        return request.headers().entries().size() == 1 && request.headers().contains("Host");
    }

    private HttpResponse getResponse(ChannelHandlerContext channelHandlerContext, HttpRequest hr) {
        QueryStringDecoder query = new QueryStringDecoder(hr.getUri());
        Multimap<String, String> params = createMultimapFromMap(query.parameters());

        Object reply;
        HttpResponseStatus hrs;
        final RequestHandler handler = VelocityJSONPlugin.getRequestManager().getHandlerForEndpoint(query.path());
        if (handler == null) {
            reply = VelocityJSONUtilities.error("No such endpoint exists.");
            hrs = HttpResponseStatus.NOT_FOUND;
        } else {
            final ApiRequest ar = new HttpServerApiRequest(((InetSocketAddress) channelHandlerContext.channel().remoteAddress()).getAddress(),
                    params, bodyBuilder.toString());
            try {
                if (handler.requiresAuthentication() && !VelocityJSONPlugin.getPlugin().authenticationProvider.authenticate(ar, query.path())) {
                    hrs = HttpResponseStatus.FORBIDDEN;
                    reply = VelocityJSONUtilities.error("Access denied.");
                } else {
                    reply = handler.handle(ar);
                    hrs = HttpResponseStatus.OK;
                }
            } catch (Throwable throwable) {
                hrs = HttpResponseStatus.INTERNAL_SERVER_ERROR;
                reply = VelocityJSONUtilities.error("An internal error has occurred. Information has been logged to the console.");
                VelocityJSONPlugin.getPlugin().getLogger().warn("Error while handling " + hr.getUri() + " from " + ar.getRemoteIp(), throwable);
            }
        }

        String json = VelocityJSONPlugin.getPlugin().gson.toJson(reply);
        DefaultFullHttpResponse hreply = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, hrs, Unpooled.wrappedBuffer(json.getBytes(CharsetUtil.UTF_8)));
        // Add a reminder that we're still running the show.
        hreply.headers().set("Content-Type", "application/json; charset=UTF-8");
        hreply.headers().set("Access-Control-Allow-Origin", "*");
        hreply.headers().set("Server", "VelocityJSON/0.1");
        hreply.headers().set("Content-Length", json.length());
        hreply.headers().set("Connection", "keep-alive");
        return hreply;
    }

    private <K, V> Multimap<K, V> createMultimapFromMap(Map<K, List<V>> map) {
        Multimap<K, V> multimap = HashMultimap.create();
        for (Map.Entry<K, List<V>> entry : map.entrySet()) {
            multimap.putAll(entry.getKey(), entry.getValue());
        }
        return multimap;
    }
}
