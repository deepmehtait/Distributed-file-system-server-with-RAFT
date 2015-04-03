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

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.core.Mgmt.LeaderElection;
import poke.core.Mgmt.LeaderElection.ElectAction;
import poke.core.Mgmt.Management;
import poke.core.Mgmt.VectorClock;
import poke.server.election.RaftElection.RState;

/**
 * Implementation of Chang-Roberts Leader election algo.
 * 
 * 
 * *** THE CR IMPLEMENTATION IS NOT COMPLETE! ***
 * 
 * Notes:
 * <ul>
 * <li>The processing accepts the protobuf messaging data structure to minimize
 * transformations.
 * 
 * TODO Is direct use of protobuf structs wise? Other than performance, should
 * we?
 * 
 * <li>Extending Resource - is this needed or should it be more specific?
 * </ul>
 * 
 * @author gash
 * 
 */
public class ChangRobertsElection implements Election {
	protected static Logger logger = LoggerFactory.getLogger("changroberts");

	private Integer nodeId;
	private ElectionState current;
	private ElectionListener listener;

	public ChangRobertsElection() {
	}

	public ChangRobertsElection(Integer nodeId) {
		this.nodeId = nodeId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see poke.server.election.Election#setListener(poke.server.election.
	 * ElectionListener)
	 */
	@Override
	public void setListener(ElectionListener listener) {
		this.listener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see poke.server.election.Election#process(eye.Comm.LeaderElection)
	 */
	@Override
	public Management process(Management mgmt) {
		LeaderElection req = mgmt.getElection();
		if (req == null)
			return null;

		if (req.getExpires() <= System.currentTimeMillis()) {
			// election has expired without a conclusion?
		}

		Management rtn = null;

		if (req.getAction().getNumber() == ElectAction.DECLAREELECTION_VALUE) {
			// an election is declared!

			// required to eliminate duplicate messages
			List<VectorClock> rtes = mgmt.getHeader().getPathList();
			for (VectorClock rp : rtes) {
				if (rp.getNodeId() == this.nodeId) {
					// message has already been sent to me, don't use and
					// forward
					return null;
				}
			}

			// I got here because the election is unknown to me

			// this 'if debug is on' should cover the below dozen or so
			// println()s. It is here to help with seeing when an election
			// occurs.
			if (logger.isDebugEnabled()) {
			}

			System.out.println("\n\n*********************************************************");
			System.out.println(" CHANG-ROBERTS ELECTION: Election declared");
			System.out.println(" FLOOD MAX ELECTION: Election declared");
			System.out.println("   Election ID:  " + req.getElectId());
			System.out.println("   Rcv from:     Node " + mgmt.getHeader().getOriginator());
			System.out.println("   Expires:      " + new Date(req.getExpires()));
			System.out.println("   Nominates:    Node " + req.getCandidateId());
			System.out.println("   Desc:         " + req.getDesc());
			System.out.print("   Routing tbl:  [");
			for (VectorClock rp : rtes)
				System.out.print("Node " + rp.getNodeId() + " (" + rp.getVersion() + "," + rp.getTime() + "), ");
			System.out.println("]");
			System.out.println("*********************************************************\n\n");

			current = new ElectionState();
			current.electionID = createElectionID();
			current.desc = req.getDesc();
			current.maxDuration = req.getExpires();
			current.startedOn = System.currentTimeMillis();
			current.state = req.getAction();
			current.id = -1; // TODO me or sender?

		} else if (req.getAction().getNumber() == ElectAction.DECLAREVOID_VALUE) {
			// no one was elected, I am dropping into standby mode
			logger.info("TODO: no one was elected, I am dropping into standby mode");
		} else if (req.getAction().getNumber() == ElectAction.DECLAREWINNER_VALUE) {
			// some node declared themself the leader
			logger.info("TODO: some node declared themself the leader");
		} else if (req.getAction().getNumber() == ElectAction.ABSTAIN_VALUE) {
			// for some reason, I decline to vote
			logger.info("TODO: for some reason, I decline to vote");
		} else if (req.getAction().getNumber() == ElectAction.NOMINATE_VALUE) {
			if (mgmt.getHeader().getOriginator() < nodeId) {
				// Someone else has a higher priority, forward nomination
				logger.info("TODO: Someone else has a higher priority, forward nomination");
			} else if (mgmt.getHeader().getOriginator() > nodeId) {
				// I have a higher priority, nominate myself
				// TODO nominate myself
				logger.info("TODO: I have a higher priority, nominate myself");
			} else {
				// this is me!
			}
		}

		return rtn;
	}

	@Override
	public Integer getElectionId() {
		// TODO Auto-generated method stub
		if (current == null)
			return null;
		return current.electionID;
	}

	@Override
	public void setNodeId(int nodeId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		current = null;
	}

	@Override
	public boolean isElectionInprogress() {
		return current != null;
	}

	private Management castVote(Management mgmt) {
		if (true)
			throw new RuntimeException("ChangRobertsElection is incomplete!");

		if (!mgmt.hasElection())
			return null;

		LeaderElection req = mgmt.getElection();
		if (req.getExpires() <= System.currentTimeMillis())
			return null;

		List<VectorClock> rtes = mgmt.getHeader().getPathList();
		for (VectorClock rp : rtes) {
			if (rp.getNodeId() == this.nodeId) {
				return null;
			}
		}

		Management rtn = null;
		if (this.nodeId > req.getCandidateId()) {
			// I can cast a vote
		}

		return rtn;
	}

	@Override
	public Integer getWinner() {
		if (current == null)
			return null;
		else if (current.state.getNumber() == ElectAction.DECLAREELECTION_VALUE)
			return current.candidate;
		else
			return null;
	}

	@Override
	public Integer createElectionID() {
		return ElectionIDGenerator.nextID();
	}

	/*
	 * if (msg.getAction() == ElectAction.ELECTION) { leaderWait =
	 * System.currentTimeMillis(); leader = msg.getOrigination(); } else if
	 * (msg.getAction() == Message.Action.LeaderElection) { leader =
	 * Message.sNobody; leaderWait = 0;
	 * 
	 * // the challenge leader is used to only issue challenges if the //
	 * challenger (ID) is less than myself challengeLeader =
	 * Math.max(challengeLeader, msg.getOrigination()); System.out.println("   "
	 * + id + " accepts " + challengeLeader + " as potiental leader");
	 * 
	 * if (challengeLeader < id) issueChallenge(); } else if (msg.getAction() ==
	 * Message.Action.LeaderDeclared) { //
	 * System.out.println("leader declared: " + // msg.getOrigination()); leader
	 * = msg.getOrigination(); leaderWait = System.currentTimeMillis();
	 * challengeWait = 0; challengeLeader = Message.sNobody; } else { // normal
	 * message processing goes here } }
	 */
	
}
