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
package poke.server.election;

import poke.core.Mgmt.Management;
import poke.server.election.RaftElection.RState;

/**
 * Describes behavior of an election
 * 
 * TODO this should be a abstract class
 * 
 * @author gash
 * 
 */
public interface Election {

	/**
	 * notification of the election results.
	 * 
	 * TODO do we need it to be a list?
	 * 
	 * @param listener
	 */
	void setListener(ElectionListener listener);

	/**
	 * reset the election
	 */
	void clear();

	/**
	 * Is an election currently in progress
	 * 
	 * @return
	 */
	boolean isElectionInprogress();

	/**
	 * the current election's ID
	 * 
	 * @return
	 */
	Integer getElectionId();

	/**
	 * create the election ID for messaging
	 * 
	 * @return
	 */
	Integer createElectionID();

	/**
	 * The winner of the election
	 * 
	 * @return The winner or null
	 */
	Integer getWinner();

	/**
	 * implementation of the Chang Roberts election. This assumes a
	 * unidirectional closed overlay network.
	 * 
	 * @param req
	 * @return the resulting management action (if any)
	 */
	Management process(Management req);

	/**
	 * the node ID of myself.
	 * 
	 * @param nodeId
	 *            The ID of a node - this not allowed to be null!
	 */
	void setNodeId(int nodeId);

}