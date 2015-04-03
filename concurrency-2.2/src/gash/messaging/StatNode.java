package gash.messaging;

import java.util.HashMap;

public class StatNode extends Node<Message> {
	private HashMap<Integer, Stat> stats = new HashMap<Integer, Stat>();
	private int broadcast;

	public StatNode(int id) {
		super(id);
	}

	public void report() {
		System.out.println("\n------------------------------------------------");
		System.out.println("Num broadcast: " + broadcast);
		if (stats.size() != 0) {
			System.out.println("Individual Stats:");
			for (Integer k : stats.keySet()) {
				Stat s = stats.get(k);
				System.out.println("    Node " + s.nodeId + ") sent: " + s.sent + ", received: " + s.received);
			}
		}
		System.out.println("------------------------------------------------\n");
	}

	@Override
	public void process(Message msg) {
		Stat s = stats.get(msg.getOriginator());
		if (s == null) {
			s = new Stat();
			s.nodeId = msg.getOriginator();
			stats.put(s.nodeId, s);
		}
		s.sent++;

		if (msg.getDeliverAs() == Message.Delivery.Broadcast && msg.getDestination() != Message.sNobody) {
			s = stats.get(msg.getDestination());
			if (s == null) {
				s = new Stat();
				s.nodeId = msg.getDestination();
				stats.put(s.nodeId, s);
			}
			s.received++;
		} else {
			broadcast++;
		}
	}

	private class Stat {
		int nodeId;
		int sent;
		int received;
	}
}
