package gash.messaging.transports;

import gash.messaging.Message;
import gash.messaging.Node;
import gash.messaging.RingNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * a line is a linear (daisy chained) group of services/nodes.
 * 
 * @author gash1
 * 
 */
public class Line implements MessageTransport<Message> {

	private boolean orderedNetwork;
	private int msgID;
	private ArrayList<LineNode> nodes;

	public Line(int numNodes, boolean ordered) {
		orderedNetwork = ordered;

		nodes = new ArrayList<LineNode>();
		for (int n = 0; n < numNodes; n++) {
			LineNode node = new LineNode(n);
			nodes.add(node);
			node.start();
		}

		// need to be descending order
		orderNodes();

		// setup network (linked list)
		for (int n = 0; n < numNodes; n++) {

			if (n + 1 < numNodes)
				nodes.get(n).setNext(nodes.get(n + 1));

			if (n - 1 > 0)
				nodes.get(n).setPrevious(nodes.get(n - 1));
		}
	}

	private void orderNodes() {
		Collections.sort(nodes, new Comparator<Node<Message>>() {

			@Override
			public int compare(Node<Message> n0, Node<Message> n1) {
				if (n0.getNodeId() == n1.getNodeId())
					return 0;
				else if (n0.getNodeId() > n1.getNodeId())
					return 1;
				else
					return -1;
			}
		});
	}

	@Override
	public void addNode(Node<Message> node) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public Node<Message>[] getNodes() {
		if (nodes == null)
			return null;

		@SuppressWarnings("unchecked")
		Node<Message>[] r = new Node[nodes.size()];
		return (Node<Message>[]) nodes.toArray(r);
	}

	private Node<Message> getNode(int id) {
		for (Node<Message> n : nodes) {
			if (id == n.getNodeId())
				return n;
		}

		throw new RuntimeException("Node not found, id = " + id);
	}

	@Override
	public void sendMessage(Message msg) {
		if (msg == null)
			return;

		if (orderedNetwork) {
			if (msg.getOriginator() < msg.getDestination())
				msg.setDirection(Message.Direction.Forward);
			else
				msg.setDirection(Message.Direction.Backward);

			getNode(msg.getOriginator()).message(msg);
		} else {
			// unordered so we must sent it out to everyone
			msg.setDirection(Message.Direction.Forward);
			getNode(msg.getOriginator()).message(msg);

			msg.setDirection(Message.Direction.Backward);
			getNode(msg.getOriginator()).message(msg);
		}
	}

	@Override
	public void sendMessage(int fromNodeId, int toNodeId, String text) {
		msgID++;

		if (orderedNetwork) {
			// node network are ordered by IDs
			Message msg = new Message(msgID);
			msg.setMessage(text);
			msg.setDestination(toNodeId);

			if (fromNodeId < toNodeId)
				msg.setDirection(Message.Direction.Forward);
			else
				msg.setDirection(Message.Direction.Backward);

			getNode(fromNodeId).message(msg);
		} else {
			broadcastMessage(fromNodeId, text);
		}
	}

	@Override
	public void broadcastMessage(int fromNodeId, String text) {
		// unordered IDs so, we must broadcast the message
		Message msg = new Message(msgID);
		msg.setMessage(text);
		msg.setDestination(Message.sNobody);
		msg.setDirection(Message.Direction.Forward);
		getNode(fromNodeId).message(msg);

		msg = new Message(msgID);
		msg.setMessage(text);
		msg.setDestination(Message.sNobody);
		msg.setDirection(Message.Direction.Backward);
		getNode(fromNodeId).message(msg);
	}

	/**
	 * represents a node in the network
	 * 
	 * @author gash1
	 * 
	 */
	public class LineNode extends RingNode {

		public LineNode(int id) {
			super(id);
		}

		public void process(Message msg) {
			if (msg.getDestination() == getId()) {
				System.out.println("Node " + getId() + " (hops = " + msg.getHops() + ") " + msg.getMessage());
			} else {
				if (msg.getDirection() == Message.Direction.Forward) {
					if (getNext() == null)
						System.out
								.println("--> msg (" + msg.getId() + ") reached end of line, hops = " + msg.getHops());
					else
						getNext().message(msg);
				} else {
					// backward
					if (getPrevious() == null)
						System.out.println("--> msg (" + msg.getId() + ") reached beginning of line, hops = "
								+ msg.getHops());
					else
						getPrevious().message(msg);
				}
			}
		}
	}
}
