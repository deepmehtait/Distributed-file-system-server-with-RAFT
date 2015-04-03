package gash.consensus.paxos;

import gash.consensus.paxos.core.Request;

import java.util.Date;

/**
 * example request. For demonstration the data is a text message.
 * 
 * @author gash
 * 
 */
public class TextRequest implements Request {
	private int requestID;
	private String requestor;
	private Date requestCreationDate;
	private RequestState state;

	private String data;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public int getRequestID() {
		return requestID;
	}

	@Override
	public String getRequestor() {
		return requestor;
	}

	@Override
	public Date getRequestCreationDate() {
		return requestCreationDate;
	}

	@Override
	public RequestState getState() {
		return state;
	}

	public void setRequestID(int requestID) {
		this.requestID = requestID;
	}

	public void setRequestor(String requestor) {
		this.requestor = requestor;
	}

	public void setRequestCreationDate(Date requestCreationDate) {
		this.requestCreationDate = requestCreationDate;
	}

	public void setState(RequestState state) {
		this.state = state;
	}

}
