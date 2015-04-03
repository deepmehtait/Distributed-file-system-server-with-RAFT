package gash.consensus.paxos.core;

import gash.messaging.Message;

public class RequestMessage extends Message {
	public RequestMessage(int id) {
		super(id);
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	protected Request request;

}
