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
package com.imaginarycode.minecraft.velocityjson.api;

public interface RequestManager {
    /**
     * Register the specified endpoint with this request handler.
     *
     * @param endpoint the endpoint URL, relative to the BungeeJSON server root
     * @param handler the endpoint {@link com.imaginarycode.minecraft.velocityjson.api.RequestHandler}
     */
    void registerEndpoint(String endpoint, RequestHandler handler);
    RequestHandler getHandlerForEndpoint(String uri);
}