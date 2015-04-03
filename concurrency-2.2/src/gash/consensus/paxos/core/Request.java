package gash.consensus.paxos.core;

import java.util.Date;

public interface Request {
	public enum RequestState {
		New, Accepted, Rejected
	}

	int getRequestID();

	String getRequestor();

	Date getRequestCreationDate();

	RequestState getState();
}
