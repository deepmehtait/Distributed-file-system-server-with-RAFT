package gash.consensus.paxos;

import gash.consensus.paxos.core.LeaderMessage;
import gash.consensus.paxos.core.LeaderMessage.LeaderState;
import gash.consensus.paxos.core.Proposal;
import gash.consensus.paxos.core.Proposal.ProposalState;
import gash.consensus.paxos.core.ProposalMessage;
import gash.consensus.paxos.core.ProposalTracker;
import gash.consensus.paxos.core.Request;
import gash.consensus.paxos.core.RequestMessage;
import gash.messaging.Message;
import gash.messaging.Message.Delivery;
import gash.messaging.Message.Direction;
import gash.messaging.RingNode;

import java.util.Date;
import java.util.Random;

/**
 * This PAXOS demonstration is really a basic implementation. There are
 * shortcomings to this example that would prevent it from being production
 * ready. These omissions reduce the complexity to allow one to understand the
 * states of a PAXOS commit sequence.
 * 
 * Omissions include:
 * <ul>
 * <li>separate processes
 * <li>handling of multiple proposers
 * <li>no proposers
 * <li>digital signing and other methods to prevent impostor node messages
 * <li>network discovery/mapping/messaging
 * <li>leader election
 * <li>loss of leader during an active proposal
 * <li>logging
 * <li>abstraction of requests
 * <li>startup and shutdown
 * </ul>
 * 
 * @author gash
 * 
 */
public class PaxosNode extends RingNode {
	public enum Role {
		Proposer, Acceptor, Learner
	}

	// For demonstration only we need to create some randomness to the nodes as
	// well as change their behavior.
	private Random rand = new Random(System.currentTimeMillis());
	private boolean reliable = true;

	// the role this node takes on. In a real system, roles are likely
	// represented as separate classes. For this demonstration we keep them in
	// one class for debugging and ease of use.
	private Role role;

	// message incrementing
	private int lastMessageID;

	// As an acceptor, this is the proposal I'm voting on.
	Proposal currentProposal;

	// As the proposer this is what I am proposing (note this example is limited
	// to only one proposal at a time!)
	ProposalTracker trackProposal;

	// As a proposer, this to increment proposals
	private int lastProposalID;

	public PaxosNode(int ID) {
		super(ID);
	}

	/**
	 * demo only! this will make acceptors randomly drop requests
	 */
	public void demoUnreliable() {
		reliable = false;
	}

	/**
	 * demo only! this is used in place of leader elections
	 */
	public void demoDeclareAsLeader() {
		LeaderMessage lm = new LeaderMessage(++lastMessageID);
		lm.setDeliverAs(Delivery.Direct);
		lm.setOriginator(this.getNodeId());
		lm.setDestination(this.getNodeId());
		lm.setDirection(Direction.Forward);
		lm.setState(LeaderState.IamTheLeader);

		sendMessage(lm);
	}

	/**
	 * a client will make a request for a job/resource/etc from this method
	 * 
	 * @param req
	 */
	public void submitRequest(Request req) {
		if (req == null)
			return;

		RequestMessage msg = new RequestMessage(100);
		msg.setOriginator(this.getNodeId());
		msg.setDestination(this.getNodeId());
		msg.setDeliverAs(Delivery.Direct);
		msg.setDirection(Direction.Forward);
		msg.setRequest(req);

		sendMessage(msg);
	}

	public void setRole(Role role) {
		this.role = role;

		reset();
	}

	/**
	 * full reset of a node's tracking - should not be called unless things are
	 * really messed up
	 */
	private void reset() {
		trackProposal = null;
		currentProposal = null;
		lastMessageID = 0;
		lastProposalID = 0;
	}

	/**
	 * routing of messages sent to the node
	 */
	@Override
	public synchronized void process(Message msg) {
		if (msg == null)
			return;

		if (msg instanceof ProposalMessage)
			manageProposals((ProposalMessage) msg);
		else if (msg instanceof RequestMessage) {
			if (role == Role.Proposer) {
				newProposal((TextRequest) ((RequestMessage) msg).getRequest());
			} else {
				// forward message to a proposer
				// TODO if there are no proposers the message needs to have a
				// TTL!
				log("forwarding the request to a proposer");
				sendMessage(msg);
			}
		} else if (msg instanceof LeaderMessage)
			manageLeader((LeaderMessage) msg);
		else {
			log("ignoring: " + msg);
		}
	}

	/**
	 * process messages for leader elections and heartbeats
	 * 
	 * We can use the quorum feature of PAXOS to perform leader election.
	 * However, it has been isolated from the PAXOS logic to hopefully make
	 * things cleaner/clearer
	 * 
	 * @param lm
	 */
	private void manageLeader(LeaderMessage lm) {
		if (lm.getState() == LeaderState.Heartbeat) {
			// this message is annoying - uncomment to see the heart beat
			// log(lm.toString());

			// TODO as an acceptor, I need to track the leader
			if (lm.getOriginator() != this.getNodeId())
				sendMessage(lm);
		} else if (lm.getState() == LeaderState.IamTheLeader) {
			log("The leader is " + lm.getOriginator());
			if (lm.getOriginator() != this.getNodeId())
				sendMessage(lm);
		} else if (lm.getState() == LeaderState.IwantToBeLeader) {
			if (lm.getOriginator() == this.getNodeId()) {
				// I am the leader
				LeaderMessage lm2 = new LeaderMessage(++lastMessageID);
				lm2.setState(LeaderState.IamTheLeader);
				lm2.setOriginator(this.getNodeId());
				lm2.setDestination(this.getNodeId());
				lm2.setDirection(Direction.Backward);
				lm2.setDeliverAs(Delivery.Direct);
				sendMessage(lm2);
			} else if (lm.getOriginator() > this.getNodeId()) {
				// forward
				sendMessage(lm);
			} else
				; // ignore
		}
	}

	/**
	 * only the proposer can send new proposals
	 * 
	 * @param req
	 */
	private synchronized void newProposal(TextRequest req) {
		if (role != Role.Proposer)
			return;

		// create a new proposal to send to the acceptors
		Proposal newP = new Proposal();
		newP.setProposalState(ProposalState.Prepare);
		newP.setLeaderID(this.getNodeId());
		newP.setProposalID(++lastProposalID);
		newP.setCreated(new Date());
		newP.setRequest(req);

		// so the proposer can track request activity
		ProposalTracker tracker = new ProposalTracker();
		tracker.setProposal(newP);

		// how long a request for proposal state-change can exist without
		// activity
		tracker.setMaxTTL(5);

		// TODO this needs to be set at runtime to the number of acceptor nodes
		// that make a quorum. A better choice is to have the nodes broadcast
		// themselves to allow the leader to map the network thus set this value
		// at will
		tracker.setQuorumNeeded(2);

		// TODO only one proposal at a time!
		trackProposal = tracker;

		StringBuilder sb = new StringBuilder();
		sb.append("NEW PROPOSAL(")
				.append(trackProposal.getProposal().getLeaderID()).append(",")
				.append(trackProposal.getProposal().getProposalID())
				.append(")");
		log(sb.toString());

		// notify nodes - set destination as self to ensure full traversal
		ProposalMessage msg = new ProposalMessage(++lastMessageID, newP);
		msg.setDeliverAs(Delivery.Broadcast);
		msg.setDestination(this.getNodeId());
		msg.setOriginator(this.getNodeId());
		msg.setDirection(Direction.Forward);
		sendMessage(msg);
	}

	/**
	 * a proposer can reject a proposal if the request takes too long
	 */
	private synchronized void rejectProposal() {
		if (trackProposal == null
				|| trackProposal.getProposal().getProposalState() == ProposalState.Accepted
				|| trackProposal.getProposal().getProposalState() == ProposalState.Rejected)
			return;

		StringBuilder sb = new StringBuilder();
		sb.append("REJECT PROPOSAL(")
				.append(trackProposal.getProposal().getLeaderID()).append(",")
				.append(trackProposal.getProposal().getProposalID())
				.append(")");
		log(sb.toString());

		// notify nodes - set destination as self to ensure full traversal
		trackProposal.getProposal().setProposalState(ProposalState.Rejected);
		ProposalMessage msg = new ProposalMessage(++lastMessageID,
				trackProposal.getProposal());
		msg.setDeliverAs(Delivery.Broadcast);
		msg.setDestination(this.getNodeId());
		msg.setOriginator(this.getNodeId());
		msg.setDirection(Direction.Backward);
		sendMessage(msg);
	}

	/**
	 * Since any acceptor can become the leader, the code to manage proposals
	 * must be contained in all acceptor nodes
	 * 
	 * Note this is setup as a single class/method to simplify testing/learning.
	 * For a real implementation this would be delegated or have separate
	 * classes that represent the different roles.
	 */
	private void manageProposals(ProposalMessage pm) {
		// log(pm.toString());

		if (role == Role.Learner)
			actAsLearner(pm);
		else if (role == Role.Acceptor)
			actAsAcceptor(pm);
		else if (role == Role.Proposer)
			actAsProposer(pm);
	}

	/**
	 * behavior of a node that performs the work and reports back to the
	 * client/requestor
	 * 
	 * @param pm
	 */
	private void actAsLearner(ProposalMessage pm) {
		if (pm.getProposal().getProposalState() == ProposalState.Accepted) {
			// Just accept the decision of the acceptors and do the work!
			TextRequest tr = (TextRequest) pm.getProposal().getRequest();
			log("accepts proposal - message is: " + tr.getData());
			// TODO send client a message that the proposal is complete
		} else {
			// RING: just forward it along
			sendMessage(pm);
		}
	}

	/**
	 * behavior of a node that regulates/manages worker nodes.
	 * 
	 * @param pm
	 */
	private void actAsAcceptor(ProposalMessage pm) {
		int todo = compareProposal(pm.getProposal());

		if (todo == -1) {
			// reject proposal as I have a newer proposal
			log("rejecting proposal - " + pm);

			// TODO send rejection message (note silence is the same as
			// rejecting)
			return;
		} else if (todo == 1)
			currentProposal = pm.getProposal();

		// TODO for demonstration only
		try {
			Thread.sleep(rand.nextInt(500) + 500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// -----------------------------------------------------------
		// NOTE: For this example we accept everything (if reliable)
		// -----------------------------------------------------------

		// update my copy of the proposal
		// TODO assume we are not going to reject the proposal
		currentProposal = pm.getProposal();

		// state machine to determine what I need to do
		if (pm.getProposal().getProposalState() == ProposalState.Accepted
				|| pm.getProposal().getProposalState() == ProposalState.Rejected) {
			// proposal completed - I don't need to do anything else
			log("Proposal " + pm.getProposal().getProposalState());
			currentProposal = null;
		} else if (pm.getDeliverAs() == Delivery.Broadcast) {
			// proposals are broadcast to everyone so i want to forward the
			// original message plus my direct reply to the proposer

			if (acceptProposal(pm)) {
				log(pm.toString());
				ProposalMessage directReply = new ProposalMessage(
						++lastMessageID, pm.getProposal());
				directReply.setOriginator(this.getNodeId());
				directReply.setDestination(pm.getOriginator()); // to proposer
				directReply.setDeliverAs(Delivery.Direct);

				// TODO assumes ordered ring
				if (this.getNodeId() < pm.getOriginator())
					directReply.setDirection(Direction.Forward);
				else
					directReply.setDirection(Direction.Backward);

				if (pm.getProposal().getProposalState() == ProposalState.Prepare)
					directReply.setAccept(true); // same as Promise
				else if (pm.getProposal().getProposalState() == ProposalState.Accept)
					directReply.setAccept(true); // agreement

				// note final states handled above

				sendMessage(directReply);
			} else {
				// this node does not accept the proposal and by not replying,
				// it casts a vote to not proceed. It could also send a direct
				// NACK (PromiseDenied, Reject)
			}
		}

		// now forward the original message
		sendMessage(pm);
	}

	/**
	 * isolate the logic that an acceptor uses to determine if a proposal should
	 * move forward
	 * 
	 * @param pm
	 * @return
	 */
	private boolean acceptProposal(ProposalMessage pm) {
		// TODO for demonstration we can make the acceptor always reject the
		// proposal on the accept phase
		if (!reliable
				&& pm.getProposal().getProposalState() == ProposalState.Accept)
			return false;
		else
			return true;
	}

	/**
	 * behavior of a proposer node - leader that sends proposals
	 * 
	 * @param pm
	 */
	private void actAsProposer(ProposalMessage pm) {
		if (trackProposal == null)
			return;
		else if (this.getNodeId() != pm.getProposal().getLeaderID()) {
			// old proposal -> ignore
			log("OLD PROPOSAL: " + pm);
			return;
		} else if (pm.getProposal().getProposalID() == trackProposal
				.getProposal().getProposalID()) {

			// determine if we have a quorum to proceed
			if (pm.isAccept())
				trackProposal.incrementInfavor();
			else
				trackProposal.incrementAgainst();

			// TODO TTL is not currently used to limit voting period
			if (trackProposal.isQuorum()) {
				// we can move forward with the next step!
				ProposalMessage pm2 = new ProposalMessage(++lastMessageID,
						trackProposal.getProposal());
				pm2.setOriginator(this.getNodeId());
				pm2.setDestination(this.getNodeId());
				pm2.setDeliverAs(Delivery.Broadcast);

				// TODO assumes ordered ring
				if (this.getNodeId() < pm.getOriginator())
					pm2.setDirection(Direction.Forward);
				else
					pm2.setDirection(Direction.Backward);

				if (trackProposal.getProposal().getProposalState() == ProposalState.Prepare) {
					trackProposal.getProposal().setProposalState(
							ProposalState.Accept);
				} else if (trackProposal.getProposal().getProposalState() == ProposalState.Accept) {
					trackProposal.getProposal().setProposalState(
							ProposalState.Accepted);
				} else if (trackProposal.getProposal().getProposalState() == ProposalState.Reject) {
					trackProposal.getProposal().setProposalState(
							ProposalState.Rejected);
				}

				StringBuilder sb = new StringBuilder();
				sb.append("WE HAVE A QUORUM (").append(trackProposal)
						.append(")! Proposal changed to ")
						.append(trackProposal.getProposal().getProposalState());
				log(sb.toString());

				// reset quorum counters at each state change
				trackProposal.reset();

				sendMessage(pm2);
			}

			// is proposal complete
			if (trackProposal.getProposal().getProposalState() == ProposalState.Accepted
					|| trackProposal.getProposal().getProposalState() == ProposalState.Rejected) {
				StringBuilder sb = new StringBuilder();
				sb.append("PROPOSAL(").append(this.getNodeId()).append(",")
						.append(trackProposal.getProposal().getProposalID())
						.append(") IS COMPLETE - ")
						.append(trackProposal.getProposal().getProposalState());
				log(sb.toString());
				trackProposal = null;
			}
		} else {
			// unknown
			log("unknown proposal response: " + pm);
		}
	}

	/**
	 * the heartbeat of the leader will notify other nodes that the leader is
	 * still alive and to check for proposals that have timed out
	 */
	public void heartbeatHandler() {
		if (role == Role.Proposer) {

			// heartbeat message
			LeaderMessage lm = new LeaderMessage(++lastMessageID);
			lm.setState(LeaderState.Heartbeat);
			lm.setOriginator(this.getNodeId());
			lm.setDestination(this.getNodeId());
			lm.setDirection(Direction.Backward);
			lm.setDeliverAs(Delivery.Broadcast);
			sendMessage(lm);

			if (trackProposal != null) {
				trackProposal.decrementTTL();
				if (trackProposal.isExpired())
					rejectProposal();
			}
		}
	}

	/**
	 * 
	 * @param to
	 * @return 1 if new proposal should replace the current, 0 if they are the
	 *         same, -1 to reject the new proposal
	 */
	private int compareProposal(Proposal to) {
		if (currentProposal == null)
			return 1;
		else if (currentProposal.getLeaderID() != to.getLeaderID())
			return 1; // new leader, always accept
		else if (currentProposal.getProposalID() < to.getProposalID())
			return 1; // override existing proposal
		else if (currentProposal.getProposalID() > to.getProposalID())
			return -1; // reject, I have a newer one
		else
			return 0; // they are the same
	}

	/**
	 * isolate logging. This really should be slf4j
	 * 
	 * @param msg
	 */
	private void log(String msg) {
		System.out.println("--> " + this.role + this.getNodeId() + ": " + msg);
		System.out.flush();
	}
}
