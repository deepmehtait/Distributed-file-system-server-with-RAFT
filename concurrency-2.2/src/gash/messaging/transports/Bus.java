package gash.messaging.transports;

import gash.messaging.Message;
import gash.messaging.Node;
import gash.messaging.StatNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * a demonstration using a communication bus that nodes attach to and listen for
 * messages. Messages sent through the bus are broadcasted to all nodes on the
 * bus, If a point-to-point delivery system was to be implemented as well as the
 * broadcast we would have affectively created a star network.
 * 
 * Like many bus designs, this implementation only allows one message to be sent
 * at a time. It however does not require the endpoint to process the message
 * before continuing sending messages (each node has a simple queue/inbox for
 * incoming messages).
 * 
 * @author gash1
 * 
 */
public class Bus<M extends Message> implements MessageTransport<Message> {
	private int msgID;
	private Transport<Message> bus;
	private StatNode stat;

	public Bus(int numNodes) {

		bus = new Transport<Message>();
		bus.start();

		stat = new StatNode(Integer.MAX_VALUE - 1);
		bus.addNode(stat);
		stat.start();

		if (numNodes > 0) {
			for (int n = 0; n < numNodes; n++) {
				BusNode node = new BusNode(n);
				node.start();

				// register node to the bus
				bus.addNode(node);
			}
		}
	}

	@Override
	public void addNode(Node<Message> node) {
		if (node == null)
			return;

		// node.start();
		bus.addNode(node);
	}

	public void showReport() {
		stat.report();
	}

	@Override
	public void sendMessage(Message msg) {
		if (msg != null)
			bus.sendMessage(msg);
	}

	@Override
	public Node<Message>[] getNodes() {
		return bus.getNodes();
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
		bus.sendMessage(msg);
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
		bus.sendMessage(msg);
	}

	public void privateMessage(int fromNode, int toNode, String text) {
		msgID++;
		Message msg = new Message(msgID);
		msg.setId(msgID);
		msg.setDeliverAs(Message.Delivery.Direct);
		msg.setDestination(toNode);
		msg.setOriginator(fromNode);
		msg.setMessage(text);
		bus.sendMessage(msg);
	}

	/**
	 * represent the bus communication
	 * 
	 * @author gash1
	 * 
	 */
	public class Transport<M extends Message> extends Thread {
		private List<Node<M>> bus;
		private List<M> inbox;

		public Transport() {
			inbox = Collections.synchronizedList(new ArrayList<M>());
			bus = Collections.synchronizedList(new ArrayList<Node<M>>());
		}

		public void addNode(Node<M> n) {
			if (n != null && !bus.contains(n))
				bus.add(n);
		}

		public Node<M>[] getNodes() {
			@SuppressWarnings("unchecked")
			Node<M>[] r = new Node[bus.size()];
			return (Node<M>[]) bus.toArray(r);
		}

		public void removeNode(Node<M> n) {
			bus.remove(n);
		}

		public void sendMessage(M msg) {
			if (msg != null)
				inbox.add(msg);
		}

		public void run() {
			try {
				while (true) {
					if (inbox.size() == 0)
						sleep(100); // not ideal
					else {
						M msg = inbox.remove(0);
						if (msg.getDeliverAs() == Message.Delivery.Broadcast) {
							for (Node<M> n : bus)
								if (n.isAlive())
									n.message(msg);
						} else {
							for (Node<M> n : bus) {
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
	 * represents a node in the network
	 * 
	 * @author gash1
	 * 
	 */
	public class BusNode extends Node<Message> {

		public BusNode(int id) {
			super(id);
		}

		@Override
		public void process(Message msg) {
			if ((msg.getDeliverAs() == Message.Delivery.Broadcast && msg.getDestination() == getNodeId())
					|| (msg.getDeliverAs() == Message.Delivery.Broadcast && msg.getDestination() == Message.sNobody))
				System.out.println("Node " + getNodeId() + " (from = " + msg.getOriginator() + ") " + msg.getMessage());
			else
				System.out.println("Node " + getNodeId() + " ignoring msg from " + msg.getOriginator());
		}
	}
}
