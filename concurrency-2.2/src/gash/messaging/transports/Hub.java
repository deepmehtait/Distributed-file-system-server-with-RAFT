package gash.messaging.transports;

import gash.messaging.Message;
import gash.messaging.Node;
import gash.messaging.StatNode;
import gash.messaging.Message.Delivery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hub implements MessageTransport<Message> {

	private int msgID;
	private Transport<Message> hub;
	private StatNode stat;

	public Hub() {

		hub = new Transport<Message>();
		hub.start();

		stat = new StatNode(Integer.MAX_VALUE - 1);
		hub.addNode(stat);
		stat.start();
	}

	@Override
	public void addNode(Node<Message> node) {
		if (node == null)
			return;

		node.start();
		hub.addNode(node);
	}

	public void showReport() {
		stat.report();
	}

	@Override
	public void sendMessage(Message msg) {
		if (msg != null)
			hub.sendMessage(msg);
	}

	@Override
	public Node<Message>[] getNodes() {
		return hub.getNodes();
	}

	@Override
	public void sendMessage(int fromNodeId, int toNodeId, String text) {
		privateMessage(fromNodeId, toNodeId, text);
	}

	@Override
	public void broadcastMessage(int fromNode, String text) {
		msgID++;
		Message msg = new Message(msgID);
		msg.setDeliverAs(Message.Delivery.Broadcast);
		msg.setDestination(Message.sNobody);
		msg.setOriginator(fromNode);
		msg.setMessage(text);
		hub.sendMessage(msg);
	}

	/**
	 * private message publicly broadcast - a variant of a private message
	 * (a.k.a. to: someone and cc: everyone)
	 * 
	 * @param fromNode
	 * @param toNode
	 * @param text
	 */
	public void privateMessageOverBroadcast(int fromNode, int toNode, String text) {
		msgID++;
		Message msg = new Message(msgID);
		msg.setDeliverAs(Message.Delivery.Broadcast);
		msg.setDestination(toNode);
		msg.setOriginator(fromNode);
		msg.setMessage(text);
		hub.sendMessage(msg);
	}

	public void privateMessage(int fromNode, int toNode, String text) {
		msgID++;
		Message msg = new Message(msgID);
		msg.setId(msgID);
		msg.setDeliverAs(Message.Delivery.Direct);
		msg.setDestination(toNode);
		msg.setOriginator(fromNode);
		msg.setMessage(text);
		hub.sendMessage(msg);
	}

	/**
	 * represent the central distribution service (hub) of a communication
	 * network
	 * 
	 * @author gash1
	 * 
	 */
	public class Transport<M extends Message> extends Thread {
		private List<Node<Message>> spokes;
		private List<Message> inbox;

		public Transport() {
			inbox = Collections.synchronizedList(new ArrayList<Message>());
			spokes = Collections.synchronizedList(new ArrayList<Node<Message>>());
		}

		public void addNode(Node<Message> n) {
			if (n != null && !spokes.contains(n))
				spokes.add(n);
		}

		public Node<M>[] getNodes() {
			@SuppressWarnings("unchecked")
			Node<M>[] r = new Node[spokes.size()];
			return (Node<M>[]) spokes.toArray(r);
		}

		public void removeNode(Node<M> n) {
			spokes.remove(n);
		}

		public void sendMessage(Message msg) {
			if (msg != null)
				inbox.add(msg);
		}

		public void run() {
			try {
				while (true) {
					if (inbox.size() == 0)
						sleep(100); // not ideal
					else {
						Message msg = inbox.remove(0);
						if (msg.getDeliverAs() == Message.Delivery.Broadcast) {
							for (Node<Message> n : spokes)
								if (n.isAlive())
									n.message(msg);
						} else {
							for (Node<Message> n : spokes) {
								if (n.getNodeId() == msg.getDestination())
									if (n.isAlive())
										n.message(msg);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * represents a node in the network - for a spoke and hub model, the spoke
	 * is a process connecting to a centralized distribution point
	 * 
	 * @author gash1
	 * 
	 */
	public static class SpokeNode extends Node<Message> {

		private Hub network;
		private int messageCounter;

		public SpokeNode(int id, Hub network) {
			super(id);
			this.network = network;
		}

		@Override
		public void process(Message msg) {
			System.out.println("Node " + getNodeId() + " (from = " + msg.getOriginator() + ") " + msg.getMessage());

			// TODO to stop proliferation of messages (replies) - demo only
			if (msg.getMessage().startsWith("Thanks"))
				return;

			Message reply = new Message(++messageCounter);
			reply.setOriginator(this.getNodeId());
			reply.setDestination(msg.getOriginator());
			reply.setDeliverAs(Delivery.Direct);
			reply.setMessage("Thanks for the message");
			network.sendMessage(reply);
		}
	}
}
