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
package poke.server.election;

import poke.core.Mgmt.LeaderElection.ElectAction;

/**
 * hold the information about an election
 * 
 * TODO used as a simple data structure. If we were to support election
 * implementations created outside of the framework, this class would need
 * getter/setter noise.
 * 
 * @author gash
 * 
 */
public class ElectionState {

	
	protected Integer id;
	protected String desc;
	protected int version = 0;
	protected ElectAction state = ElectAction.DECLAREELECTION;
	protected Integer electionID;
	protected long startedOn = 0, lastVoteOn = 0, maxDuration = -1;
	protected int candidate;
	protected ElectionListener listener;
	protected boolean active = false;

	public boolean isActive() {
		return id != null && active;
	}
}
