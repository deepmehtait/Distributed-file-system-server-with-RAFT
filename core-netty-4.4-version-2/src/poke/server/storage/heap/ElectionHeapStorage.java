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
package poke.server.storage.heap;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.core.Mgmt.LeaderElection;
import poke.server.storage.ElectionStorage;

public class ElectionHeapStorage implements ElectionStorage {
	protected static Logger logger = LoggerFactory.getLogger("storage");
	private HashMap<Long, DataElection> spaces = new HashMap<Long, DataElection>();

	@Override
	public boolean addElection(LeaderElection le) {

		// override any existing election
		
		return false;
	}

	@Override
	public boolean updateElection(LeaderElection le) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeElection(long electionId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> findElections(long electionId) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * simple data store - only one election at a time!
	 * 
	 * @author gash
	 * 
	 */
	private static class DataElection {
		int electionId;
		String electionDesc;
		long expires;
		boolean sentVote;

	}
}
