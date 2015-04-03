package poke.server.voting;

import java.util.ArrayList;

public class CumulativeState extends VoteState {
	private int requiredVotes = 0;
	private long maxDuration = -1;

	private ArrayList<Integer> voters;
	private ArrayList<Integer> candidates;
	private ArrayList<Integer> candidateVotes;

	public CumulativeState(int requiredVotes, String desc) {
		this.requiredVotes = requiredVotes;
		this.desc = desc;
		this.startedOn = System.currentTimeMillis();
		this.state = -1;
		this.version = 1;
		this.maxDuration = -1;
	}

	public int getRequiredVotes() {
		return requiredVotes;
	}

	public void setRequiredVotes(int requiredVotes) {
		this.requiredVotes = requiredVotes;
	}

	public long getMaxDuration() {
		return maxDuration;
	}

	public void setMaxDuration(long maxDuration) {
		this.maxDuration = maxDuration;
	}

	@Override
	public void castVote(Vote vote) {
		// TODO Auto-generated method stub
		
	}
}
