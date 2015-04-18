package poke.server.election;

import java.util.LinkedHashMap;

public class LogMessage {
	
	int term;
	int logIndex=0 ;
	int prevLogTerm =0;
	int prevLogIndex =0 ;
	LinkedHashMap<Integer, String> entries;
	
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
	public int getPrevLogTerm() {
		return prevLogTerm;
	}
	public void setPrevLogTerm(int prevLogTerm) {
		this.prevLogTerm = prevLogTerm;
	}
	public int getPrevLogIndex() {
		return prevLogIndex;
	}
	public void setPrevLogIndex(int prevLogIndex) {
		this.prevLogIndex = prevLogIndex;
	}
	public LinkedHashMap<Integer, String> getEntries() {
		return entries;
	}
	public void setEntries(LinkedHashMap<Integer, String> entries) {
		this.entries = entries;
	}

}
