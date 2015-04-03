package gash.consensus.paxos.core;

import gash.consensus.paxos.PaxosNode;

public class Heartbeat extends Thread {
	private PaxosNode node;
	private boolean forever = true;

	public Heartbeat(PaxosNode node) {
		this.node = node;
	}

	public void shutdown() {
		forever = false;
	}

	public void run() {

		while (forever) {
			try {
				Thread.sleep(2000);
				node.heartbeatHandler();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("Heartbeat shutting down");

	}
}
