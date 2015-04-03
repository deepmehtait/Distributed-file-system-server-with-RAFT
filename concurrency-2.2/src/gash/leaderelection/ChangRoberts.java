package gash.leaderelection;

import java.util.concurrent.atomic.AtomicInteger;

import gash.messaging.Message;
import gash.messaging.Node;
import gash.messaging.transports.Bus;

/**
 * Bully demonstrates the bully algorithm for election of a leader.
 * 
 * The algorithm uses an approach where the largest ID of a process wins.
 * 
 * Steps:
 * 
 * 1) Nodes monitor the health of the leader node (heart beat messages)
 * 
 * 2) On a heart beat (time delta) failure or no leader was specified, as in a
 * startup situation, a monitoring node will broadcast a challenge for
 * leadership (sending its ID) to all nodes.
 * 
 * 3) Message traffic is reduced (stops challenges) when a node receives a
 * challenge from a node with a larger ID.
 * 
 * 4) If a node receives a challenge from a node with a lower ID, it must
 * immediately send out a challenge.
 * 
 * 5) If no challenges are forthcoming (time delta) then the highest node
 * declares itself as the leader.
 * 
 * Notes:
 * 
 * 1) Alternate approaches could only send messages to nodes having a higher ID;
 * assumes an ordered network. This example assumes random ordering (e.g., a transport
 * or adhoc network) and broadcasts to all nodes.
 * 
 * 2) The initial broadcast has the benefit in alerting all nodes to enter into
 * challenge mode.
 * 
 * 3) This implementation does a broadcast to all nodes; whereas the algo should
 * only send to nodes with an ID greater than the sender's ID
 * 
 * @author gash1
 * 
 */
public class ChangRoberts {
	static AtomicInteger msgID = new AtomicInteger(0);

	private Bus<? extends ChangMessage> transport;

	public ChangRoberts() {
		transport = new Bus<ChangMessage>(0);
	}

	public void addNode(ChangNode node) {
		if (node == null)
			return;

		node.setTransport(transport);

		@SuppressWarnings({ "rawtypes", "unchecked" })
		Node<Message> n = (Node) (node);
		transport.addNode(n);
	}

	/**
	 * represents a node in the network. Class only represents leader monitoring
	 * and election process. This would exist as a separate communication path
	 * or embedded within the main communication traffic. If embedded, a
	 * priority for reading leader messages should be used.
	 * 
	 * @author gash1
	 * 
	 */
	public static class ChangNode extends Node<ChangMessage> {
		private static final long sWaitFor = 3000;

		protected Bus<? extends ChangMessage> bus;
		protected int delay = 500;

		protected int leader = -1;
		protected long leaderWait = 0;
		protected int challengeLeader = 0;
		protected long challengeWait = 0;

		// HACK to ensure leader election in a stable system
		private int leaderLife = 4;

		public ChangNode(int id) {
			super(id);

			this.leader = ChangMessage.sNobody;
			this.leaderWait = 0;
			this.challengeLeader = ChangMessage.sNobody;
			this.challengeWait = 0;

			// Heart beat: monitor leader health and election process
			this.delay = 1000;
		}

		public void setTransport(Bus<? extends ChangMessage> bus) {
			this.bus = bus;
		}

		@Override
		public void run() {
			try {
				while (true) {
					sleep(delay);

					while (inbox.size() > 0) {
						ChangMessage msg = inbox.remove(0);
						process(msg);
					}

					if (leader != ChangMessage.sNobody)
						leaderAck();
					else
						checkChallengeStatus();

					checkLeaderStatus();

					// HACK to demonstrate alive heart-beat failing and the
					// nodes go into a leader election mode
					if (leader == getNodeId()) {
						leaderLife--;
						if (leaderLife == 0) {
							System.out.println("*** " + getNodeId() + " retiring as leader ***");
							return;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void message(ChangMessage msg) {
			inbox.add(msg);
		}

		public void process(ChangMessage msg) {
			if (msg.getAction() == ChangMessage.Action.LeaderAlive) {
				leaderWait = System.currentTimeMillis();
				leader = msg.getOriginator();
			} else if (msg.getAction() == ChangMessage.Action.LeaderElection) {
				leader = ChangMessage.sNobody;
				leaderWait = 0;

				// the challenge leader is used to only issue challenges if the
				// challenger (ID) is less than myself
				challengeLeader = Math.max(challengeLeader, msg.getOriginator());
				System.out.println("   " + getNodeId() + " accepts " + challengeLeader + " as potiental leader");

				if (challengeLeader < getNodeId())
					issueChallenge();
			} else if (msg.getAction() == ChangMessage.Action.LeaderDeclared) {
				// System.out.println("leader declared: " +
				// msg.getOrigination());
				leader = msg.getOriginator();
				leaderWait = System.currentTimeMillis();
				challengeWait = 0;
				challengeLeader = ChangMessage.sNobody;
			} else {
				// normal message processing goes here
			}
		}

		private void leaderAck() {
			if (leader != getNodeId())
				return;

			// slow checking down as we are not electing a leader
			delay = 2000;

			// I'm the leader
			System.out.println("* I'm the leader: " + getNodeId() + " *");
			ChangMessage m = new ChangMessage(ChangRoberts.msgID.incrementAndGet());
			m.setOriginator(getNodeId());
			m.setAction(ChangMessage.Action.LeaderAlive);
			bus.sendMessage(m);
		}

		private void checkLeaderStatus() {
			if (challengeLeader != ChangMessage.sNobody)
				return;
			else if (System.currentTimeMillis() - leaderWait > sWaitFor) {
				if (leaderWait != 0)
					System.out.println("! " + getNodeId() + " declares the leader dead !");

				// assume leader is dead (or startup)
				leader = ChangMessage.sNobody;
				issueChallenge();
			}
		}

		private void checkChallengeStatus() {
			if (challengeLeader == -1 || challengeLeader != getNodeId())
				return;

			// I'm the current potiental leader
			if (System.currentTimeMillis() - challengeWait > sWaitFor) {
				System.out.println("   " + getNodeId() + " declaring self as leader");
				ChangMessage m = new ChangMessage(ChangRoberts.msgID.incrementAndGet());
				m.setOriginator(getNodeId());
				m.setAction(ChangMessage.Action.LeaderDeclared);
				bus.sendMessage(m);
			}
		}

		private void issueChallenge() {
			System.out.println(getNodeId() + " issues a challenge");

			// entering leader election mode - want to process messages faster
			delay = 500;

			// issue a challenge
			challengeLeader = getNodeId();
			challengeWait = System.currentTimeMillis(); // countdown
			ChangMessage m = new ChangMessage(ChangRoberts.msgID.incrementAndGet());
			m.setOriginator(getNodeId());
			m.setAction(ChangMessage.Action.LeaderElection);
			bus.sendMessage(m);
		}
	}

	/**
	 * message sent through the network
	 * 
	 * @author gash1
	 * 
	 */
	public static class ChangMessage extends Message {
		public static final int sNobody = -1;

		public enum Action {
			LeaderAlive, LeaderElection, LeaderDeclared
		}

		private Action action;
		private int leader;

		public ChangMessage(int msgId) {
			super(msgId);
		}

		public int getLeader() {
			return leader;
		}

		public void setLeader(int leader) {
			this.leader = leader;
		}

		public Action getAction() {
			return action;
		}

		public void setAction(Action action) {
			this.action = action;
		}
	}
}
