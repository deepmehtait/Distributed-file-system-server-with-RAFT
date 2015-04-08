package poke.server.election;

import io.netty.channel.Channel;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.core.Mgmt.LeaderElection;
import poke.core.Mgmt.LeaderElection.ElectAction;
import poke.core.Mgmt.Management;
import poke.core.Mgmt.MgmtHeader;
import poke.core.Mgmt.RaftMessage;
import poke.core.Mgmt.RaftMessage.RaftAction;
import poke.core.Mgmt.RaftMessage.RaftAppendAction;
import poke.server.managers.ConnectionManager;
import poke.server.managers.HeartbeatManager;
import poke.server.managers.RaftManager;

public class RaftElection implements Election {

	protected static Logger logger = LoggerFactory.getLogger("Raft");
	private Integer nodeId;
	private ElectionListener listener;

	private ElectionState current;
	private int count = 0;

	public enum RState {
		Follower, Candidate, Leader
	}

	private RState currentState;
	private int term;
	private int leaderId = -1;
	private long lastKnownBeat = System.currentTimeMillis();
	private int timeElection;
	private RaftMonitor monitor = new RaftMonitor();
	private RaftMessage votedFor;

	public RaftElection() {
		this.timeElection = new Random().nextInt(10000);
		if (this.timeElection < 5000)
			this.timeElection += 3000;
		logger.info("Time Electio  " + timeElection);
		currentState = RState.Follower;
	}

	/**
	 * init with whoami
	 * 
	 * @param nodeId
	 */
	// public RaftElection(Integer nodeId) {
	// this.nodeId = nodeId;
	// this.timeElection = new Random().nextInt(10000);
	// if(this.timeElection < 2000)
	// this.timeElection+=5000;
	// logger.info("TimeElection : "+ nodeId +" - "+ timeElection);
	// currentState = RState.Follower;
	// }

	public void setListener(ElectionListener listener) {
		this.listener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see poke.server.election.Election#process(eye.Comm.LeaderElection)
	 * 
	 * @return The Management instance returned represents the message to send
	 * on. If null, no action is required. Not a great choice to convey to the
	 * caller; can be improved upon easily.
	 */
	private int majority_count = 0;

	public Management process(Management mgmt) {
		if (!mgmt.hasRaftmessage())
			return null;

		RaftMessage rm = mgmt.getRaftmessage();

		Management rtn = null;

		if (rm.getRaftAction().getNumber() == RaftAction.REQUESTVOTE_VALUE) {
			// an election is declared!
			//
			// if (currentState == RState.Candidate) {
			// // candidate ignores other vote requests
			// } else
			if (currentState == RState.Follower
					|| currentState == RState.Candidate) {

				this.lastKnownBeat = System.currentTimeMillis();
				/**
				 * check if already voted for this term or else vote for the
				 * candidate
				 **/

				if (this.votedFor == null
						|| rm.getTerm() > this.votedFor.getTerm()) {
					if (this.votedFor != null) {
						System.out.println("Voting for "
								+ mgmt.getHeader().getOriginator()
								+ " for term" + rm.getTerm() + " from node "
								+ nodeId + " voted term "
								+ this.votedFor.getTerm());
					} else {
						System.out.println("Node " + nodeId
								+ " Voting for first time for "
								+ mgmt.getHeader().getOriginator());
					}

					this.votedFor = rm;
					rtn = castVote();
				}
			} else if (currentState == RState.Leader) {
				// TODO
			}
		} else if (rm.getRaftAction().getNumber() == RaftAction.VOTE_VALUE) {

			if (currentState == RState.Candidate) {
				System.out.println("Node " + getNodeId()
						+ " received vote from Node "
						+ mgmt.getHeader().getOriginator() + ". Votecount "
						+ count);
				receiveVote(rm);
			} else if (currentState == RState.Follower) {

			} else if (currentState == RState.Leader) {

			}

		} else if (rm.getRaftAction().getNumber() == RaftAction.LEADER_VALUE) {
			if (rm.getTerm() > this.term) {
				this.leaderId = mgmt.getHeader().getOriginator();
				this.term = rm.getTerm();
				this.lastKnownBeat = System.currentTimeMillis();
				notify(true, mgmt.getHeader().getOriginator());
				logger.info("Node " + mgmt.getHeader().getOriginator()
						+ " is the leader ");
			}
		} else if (rm.getRaftAction().getNumber() == RaftAction.APPEND_VALUE) {
			if (currentState == RState.Candidate) {
				if (rm.getTerm() >= term) {
					this.lastKnownBeat = System.currentTimeMillis();
					this.term = rm.getTerm();
					this.leaderId = mgmt.getHeader().getOriginator();
					this.currentState = RState.Follower;
					logger.info("Received Append RPC from leader "
							+ mgmt.getHeader().getOriginator());

				}

			} else if (currentState == RState.Follower) {
				this.term = rm.getTerm();
				// Move this line to last stmt as this resets the timer
				this.lastKnownBeat = System.currentTimeMillis();
				logger.info("---Test--- " + mgmt.getHeader().getOriginator()
						+ "\n RaftAction=" + rm.getRaftAction().getNumber()
						+ " RaftAppendAction="
						+ rm.getRaftAppendAction().getNumber());
				if (rm.getRaftAppendAction().getNumber() == RaftAppendAction.APPENDHEARTBEAT_VALUE) {
					// Return Sucess to Leader if its normal Heartbeat
					// message

					logger.info("*Follower stateReceived AppendAction HB RPC from leader "
							+ mgmt.getHeader().getOriginator()
							+ "\n RaftAction="
							+ rm.getRaftAction().getNumber()
							+ " RaftAppendAction="
							+ rm.getRaftAppendAction().getNumber());
					sendAppendResponse(mgmt);

				} else if (rm.getRaftAppendAction().getNumber() == RaftAppendAction.APPENDLOG_VALUE) {
					// Return Sucess to Leader if Log is done
					// message

					logger.info("**Follower stateReceived AppendAction HB Log  from leader "
							+ mgmt.getHeader().getOriginator()
							+ "\n RaftAction="
							+ rm.getRaftAction().getNumber()
							+ " RaftAppendAction="
							+ rm.getRaftAppendAction().getNumber());
					sendAppendResponseLog(mgmt);

				} else if (rm.getRaftAppendAction().getNumber() == RaftAppendAction.APPENDVALUE_VALUE) {
					// Return Sucess to Leader if Value is done
					// message

					logger.info("***Follower stateReceived AppendAction HB VALUE  from leader "
							+ mgmt.getHeader().getOriginator()
							+ "\n RaftAction="
							+ rm.getRaftAction().getNumber()
							+ " RaftAppendAction="
							+ rm.getRaftAppendAction().getNumber());
					sendAppendResponseValue(mgmt);

				}
				// TODO append work
			} else if (currentState == RState.Leader) {
				// should not happen
			}
		} else if (rm.getRaftAction().getNumber() == RaftAction.APPENDRESPONSE_VALUE) {
			if (currentState == RState.Leader) {

				if (rm.getRaftAppendAction().getNumber() == RaftAppendAction.APPENDLOG_VALUE) {
					if (rm.getSuccess() == 1) {
						majority_count++;

						if (majority_count >= (HeartbeatManager.getInstance().outgoingHB
								.size() + 1) / 2) {
							i = 5;
							// sendAppendNotice();

							// how to break here
						}
					} else {
						if (majority_count < (HeartbeatManager.getInstance().outgoingHB
								.size() + 1) / 2) {
							int id = mgmt.getHeader().getOriginator();

							logger.info("Leader Node " + this.nodeId
									+ " sending appendAction HB LOG RPC's");
							RaftMessage.Builder rm1 = RaftMessage.newBuilder();
							MgmtHeader.Builder mhb = MgmtHeader.newBuilder();
							mhb.setTime(System.currentTimeMillis());
							mhb.setSecurityCode(-999); // TODO add security
							mhb.setOriginator(this.leaderId);
							mhb.setToNode(id);

							// Raft Message to be added
							rm1.setTerm(term);
							rm1.setRaftAction(RaftAction.APPEND);
							rm1.setRaftAppendAction(RaftAppendAction.APPENDLOG);
							Management.Builder mb = Management.newBuilder();

							mb.setHeader(mhb.build());
							mb.setRaftmessage(rm1.build());

							Channel ch = ConnectionManager.getConnection(id,
									true);
							if (ch != null)
								ch.writeAndFlush(mb.build());

						}

					}
				}

				if (rm.getRaftAppendAction().getNumber() == RaftAppendAction.APPENDVALUE_VALUE) {
					// do nothing
				}

				if (rm.getRaftAppendAction().getNumber() == RaftAppendAction.APPENDHEARTBEAT_VALUE) {
					// do nothing
				}

			}

		}

		if (rm.getTerm() > this.term) {
			this.leaderId = mgmt.getHeader().getOriginator();
			this.term = rm.getTerm();
			this.lastKnownBeat = System.currentTimeMillis();
			notify(true, mgmt.getHeader().getOriginator());
			logger.info("Node " + mgmt.getHeader().getOriginator()
					+ " is the leader ");

		}
		return rtn;
	}

	// Response of Normal HB to Leader
	private Management sendAppendResponse(Management mgmt) {
		// TODO Auto-generated method stub

		RaftMessage.Builder rm = RaftMessage.newBuilder();
		MgmtHeader.Builder mhb = MgmtHeader.newBuilder();
		mhb.setTime(System.currentTimeMillis());
		mhb.setSecurityCode(-999); // TODO add security
		mhb.setOriginator(this.nodeId);
		mhb.setToNode(mgmt.getHeader().getOriginator());
		// Raft Message to be added
		rm.setTerm(term);
		rm.setSuccess(1);
		rm.setRaftAction(RaftAction.APPENDRESPONSE);
		Management.Builder mb = Management.newBuilder();
		mb.setHeader(mhb.build());
		mb.setRaftmessage(rm.build());
		try {
			Channel ch = ConnectionManager.getConnection(mgmt.getHeader()
					.getOriginator(), true);
			if (ch != null)
				ch.writeAndFlush(mb.build());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;

	}

	// Response of Log Replication to Leader
	// can be success of failure
	private Management sendAppendResponseLog(Management mgmt) {
		RaftMessage.Builder rm = RaftMessage.newBuilder();
		MgmtHeader.Builder mhb = MgmtHeader.newBuilder();
		mhb.setTime(System.currentTimeMillis());
		mhb.setSecurityCode(-999); // TODO add security
		mhb.setOriginator(this.nodeId);
		mhb.setToNode(mgmt.getHeader().getOriginator());
		// Raft Message to be added
		rm.setTerm(term);
		rm.setSuccess(1);
		rm.setRaftAction(RaftAction.APPENDRESPONSE);
		Management.Builder mb = Management.newBuilder();
		mb.setHeader(mhb.build());
		mb.setRaftmessage(rm.build());

		try {
			Channel ch = ConnectionManager.getConnection(mgmt.getHeader()
					.getOriginator(), true);
			if (ch != null)
				ch.writeAndFlush(mb.build());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;

	}

	// Response of Vlaue to Leader
	// can be success of failure
	private Management sendAppendResponseValue(Management mgmt) {
		RaftMessage.Builder rm = RaftMessage.newBuilder();
		MgmtHeader.Builder mhb = MgmtHeader.newBuilder();
		mhb.setTime(System.currentTimeMillis());
		mhb.setSecurityCode(-999); // TODO add security
		mhb.setOriginator(this.nodeId);
		mhb.setToNode(mgmt.getHeader().getOriginator());
		// Raft Message to be added
		rm.setTerm(term);
		rm.setSuccess(1);
		rm.setRaftAction(RaftAction.APPENDRESPONSE);
		Management.Builder mb = Management.newBuilder();
		mb.setHeader(mhb.build());
		mb.setRaftmessage(rm.build());

		try {
			Channel ch = ConnectionManager.getConnection(mgmt.getHeader()
					.getOriginator(), true);
			if (ch != null)
				ch.writeAndFlush(mb.build());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;

	}

	private void receiveVote(RaftMessage rm) {
		logger.info("Size " + HeartbeatManager.getInstance().outgoingHB.size());
		if (++count > (HeartbeatManager.getInstance().outgoingHB.size() + 1) / 2) {
			logger.info("Final Count Received " + count);
			count = 0;
			currentState = RState.Leader;
			leaderId = this.nodeId;
			System.out.println(" Leader elected " + this.nodeId);
			ConnectionManager.broadcastAndFlush(sendLeaderMesssage());
		}
	}

	private Management sendLeaderMesssage() {
		RaftMessage.Builder rm = RaftMessage.newBuilder();
		MgmtHeader.Builder mhb = MgmtHeader.newBuilder();
		mhb.setTime(System.currentTimeMillis());
		mhb.setSecurityCode(-999); // TODO add security
		mhb.setOriginator(this.nodeId);

		// Raft Message to be added
		rm.setTerm(term);
		rm.setRaftAction(RaftAction.LEADER);

		Management.Builder mb = Management.newBuilder();
		mb.setHeader(mhb.build());
		mb.setRaftmessage(rm.build());

		return mb.build();
	}

	public Integer getElectionId() {
		if (current == null)
			return null;
		return current.electionID;
	}

	/**
	 * The ID of the election received by the node.
	 * 
	 * This could be different than the ID held by the election instance (this).
	 * If so, what do we do?
	 * 
	 * @param id
	 */
	@SuppressWarnings("unused")
	private void setElectionId(int id) {
		if (current != null) {
			if (current.electionID != id) {
				// need to resolve this!
			}
		}
	}

	/**
	 * whoami
	 * 
	 * @return
	 */
	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public synchronized void clear() {
		current = null;
	}

	public boolean isElectionInprogress() {
		return current != null;
	}

	private void notify(boolean success, Integer leader) {
		logger.info("In NOtify Method ");
		if (listener != null) {
			listener.concludeWith(success, leader);
			logger.info("Calling concludeWith");
		}
	}

	@SuppressWarnings("unused")
	private boolean updateCurrent(LeaderElection req) {
		boolean isNew = false;

		if (current == null) {
			current = new ElectionState();
			isNew = true;
		}

		current.electionID = req.getElectId();
		current.candidate = req.getCandidateId();
		current.desc = req.getDesc();
		current.maxDuration = req.getExpires();
		current.startedOn = System.currentTimeMillis();
		current.state = req.getAction();
		current.id = -1; // TODO me or sender?
		current.active = true;

		return isNew;
	}

	public Integer createElectionID() {
		return ElectionIDGenerator.nextID();
	}

	/**
	 * cast a vote based on what I know (my ID) and where the message has
	 * traveled.
	 * 
	 * This is not a pretty piece of code, nor is the problem as we cannot
	 * ensure consistent behavior.
	 * 
	 * @param mgmt
	 * @param isNew
	 * @return
	 */
	private synchronized Management castVote() {
		RaftMessage.Builder rm = RaftMessage.newBuilder();
		MgmtHeader.Builder mhb = MgmtHeader.newBuilder();
		mhb.setTime(System.currentTimeMillis());
		mhb.setSecurityCode(-999); // TODO add security
		mhb.setOriginator(this.nodeId);

		// Raft Message to be added
		rm.setTerm(term);
		rm.setRaftAction(RaftAction.VOTE);

		Management.Builder mb = Management.newBuilder();
		mb.setHeader(mhb.build());
		mb.setRaftmessage(rm.build());

		return mb.build();

	}

	public Integer getWinner() {
		if (current == null)
			return null;
		else if (current.state.getNumber() == ElectAction.DECLAREELECTION_VALUE)
			return current.candidate;
		else
			return null;
	}

	public RState getCurrentState() {
		return currentState;
	}

	public void setCurrentState(RState currentState) {
		this.currentState = currentState;
	}

	public int getLeaderId() {
		return leaderId;
	}

	public void setLeaderId(int leaderId) {
		this.leaderId = leaderId;
	}

	public int getTimeElection() {
		return timeElection;
	}

	public void setTimeElection(int timeElection) {
		this.timeElection = timeElection;
	}

	int i = 0;

	public Management sendAppendNotice() {
		i++;
		logger.info("" + i);
		if (i % 5 == 0) {
			logger.info("Leader Node " + this.nodeId
					+ " sending appendAction of VALUE RPC's");
			RaftMessage.Builder rm = RaftMessage.newBuilder();
			MgmtHeader.Builder mhb = MgmtHeader.newBuilder();
			mhb.setTime(System.currentTimeMillis());
			mhb.setSecurityCode(-999); // TODO add security
			mhb.setOriginator(this.nodeId);

			// Raft Message to be added
			rm.setTerm(term);
			rm.setRaftAction(RaftAction.APPEND);
			rm.setRaftAppendAction(RaftAppendAction.APPENDVALUE);
			Management.Builder mb = Management.newBuilder();
			mb.setHeader(mhb.build());
			mb.setRaftmessage(rm.build());
			return mb.build();
		} else if (i % 3 == 0) {
			logger.info("Leader Node " + this.nodeId
					+ " sending appendAction HB LOG RPC's");
			RaftMessage.Builder rm = RaftMessage.newBuilder();
			MgmtHeader.Builder mhb = MgmtHeader.newBuilder();
			mhb.setTime(System.currentTimeMillis());
			mhb.setSecurityCode(-999); // TODO add security
			mhb.setOriginator(this.nodeId);

			// Raft Message to be added
			rm.setTerm(term);
			rm.setRaftAction(RaftAction.APPEND);
			rm.setRaftAppendAction(RaftAppendAction.APPENDLOG);
			Management.Builder mb = Management.newBuilder();
			mb.setHeader(mhb.build());
			mb.setRaftmessage(rm.build());
			return mb.build();
		} else {
			logger.info("Leader Node " + this.nodeId
					+ " sending appendAction HB RPC's");
			RaftMessage.Builder rm = RaftMessage.newBuilder();
			MgmtHeader.Builder mhb = MgmtHeader.newBuilder();
			mhb.setTime(System.currentTimeMillis());
			mhb.setSecurityCode(-999); // TODO add security
			mhb.setOriginator(this.nodeId);

			// Raft Message to be added
			rm.setTerm(term);
			rm.setRaftAction(RaftAction.APPEND);
			rm.setRaftAppendAction(RaftAppendAction.APPENDHEARTBEAT);
			Management.Builder mb = Management.newBuilder();
			mb.setHeader(mhb.build());
			mb.setRaftmessage(rm.build());
			return mb.build();
		}
	}

	public RaftMonitor getMonitor() {
		return monitor;
	}

	public class RaftMonitor extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (currentState == RState.Leader)
					ConnectionManager.broadcastAndFlush(sendAppendNotice());
				else {
					boolean blnStartElection = RaftManager.getInstance()
							.assessCurrentState();
					if (blnStartElection) {
						long now = System.currentTimeMillis();
						if ((now - lastKnownBeat) > timeElection)
							startElection();
					}
				}
			}
		}
	}

	private void startElection() {
		System.out.println("Timeout! Election declared by node " + getNodeId()
				+ "for term " + (term + 1));
		// Declare itself candidate, vote for self and Broadcast request for
		// votes To begin an election, a follower increments its current
		// term and transitions to candidate state. It then votes for
		// itself and issues RequestVote RPCs in parallel to each of
		// the other servers in the cluster.
		lastKnownBeat = System.currentTimeMillis();
		currentState = RState.Candidate;
		count = 1;
		term++;
		logger.info("size of nodes "
				+ HeartbeatManager.getInstance().outgoingHB.size());
		if (HeartbeatManager.getInstance().outgoingHB.size() == 0) {
			// logger.info("size of nodes.! IN IF="+HeartbeatManager.getInstance().outgoingHB.size());
			count = 0;
			currentState = RState.Leader;
			leaderId = this.nodeId;
			logger.info(" Leader elected " + this.nodeId);
			ConnectionManager.broadcastAndFlush(sendLeaderMesssage());
		}

		else {
			logger.info("size of nodes IN ELSE.!="
					+ HeartbeatManager.getInstance().outgoingHB.size());
			ConnectionManager.broadcastAndFlush(sendRequestVoteNotice());
		}

	}

	private Management sendRequestVoteNotice() {
		RaftMessage.Builder rm = RaftMessage.newBuilder();
		MgmtHeader.Builder mhb = MgmtHeader.newBuilder();
		mhb.setTime(System.currentTimeMillis());
		mhb.setSecurityCode(-999); // TODO add security
		mhb.setOriginator(this.nodeId);

		// Raft Message to be added
		rm.setTerm(term);
		rm.setRaftAction(RaftAction.REQUESTVOTE);

		Management.Builder mb = Management.newBuilder();
		mb.setHeader(mhb.build());
		mb.setRaftmessage(rm.build());

		return mb.build();

	}
}
