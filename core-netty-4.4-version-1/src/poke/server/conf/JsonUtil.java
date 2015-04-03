/*
 * copyright 2012, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package poke.server.conf;

import java.io.File;

import org.codehaus.jackson.map.ObjectMapper;

public class JsonUtil {
	private static JsonUtil instance;

	public static void init(File cfg) {

	}

	public static JsonUtil getInstance() {
		if (instance == null)
			throw new RuntimeException("Server has not been initialized");

		return instance;
	}

	public static String encode(Object data) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(data);
		} catch (Exception ex) {
			return null;
		}
	}

	public static <T> T decode(String data, Class<T> theClass) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(data.getBytes(), theClass);
		} catch (Exception ex) {
			return null;
		}
	}
}
