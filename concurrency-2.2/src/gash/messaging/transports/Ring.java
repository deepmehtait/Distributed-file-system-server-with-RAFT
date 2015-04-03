package gash.messaging.transports;

import gash.messaging.Message;
import gash.messaging.Node;
import gash.messaging.RingNode;

import java.util.ArrayList;

/**
 * a ring is a closed line (daisy chained) group of services/nodes.
 * 
 * @author gash1
 * 
 */
public class Ring implements MessageTransport<Message> {

	private int msgID;
	private ArrayList<RingNode> nodes;

	public Ring(int numNodes) {
		nodes = new ArrayList<RingNode>(numNodes);

		if (numNodes <= 0)
			return;

		for (int n = 0; n < numNodes; n++) {
			RingNode node = new RingNode(n);
			nodes.add(node);
			node.start();
		}

		// setup network (linked list)
		RingNode[] list = getNodes();
		for (int n = 0; n < numNodes; n++) {
			if (n + 1 < numNodes)
				list[n].setNext(list[n + 1]);

			if (n - 1 >= 0)
				list[n].setPrevious(list[n - 1]);
		}

		// close the ring
		list[0].setPrevious(list[numNodes - 1]);
		list[numNodes - 1].setNext(list[0]);

		// show the ring
		// for (int n = 0; n < numNodes; n++)
		// System.out.println(n + ") P: " + nodes[n].previous + ", N: " +
		// nodes[n].next);
	}

	@Override
	public RingNode[] getNodes() {
		if (nodes == null)
			return null;

		RingNode[] r = new RingNode[nodes.size()];
		return nodes.toArray(r);
	}

	@Override
	public void addNode(Node<Message> node) {
		// TODO this class needs to be templated
		nodes.add((RingNode) node);
	}

	private RingNode getNode(int id) {
		for (Node<Message> n : nodes) {
			if (id == n.getNodeId())
				return (RingNode) n;
		}

		throw new RuntimeException("Node not found, id = " + id);
	}

	@Override
	public void sendMessage(Message msg) {
		if (msg == null)
			return;

		if (msg.getOriginator() < msg.getDestination())
			msg.setDirection(Message.Direction.Forward);
		else
			msg.setDirection(Message.Direction.Backward);

		getNode(msg.getOriginator()).message(msg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gash.messaging.MessageTransport#sendMessage(int, int,
	 * java.lang.String)
	 */
	@Override
	public void sendMessage(int fromNodeId, int toNodeId, String text) {
		msgID++;

		Message msg = new Message(msgID);
		msg.setMessage(text);
		msg.setDestination(toNodeId);
		msg.setOriginator(fromNodeId);

		// if we could calculate distances (hops), we could determine the
		// optimum traversal direction. For now, set direction based on > or <
		if (fromNodeId < toNodeId)
			msg.setDirection(Message.Direction.Forward);
		else
			msg.setDirection(Message.Direction.Backward);

		getNode(fromNodeId).message(msg);
	}

	@Override
	public void broadcastMessage(int fromNodeId, String text) {
		Message msg = new Message(msgID);
		msg.setMessage(text);
		msg.setDestination(fromNodeId);
		msg.setOriginator(fromNodeId);
		msg.setDirection(Message.Direction.Forward);
		getNode(fromNodeId).message(msg);
	}

	/**
	 * emulates the message sent through network
	 * 
	 * @author gash1
	 * 
	 */

}
