package gash.leaderelection.raft;

import gash.messaging.Message;
import gash.messaging.Message.Delivery;
import gash.messaging.Node;
import gash.messaging.transports.Bus;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Raft consensus algorithm is similar to PAXOS and Flood Max though it
 * claims to be easier to understand. The paper "In Search of an Understandable
 * Consensus Algorithm" explains the concept. See
 * https://ramcloud.stanford.edu/raft.pdf
 * 
 * Note the Raft algo is both a leader election and consensus algo. It ties the
 * election process to the state of its distributed log (state) as the state is
 * part of the decision process of which node should be the leader.
 * 
 * 
 * @author gash
 *
 */
public class Raft {
	static AtomicInteger msgID = new AtomicInteger(0);

	private Bus<? extends RaftMessage> transport;

	public Raft() {
		transport = new Bus<RaftMessage>(0);
	}

	public void addNode(RaftNode node) {
		if (node == null)
			return;

		node.setTransport(transport);

		@SuppressWarnings({ "rawtypes", "unchecked" })
		Node<Message> n = (Node) (node);
		transport.addNode(n);

		if (!node.isAlive())
			node.start();
	}

	/** processes heartbeats */
	public interface HeartMonitorListener {
		public void doMonitor();
	}

	public static abstract class LogEntryBase {
		private int term;
	}

	private static class LogEntry extends LogEntryBase {

	}

	/** triggers monitoring of the heartbeat */
	public static class RaftMonitor extends TimerTask {
		private RaftNode<RaftMessage> node;

		public RaftMonitor(RaftNode<RaftMessage> node) {
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
	public static class RaftNode<M extends RaftMessage> extends Node<M> {
		public enum RState {
			Follower, Candidate, Leader
		}

		private Timer timer;
		private RaftMonitor monitor;
		private RState state = RState.Follower;
		private int leaderID = -1;
		private long lastKnownBeat;
		private int beatSensitivity = 3; // 2 misses
		private int beatDelta = 3000; // 3 seconds
		private int beatCounter = 0;
		private int timeElection;
		private int term;
		private int voteCount;
		private RaftMessage votedFor;

		private Bus<? extends RaftMessage> transport;

		public RaftNode(int id) {
			super(id);
			this.timeElection = new Random().nextInt(10000);
			if (this.timeElection < 5000)
				this.timeElection += 3000;
			System.out.println("TimeElection : " + id + " - " + timeElection);
			state = RState.Follower;
		}

		@SuppressWarnings("unchecked")
		public void start() {
			if (this.timer != null)
				return;
			monitor = new RaftMonitor((RaftNode<RaftMessage>) this);
			int freq = (int) (timeElection * .75);
			if (freq == 0)
				freq = 1;

			timer = new Timer();
			timer.scheduleAtFixedRate(monitor, timeElection * 2, freq);
			super.start();
		}

		int counter = 0;

		protected void checkBeats() {
			System.out.println("--> node " + getNodeId() + " heartbeat");
			if (state == RState.Leader) {
				if (counter == 3) {
					System.out
							.println("leader state now we change it to follower to start election again"); // startElection();

				} else {

					sendAppendNotice();
					counter++;
				}

			} else {
				long now = System.currentTimeMillis();
				if (now - lastKnownBeat > timeElection)
					startElection();
			}
		}

		private void startElection() {
			System.out.println("Timeout! Election declared by node "
					+ getNodeId() + "for term " + (term + 1));
			// Declare itself candidate, vote for self and Broadcast request for
			// votes
			state = RState.Candidate;
			voteCount = 1;
			term++;
			sendRequestVoteNotice();

		}

		private void sendLeaderNotice() {
			RaftMessage msg = new RaftMessage(Raft.msgID.incrementAndGet());
			msg.setOriginator(getNodeId());
			msg.setDeliverAs(Delivery.Broadcast);
			msg.setDestination(-1);
			msg.setAction(RaftMessage.Action.Leader); /*
													 * msg.setAction(RaftMessage.
													 * Action.Append)
													 */
			msg.setTerm(term);
			send(msg);
		}

		private void sendAppendNotice() {
			RaftMessage msg = new RaftMessage(Raft.msgID.incrementAndGet());
			msg.setOriginator(getNodeId());
			msg.setDeliverAs(Delivery.Broadcast);
			msg.setDestination(-1);
			msg.setAction(RaftMessage.Action.Append);
			msg.setTerm(term);
			send(msg);
		}

		/** TODO args should set voting preference */
		private void sendRequestVoteNotice() {
			RaftMessage msg = new RaftMessage(Raft.msgID.incrementAndGet());
			msg.setOriginator(getNodeId());
			msg.setDeliverAs(Delivery.Broadcast);
			msg.setDestination(-1);
			msg.setAction(RaftMessage.Action.RequestVote);// msg.setAction(RaftMessage.Action.Append);
			msg.setTerm(term);
			send(msg);
		}

		private void send(RaftMessage msg) {
			// enqueue the message - if we directly call the nodes method, we
			// end up with a deep call stack and not a message-based model.
			transport.sendMessage(msg);
		}

		/** this is called by the Node's run() - reads from its inbox */
		@Override
		public void process(RaftMessage msg) {
			// TODO process

			// TODO process
			RaftMessage.Action action = msg.getAction();
			switch (action) {
			case Append:
				if (state == RState.Candidate) {
					if (msg.getTerm() >= term) {
						this.term = msg.getTerm();
						leaderID = msg.getOriginator();
						state = RState.Follower;
					}
				} else if (state == RState.Follower) {
					this.term = msg.getTerm();
					lastKnownBeat = System.currentTimeMillis();
					// TODO append work
				} else if (state == RState.Leader) {
					// should not happen
				}
				break;
			case RequestVote:
				if (state == RState.Candidate) {
					// candidate ignores other vote requests
				} else if (state == RState.Follower) {
					/**
					 * check if already voted for this term or else vote for the
					 * candidate
					 **/
					if (votedFor == null || msg.getTerm() > votedFor.getTerm()) {
						if (votedFor != null) {
							System.out.println("Voting for "
									+ msg.getOriginator() + " for term"
									+ msg.getTerm() + " from node " + nodeId
									+ " voted term " + votedFor.getTerm());
						} else {
							System.out.println("Node " + nodeId
									+ " Voting for first time for "
									+ msg.getOriginator());
						}

						votedFor = msg;
						voteForCandidate(msg);
					}
				} else if (state == RState.Leader) {
					// TODO
				}
				break;
			case Leader:
				if (msg.getTerm() > this.term) {
					leaderID = msg.getOriginator();
					this.term = msg.getTerm();
					lastKnownBeat = System.currentTimeMillis();
				}
				break;
			case Vote:
				if (state == RState.Candidate) {
					receiveVote(msg);
				} else if (state == RState.Follower) {

				} else if (state == RState.Leader) {

				}
				break;
			default:
			}
		}

		private void voteForCandidate(RaftMessage voteRequest) {

			RaftMessage msg = new RaftMessage(Raft.msgID.incrementAndGet());
			msg.setOriginator(getNodeId());
			msg.setDeliverAs(Delivery.Direct);
			msg.setDestination(voteRequest.getOriginator());
			msg.setTerm(term);
			msg.setAction(RaftMessage.Action.Vote);
			send(msg);
		}

		public void receiveVote(RaftMessage msg) {
			System.out.println("Vote received at node " + getNodeId()
					+ " votecount " + voteCount);
			if (++voteCount > 3) {
				voteCount = 0;
				sendLeaderNotice();
				state = RState.Leader;
				leaderID = getNodeId();
				System.out.println(" Leader elected " + getNodeId());
			}
		}

		public void setTransport(Bus<? extends RaftMessage> t) {
			this.transport = t;
		}
	}
}