package gash.clock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VectorClock {

	/** behavior of touch() if the node is not already part of the vector clock */
	private boolean failOnUknown = false;

	private List<ClockEntry> vector = Collections.synchronizedList(new ArrayList<ClockEntry>());

	public VectorClock() {
	}

	/**
	 * creates a deep copy of the supplied clock
	 * 
	 * @param vc
	 */
	public VectorClock(VectorClock vc) {
		List<ClockEntry> vector = Collections.synchronizedList(new ArrayList<ClockEntry>());
		for (ClockEntry e : vc.vector) {
			ClockEntry e2 = new ClockEntry();
			e2.node = e.node;
			e2.timestamp = e.timestamp;
			e2.version = e.version;
			vector.add(e2);
		}
		this.vector = vector;
	}

	public VectorClock clone() {
		return new VectorClock(this);
	}

	public void clear() {
		vector = Collections.synchronizedList(new ArrayList<ClockEntry>());
	}

	public void add(String node) {
		for (ClockEntry e : vector) {
			if (e.getNode().equals(node))
				return;
		}

		ClockEntry e = new ClockEntry();
		e.setNode(node);
		e.touch();
	}

	public void touch(String node) {
		for (ClockEntry e : vector) {
			if (e.getNode().equals(node)) {
				e.touch();
				return;
			}
		}

		if (failOnUknown)
			throw new RuntimeException("Unknown node: " + node);
		else {
			//System.out.println("---> clock: adding node " + node);
			ClockEntry e = new ClockEntry();
			e.setNode(node);
			e.touch();
			vector.add(e);
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		Collections.sort(vector, new Comparator<ClockEntry>() {

			@Override
			public int compare(ClockEntry arg0, ClockEntry arg1) {
				return arg0.node.toLowerCase().compareTo(arg1.node.toLowerCase());
			}
		});

		//System.out.println("--> " + vector);
		for (ClockEntry e : vector)
			sb.append(e.toString() + " ");

		return sb.toString().trim();
	}

	public boolean isFailOnUknown() {
		return failOnUknown;
	}

	public void setFailOnUknown(boolean failOnUknown) {
		this.failOnUknown = failOnUknown;
	}
}
