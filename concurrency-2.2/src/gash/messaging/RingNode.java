package gash.messaging;

/**
 * represents a node in the network
 * 
 * @author gash1
 * 
 */
public class RingNode extends Node<Message> {

	protected RingNode previous;
	protected RingNode next;

	public RingNode getPrevious() {
		return previous;
	}

	public void setPrevious(RingNode previous) {
		this.previous = previous;
	}

	public RingNode getNext() {
		return next;
	}

	public void setNext(RingNode next) {
		this.next = next;
	}

	public RingNode(int id) {
		super(id);
	}

	public String toString() {
		return String.valueOf(nodeId);
	}

	public void process(Message msg) {
		if (msg.getDestination() == nodeId) {
			System.out.println("Node " + nodeId + " got a message from node "
					+ msg.getOriginator() + " (hops = " + msg.getHops()
					+ ", clock = " + msg.getClock() + "), msg: "
					+ msg.getMessage());
			return;
		}

		// stopping traversal for an unknown node ID
		if (nodeId == msg.getOriginator() && msg.getHops() > 0
				&& !msg.isReverse()) {
			System.out.println("--> failed (msg id = " + msg.getId()
					+ ", clock = " + msg.getClock() + ") unknown node "
					+ msg.getDestination() + ", hops = " + msg.getHops());
			return;
		} else if (msg.isReverse() && previous == null) {
			System.out.println("--> failed (msg id = " + msg.getId()
					+ ", clock = " + msg.getClock() + ") unreachable node "
					+ msg.getDestination() + ", hops = " + msg.getHops());
			return;
		}

		sendMessage(msg);
	}

	public void sendMessage(Message msg) {
		//System.out.println("sendMessage(): " + msg.toString());
		
		msg.incrementHops(nodeId);
		
		if (msg.getDirection() == Message.Direction.Forward) {
			if (next != null)
				next.message(msg);
			else if (previous != null) {
				// broken ring (not closed)
				msg.setReverse(true);
				previous.message(msg);
			}
		} else {
			// Direction.backward
			if (previous != null)
				previous.message(msg);
			else if (next != null) {
				// broken ring (not closed)
				msg.setReverse(true);
				next.message(msg);
			}
		}
	}
}
