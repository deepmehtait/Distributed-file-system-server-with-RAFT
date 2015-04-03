/*
 * copyright 2014, gash
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
package poke.server.managers;

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VotingManager {
	protected static Logger logger = LoggerFactory.getLogger("voting");
	protected static AtomicReference<VotingManager> instance = new AtomicReference<VotingManager>();

	private String nodeId;

	public static VotingManager getInstance(String id) {
		instance.compareAndSet(null, new VotingManager(id));
		return instance.get();
	}

	public static VotingManager getInstance() {
		return instance.get();
	}

	public VotingManager(String nodeId) {
		this.nodeId = nodeId;
	}
}
