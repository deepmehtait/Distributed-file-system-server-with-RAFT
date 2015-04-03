package gash.consensus.paxos.core;

import gash.messaging.Message;

public class ProposalMessage extends Message {
	private Proposal proposal;

	private boolean accept; // response

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Proposal(").append(this.getOriginator()).append(",")
				.append(proposal.getProposalID()).append(") by ")
				.append(proposal.getRequest().getRequestor())
				.append(", state: ").append(proposal.getProposalState());
		return sb.toString();
	}

	public boolean isAccept() {
		return accept;
	}

	public void setAccept(boolean accept) {
		this.accept = accept;
	}

	public ProposalMessage(int id, Proposal proposal) {
		super(id);
		this.proposal = proposal;
	}

	public Proposal getProposal() {
		return proposal;
	}
}
