package poke.server.voting;

public class Vote {

	private Long electionID;
	private int voter;
	private int candidate;
	private int value;

	public Long getElectionID() {
		return electionID;
	}

	public void setElectionID(Long electionID) {
		this.electionID = electionID;
	}

	public int getVoter() {
		return voter;
	}

	public void setVoter(int voter) {
		this.voter = voter;
	}

	public int getCandidate() {
		return candidate;
	}

	public void setCandidate(int candidate) {
		this.candidate = candidate;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

}
