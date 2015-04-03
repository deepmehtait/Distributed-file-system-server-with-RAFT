package gash.consensus.paxos.core;

import java.util.Date;

/**
 * the proposal issued by the proposer (leader). Normally the created (Date) is
 * not used to limit quorum or request timeouts; a heartbeat is generally used.
 * For this demonstration, the date is okay.
 * 
 * @author gash
 * 
 */
public class Proposal {
	// note PromiseDenied, Reject are NACKs to actively reject a proposal
	public enum ProposalState {
		New, Prepare, Promise, PromiseDenied, Accept, Accepted, Reject, Rejected
	}

	private int leaderID;
	private int proposalID;
	private Date created;
	private ProposalState state = ProposalState.New;
	private Request request;

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(leaderID).append(":").append(proposalID).append(" - ");

		return sb.toString();
	}

	public int getLeaderID() {
		return leaderID;
	}

	public void setLeaderID(int leaderID) {
		this.leaderID = leaderID;
	}

	public int getProposalID() {
		return proposalID;
	}

	public void setProposalID(int proposalID) {
		this.proposalID = proposalID;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public ProposalState getProposalState() {
		return state;
	}

	public void setProposalState(ProposalState state) {
		this.state = state;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

}
