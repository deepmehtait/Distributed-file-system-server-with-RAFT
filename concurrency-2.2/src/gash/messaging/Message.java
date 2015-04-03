package gash.messaging;

import gash.clock.VectorClock;

/**
 * message sent through the network
 * 
 * @author gash1
 * 
 */
public class Message {
	public static final int sNobody = -1;

	public enum Direction {
		Forward, Backward
	}

	public enum Delivery {
		Broadcast, Direct
	}

	private int nodeId;
	private int destination = -1; // node ID
	private int originator = -1; // node ID
	private int hops;
	private String message;
	private boolean reverse = false;
	private Direction direction = Direction.Forward;
	private VectorClock clock;
	private Delivery deliverAs = Delivery.Direct;

	public Message(int id) {
		this.nodeId = id;
		clock = new VectorClock();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("M").append(nodeId).append(" to: ").append(originator)
				.append(", from: ").append(destination);
		return sb.toString();
	}

	public Delivery getDeliverAs() {
		return deliverAs;
	}

	public void setDeliverAs(Delivery deliverAs) {
		this.deliverAs = deliverAs;
	}

	public int getDestination() {
		return destination;
	}

	public void setDestination(int destination) {
		this.destination = destination;
	}

	public int getHops() {
		return hops;
	}

	public synchronized void incrementHops(int fromNodeId) {
		clock.touch(String.valueOf(fromNodeId));
		this.hops++;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public int getId() {
		return nodeId;
	}

	public void setId(int id) {
		this.nodeId = id;
	}

	public int getOriginator() {
		return originator;
	}

	public void setOriginator(int originator) {
		this.originator = originator;
	}

	public boolean isReverse() {
		return reverse;
	}

	public void setReverse(boolean reverse) {
		System.out.println("--> reversing direction");

		if (direction == Direction.Forward)
			direction = Direction.Backward;
		else
			direction = Direction.Forward;

		this.reverse = reverse;
	}

	public VectorClock getClock() {
		return clock;
	}
}