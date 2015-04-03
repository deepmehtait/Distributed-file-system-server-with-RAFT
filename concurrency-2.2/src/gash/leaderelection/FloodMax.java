package gash.leaderelection;

import gash.messaging.Message;
import gash.messaging.Message.Delivery;
import gash.messaging.Message.Direction;
import gash.messaging.RingNode;
import gash.messaging.transports.Ring;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Flood Max election algorithm uses a synchronized broadcast of heartbeats
 * and challenges to ensure the leader is alive and operating.
 * 
 * FM works by the leader sending heartbeat (HB) messages to known nodes of a
 * network. If a non-leader node (node) receives a heartbeat, it updates its
 * internal state. If a node detects (i.e. a thread) that a HB(s) was not
 * received, it will issue an election. An election is broadcast (flood the
 * network) to all nodes. A node will compare the election candidate with its
 * ID, and will do one of two responses. 1) if the ID is less than its ID, it
 * will broadcast an election with its ID. 2) if the ID is greater than its ID,
 * it will store and forward the election message. An election is over when a
 * node that sent its ID, receives a message with its own ID. Typically, the
 * node with the highest ID wins the election.
 * 
 * @author gash
 *
 */
public class FloodMax {
	static AtomicInteger msgID = new AtomicInteger(0);

	private Ring transport;

	/** creates and starts the network */
	public FloodMax(int numNodes) throws Exception {
		transport = new Ring(0);

		for (int n = 0; n < numNodes; n++) {
			FloodNode fn = new FloodNode(n);
			fn.start();

			transport.addNode(fn);
		}

		// setup the ring
		RingNode[] list = transport.getNodes();
		for (int n = 0; n < numNodes; n++) {
			if (n + 1 < numNodes)
				list[n].setNext(list[n + 1]);

			if (n - 1 >= 0)
				list[n].setPrevious(list[n - 1]);
		}

		// close the ring
		list[0].setPrevious(list[numNodes - 1]);
		list[numNodes - 1].setNext(list[0]);
	}

	/** processes heartbeats */
	public interface HeartMonitorListener {
		public void doMonitor();
	}

	/** notices on election */
	public static class FloodMsg extends Message {
		private FloodNode.FMState state;

		// message id
		public FloodMsg(int id) {
			super(id);
		}

		public FloodNode.FMState getState() {
			return state;
		}

		public void setState(FloodNode.FMState state) {
			this.state = state;
		}
	}

	/** triggers monitoring of the heartbeat */
	public static class FloodMonitor extends TimerTask {
		private FloodNode node;

		public FloodMonitor(FloodNode node) {
			if (node == null)
				throw new RuntimeException("Missing node");

			this.node = node;
		}

		@Override
		public void run() {
			node.checkBeats();
		}
	}

	/** our network node */
	public static class FloodNode extends RingNode {
		public enum FMState {
			Member, Candidate, Leader
		}

		private FMState state = FMState.Member;
		private int leaderID = -1;
		private long lastKnownBeat;
		private int beatSensitivity = 3; // 2 misses
		private int beatDelta = 3000; // 3 seconds
		private int beatCounter = 0;
		private Timer timer;
		private FloodMonitor monitor;

		/** accept defaults */
		public FloodNode(int ID) {
			super(ID);
		}

		public FloodNode(int ID, int beatSensitivity, int beatDelta) {
			super(ID);
			this.beatSensitivity = beatSensitivity;
			this.beatDelta = beatDelta;
		}

		public void start() {
			if (this.timer != null)
				return;

			monitor = new FloodMonitor(this);

			// allow the threads to start before checking HB. Also, the
			// beatDelta should be slightly greater than the frequency in which
			// messages are emitted to reduce false positives.
			int freq = (int) (beatDelta * .75);
			if (freq == 0)
				freq = 1;

			timer = new Timer();
			timer.scheduleAtFixedRate(monitor, beatDelta * 2, freq);

			super.start();
		}

		public void receiveBeat(int fromID) {
			if (leaderID == fromID) {
				lastKnownBeat = System.currentTimeMillis();
				beatCounter = 0;
			} else {
				// this can occur if the node joins the network or a partition
				// created two leaders.
			}
		}

		protected void checkBeats() {
			System.out.println("--> node " + getNodeId() + " heartbeat (counter = " + beatCounter + ")");

			// leader must sent HB to other nodes otherwise an election will
			// start
			if (this.leaderID == getNodeId()) {
				leaderID = -1;
				beatCounter = 0;
				sendLeaderNotice();
				return;
			} else if (state == FloodNode.FMState.Candidate) {
				// ignore triggers from HB as we are in an election
				beatCounter = 0;
				return;
			}

			long now = System.currentTimeMillis();
			if (now - lastKnownBeat > beatDelta && state != FloodNode.FMState.Candidate) {
				beatCounter++;
				if (beatCounter > beatSensitivity) {
					System.out.println("--> node " + getNodeId() + " starting an election");
					// leader is dead! Long live me!
					state = FMState.Candidate;
					sendElectionNotice(null);
				}
			}
		}

		/**
		 * this serves two purposes 1) HBs pass through this message, and 2)
		 * elections are processed here as well.
		 * 
		 * @param msg
		 */
		private void receiveElectionNotice(FloodMsg msg) {
			if (msg.getState() == FloodNode.FMState.Candidate) {
				// election in progress
				if (msg.getOriginator() == getNodeId() && leaderID == -1) {
					// I'm the leader, and only say it once!
					System.out.println("--> node " + getNodeId() + " declares itself to be the leader");

					state = FloodNode.FMState.Leader;
					leaderID = getNodeId();

					sendLeaderNotice();
				} else if (msg.getOriginator() < getNodeId()) {
					// I'm a better candidate
					System.out
							.println("--> node " + getNodeId() + " is a better candidate than " + msg.getOriginator());

					state = FloodNode.FMState.Candidate;

					sendElectionNotice(null);
				} else if (msg.getOriginator() > getNodeId()) {
					System.out.println("--> node " + getNodeId() + " forwarding candidate " + msg.getOriginator());

					state = FloodNode.FMState.Candidate;

					sendElectionNotice(msg);
				}
			} else if (msg.getState() == FloodNode.FMState.Leader) {
				// leader declared or HB!
				System.out.println("--> node " + getNodeId() + " acknowledges the leader is " + msg.getOriginator());

				// things to do when we get a HB
				leaderID = msg.getOriginator();
				lastKnownBeat = System.currentTimeMillis();
				state = FloodNode.FMState.Member;
				beatCounter = 0;

				// since we are ring, messages must be forwarded
				if (msg.getOriginator() != getNodeId())
					sendElectionNotice(msg);
			}
		}

		private void sendElectionNotice(FloodMsg originalMsg) {
			if (originalMsg == null) {
				FloodMsg msg = new FloodMsg(FloodMax.msgID.incrementAndGet());
				msg.setOriginator(getNodeId());
				msg.setDirection(Direction.Forward);
				msg.setDeliverAs(Delivery.Direct);
				msg.setDestination(this.getNext().getNodeId());
				msg.state = FloodNode.FMState.Candidate;
				send(msg);
			} else {
				originalMsg.setDestination(this.getNext().getNodeId());
				send(originalMsg);
			}
		}

		private void sendLeaderNotice() {
			FloodMsg msg = new FloodMsg(FloodMax.msgID.incrementAndGet());
			msg.setOriginator(getNodeId());
			msg.setDirection(Direction.Forward);
			msg.setDeliverAs(Delivery.Direct);
			msg.setDestination(this.getNext().getNodeId());
			msg.state = FloodNode.FMState.Leader;
			send(msg);
		}

		private void send(FloodMsg msg) {
			// enqueue the message - if we directly call the nodes method, we
			// end up with a deep call stack and not a message-based model.
			this.getNext().message(msg);
		}

		/** this is called by the Node's run() - reads from its inbox */
		@Override
		public void process(Message msg) {
			receiveElectionNotice((FloodMsg) msg);
		}
	}
}
