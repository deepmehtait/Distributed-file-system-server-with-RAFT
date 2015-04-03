package gash.messaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Node<M extends Message> extends Thread {
	protected int nodeId;
	protected List<M> inbox;
	private boolean forever = true;

	public Node(int id) {
		inbox = Collections.synchronizedList(new ArrayList<M>());
		nodeId = id;
	}

	public abstract void process(M msg);

	public void message(M msg) {
		if (msg != null) {
			msg.incrementHops(nodeId);
			getInbox().add(msg);
		}
	}

	public void shutdown() {
		forever = false;
	}

	public void run() {

		while (forever) {
			try {
				if (getInbox().size() == 0) {
					sleep(100);
				} else {
					// System.out.println("<node.run() processing a message>");
					M msg = getInbox().remove(0);
					process(msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("** node " + nodeId + " shutting down **");
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int id) {
		this.nodeId = id;
	}

	public List<M> getInbox() {
		return inbox;
	}

	public void setInbox(List<M> inbox) {
		this.inbox = inbox;
	}
}