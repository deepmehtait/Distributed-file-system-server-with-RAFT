package poke.server.managers;

import io.netty.channel.Channel;

import java.beans.Beans;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.core.Mgmt.Management;
import poke.core.Mgmt.MgmtHeader;
import poke.core.Mgmt.RaftMessage;
import poke.core.Mgmt.RaftMessage.RaftAction;
import poke.server.conf.ServerConf;
import poke.server.election.Election;
import poke.server.election.ElectionListener;
import poke.server.election.RaftElection;
import poke.server.managers.ConnectionManager.connectionState;

public class RaftManager implements ElectionListener {

	protected static Logger logger = LoggerFactory.getLogger("election");
	protected static AtomicReference<RaftManager> instance = new AtomicReference<RaftManager>();

	private static ServerConf conf;
	private static long lastKnownBeat = System.currentTimeMillis();
	// number of times we try to get the leader when a node starts up
	private int firstTime = 2;
	/** The election that is in progress - only ONE! */
	private Election election;
	private int electionCycle = -1;
	private Integer syncPt = 1;
	/** The leader */
	Integer leaderNode;

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		RaftManager.logger = logger;
	}

	public static RaftManager getInstance() {
		return instance.get();
	}

	public static void setInstance(AtomicReference<RaftManager> instance) {
		RaftManager.instance = instance;
	}

	public static ServerConf getConf() {
		return conf;
	}

	public static void setConf(ServerConf conf) {
		RaftManager.conf = conf;
	}

	public static long getLastKnownBeat() {
		return lastKnownBeat;
	}

	public static void setLastKnownBeat(long lastKnownBeat) {
		RaftManager.lastKnownBeat = lastKnownBeat;
	}

	public int getFirstTime() {
		return firstTime;
	}

	public void setFirstTime(int firstTime) {
		this.firstTime = firstTime;
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	public int getElectionCycle() {
		return electionCycle;
	}

	public void setElectionCycle(int electionCycle) {
		this.electionCycle = electionCycle;
	}

	public Integer getSyncPt() {
		return syncPt;
	}

	public void setSyncPt(Integer syncPt) {
		this.syncPt = syncPt;
	}

	public Integer getLeaderNode() {
		return leaderNode;
	}

	public void setLeaderNode(Integer leaderNode) {
		this.leaderNode = leaderNode;
	}

	public static RaftManager initManager(ServerConf conf) {
		RaftManager.conf = conf;
		instance.compareAndSet(null, new RaftManager());
		return instance.get();
	}

	public Integer whoIsTheLeader() {
		return this.leaderNode;
	}

	public void processRequest(Management mgmt) {
		if (!mgmt.hasRaftmessage())
			return;

		RaftMessage rm = mgmt.getRaftmessage();

		// when a new node joins the network it will want to know who the leader
		// is - we kind of ram this request-response in the process request
		// though there has to be a better place for it
		if (rm.getRaftAction().getNumber() == RaftAction.WHOISTHELEADER_VALUE) {
			respondToWhoIsTheLeader(mgmt);
			return;
		}
		// else if (rm.getRaftAction().getNumber() ==
		// RaftAction.THELEADERIS_VALUE) {
		// logger.info("Node " + conf.getNodeId()
		// + " got an answer on who the leader is. Its Node "
		// + rm.getLeader());
		// this.leaderNode = rm.getLeader();
		// return;
		// }

		// else fall through to an election

		Management rtn = electionInstance().process(mgmt);
		if (rtn != null)
			ConnectionManager.broadcastAndFlush(rtn);
	}

	/**
	 * check the health of the leader (usually called after a HB update)
	 * 
	 * @param mgmt
	 */
	public boolean assessCurrentState() {
		// logger.info("RaftManager.assessCurrentState() checking elected leader status");

		if (firstTime > 0 && ConnectionManager.getNumMgmtConnections() > 0) {
			// give it two tries to get the leader
			this.firstTime--;
			logger.info("In assessCurrentState() if condition");
			askWhoIsTheLeader();
			return false;
		}

		else {
			// if this is not an election state, we need to assess the H&S of //
			// the network's leader
			// synchronized (syncPt) {
			// long now = System.currentTimeMillis();
			// if(now-lastKnownBeat>1000)
			// startElection();
			// }
			return true;
		}

	}

	/** election listener implementation */
	public void concludeWith(boolean success, Integer leaderID) {
		if (success) {
			logger.info("----> the leader is " + leaderID);
			this.leaderNode = leaderID;
		}

		election.clear();
	}

	private void respondToWhoIsTheLeader(Management mgmt) {
		if (this.leaderNode == null) {
			logger.info("----> I cannot respond to who the leader is! I don't know!");
			return;
		}

		logger.info("Node " + conf.getNodeId() + " is replying to "
				+ mgmt.getHeader().getOriginator()
				+ "'s request who the leader is. Its Node " + this.leaderNode);

		MgmtHeader.Builder mhb = MgmtHeader.newBuilder();
		mhb.setOriginator(conf.getNodeId());
		mhb.setTime(System.currentTimeMillis());
		mhb.setSecurityCode(-999); // TODO add security

		RaftMessage.Builder rmb = RaftMessage.newBuilder();
		rmb.setLeader(this.leaderNode);
		rmb.setRaftAction(RaftAction.THELEADERIS);

		Management.Builder mb = Management.newBuilder();
		mb.setHeader(mhb.build());
		mb.setRaftmessage(rmb);
		try {

			Channel ch = ConnectionManager.getConnection(mgmt.getHeader()
					.getOriginator(), connectionState.SERVERMGMT);
			if (ch != null)
				ch.writeAndFlush(mb.build());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void askWhoIsTheLeader() {
		logger.info("Node " + conf.getNodeId() + " is searching for the leader");
		/*
		 * MgmtHeader.Builder mhb = MgmtHeader.newBuilder();
		 * mhb.setOriginator(conf.getNodeId());
		 * mhb.setTime(System.currentTimeMillis()); mhb.setSecurityCode(-999);
		 * // TODO add security
		 * 
		 * RaftMessage.Builder rmb = RaftMessage.newBuilder();
		 * rmb.setRaftAction(RaftAction.WHOISTHELEADER);
		 * 
		 * Management.Builder mb = Management.newBuilder();
		 * mb.setHeader(mhb.build()); mb.setRaftmessage(rmb); Channel ch =
		 * ConnectionManager.getConnection(conf.getNodeId(),true); if(ch!=null){
		 * // now send it to the requester
		 * ConnectionManager.broadcastAndFlush(mb.build()); }
		 */
		if (whoIsTheLeader() == null) {
			logger.info("----> I cannot find the leader is! I don't know!");
			return;
		} else {
			logger.info("The Leader is " + this.leaderNode);
		}

	}

	private Election electionInstance() {
		if (election == null) {
			synchronized (syncPt) {
				if (election != null)
					return election;

				// new election
				String clazz = RaftManager.conf.getElectionImplementation();

				// if an election instance already existed, this would
				// override the current election
				try {
					election = (Election) Beans.instantiate(this.getClass()
							.getClassLoader(), clazz);
					election.setNodeId(conf.getNodeId());
					election.setListener(this);
				} catch (Exception e) {
					logger.error("Failed to create " + clazz, e);
				}
			}
		}

		return election;

	}

	/**********Leader ELection 29th March 2015******************************
	 * This function will call the run method of Raft Monitor class in the election instance. 
	 * In our case RaftElection --> Raft Monitor
	 */
	public void startMonitor() {		
		logger.info("Raft Monitor Started ");
		if (election == null)
			((RaftElection) electionInstance()).getMonitor().start();

	}

}
