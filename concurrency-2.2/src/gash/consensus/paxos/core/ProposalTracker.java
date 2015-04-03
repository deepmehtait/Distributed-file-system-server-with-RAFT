package gash.consensus.paxos.core;

public class ProposalTracker {
	private Proposal proposal;
	private int infavor;
	private int against;
	private int quorumNeeded;
	private int ttl;
	private int maxTTL;

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(" infavor: ").append(infavor).append(", against: ")
				.append(against).append(", ttl: ").append(ttl);
		return sb.toString();
	}

	public void reset() {
		infavor = 0;
		against = 0;
		ttl = 0;
	}

	public int getQuorumNeeded() {
		return quorumNeeded;
	}

	public void setQuorumNeeded(int quorumNeeded) {
		this.quorumNeeded = quorumNeeded;
	}

	public boolean isExpired() {
		return ttl > maxTTL;
	}

	public void decrementTTL() {
		ttl++;
	}

	public void incrementInfavor() {
		infavor++;
	}

	public void incrementAgainst() {
		against++;
	}

	public boolean isQuorum() {
		return infavor >= quorumNeeded;
	}

	public Proposal getProposal() {
		return proposal;
	}

	public void setProposal(Proposal proposal) {
		this.proposal = proposal;
	}

	public int getInfavor() {
		return infavor;
	}

	public int getAgainst() {
		return against;
	}

	public int getTtl() {
		return ttl;
	}

	public void setMaxTTL(int maxTTL) {
		this.maxTTL = maxTTL;
	}
}
