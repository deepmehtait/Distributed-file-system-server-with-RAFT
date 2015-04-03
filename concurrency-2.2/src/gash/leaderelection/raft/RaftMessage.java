package gash.leaderelection.raft;

import gash.leaderelection.raft.Raft.LogEntryBase;
import gash.leaderelection.raft.Raft.RaftNode;
import gash.messaging.Message;

import java.util.List;

/**
 * The message represents the four messages used in Raft:
 * <ol>
 * <li>Leader
 * <li>RequestVote
 * <li>Vote
 * <li>Append
 * <li>
 * </ol>
 * 
 * @author gash
 *
 */
public class RaftMessage extends Message {
	public enum Action {
		Append, RequestVote, Leader, Vote
	}

	private Action action;
	private int term;
	private int logIndex;
	private List<LogEntryBase> entries;
	private int prevTerm;
	private int prevLogIndex;

	// message id
	public RaftMessage(int id) {
		super(id);
	}

	public int getTerm() {
		return term;
	}

	public void setTerm(int term) {
		this.term = term;
	}

	public int getLogIndex() {
		return logIndex;
	}

	public void setLogIndex(int logIndex) {
		this.logIndex = logIndex;
	}

	public List<LogEntryBase> getEntries() {
		return entries;
	}

	public void setEntries(List<LogEntryBase> entries) {
		this.entries = entries;
	}

	public int getPrevTerm() {
		return prevTerm;
	}

	public void setPrevTerm(int prevTerm) {
		this.prevTerm = prevTerm;
	}

	public int getPrevLogIndex() {
		return prevLogIndex;
	}

	public void setPrevLogIndex(int prevLogIndex) {
		this.prevLogIndex = prevLogIndex;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}
}
