/*
 * Copyright (c) 2015 IBM Corporation and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brunel.build.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;

import org.brunel.geom.Rect;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ContentReader {
	public static String readContentFromUrl(URI uri) throws IOException {
		//TODO:  Centrally handle security
		try {
			//Setting User-Agent avoids receiving an http 403 error.
			URLConnection httpcon = uri.toURL().openConnection();
			httpcon.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11");

			return readContent(httpcon.getInputStream());
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("Could not read data from: " + uri, ex.getCause());
		}
	}

	public static String readContent(InputStream is) throws IOException {
		// Use StringBuilder to read the data in large chunks
		StringBuilder builder = new StringBuilder();
		byte[] data = new byte[20480];

		int c;
		while ((c = is.read(data, 0, data.length)) > 0)
			builder.append(new String(data, 0, c));

		return builder.toString();
	}
	
	public static JsonElement readJsonContentFromUrl(String url) {
		try {
			String json = readContentFromUrl(new URI(url));
			JsonElement parsed = new JsonParser().parse(json);
			return parsed;
		} catch (IOException e) {
			throw new IllegalArgumentException("Unable to read json at: " + url);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("URL for json malformed: " + url);
		} 
	}
	
	/**
	 * Convenience method to extract bounds from a topojson file
	 * @param url
	 * @return
	 */
	public static Rect extractBoundsFromTopoJson(String url) {
		
		try {
			JsonElement parsed = readJsonContentFromUrl(url);
			JsonObject root = parsed.getAsJsonObject();
			JsonArray bbox = root.getAsJsonArray("bbox");
			Rect bounds = new Rect(bbox.get(0).getAsDouble(), bbox.get(2).getAsDouble(), bbox.get(1).getAsDouble(), bbox.get(3).getAsDouble());			
			return bounds;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Unable to locate map bounds in topojson at : " + url);

		}
		
	}

}
