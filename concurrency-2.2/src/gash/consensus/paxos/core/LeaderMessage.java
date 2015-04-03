package gash.consensus.paxos.core;

import gash.messaging.Message;

/**
 * Leader election messages
 * 
 * @author gash
 * 
 */
public class LeaderMessage extends Message {
	public enum LeaderState {
		IamTheLeader, IwantToBeLeader, Heartbeat
	}

	private LeaderState state;

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Leader (").append(this.getState()).append(") from ")
				.append(this.getDestination());
		return sb.toString();
	}

	public LeaderState getState() {
		return state;
	}

	public void setState(LeaderState state) {
		this.state = state;
	}

	public LeaderMessage(int msgID) {
		super(msgID);
	}
}
