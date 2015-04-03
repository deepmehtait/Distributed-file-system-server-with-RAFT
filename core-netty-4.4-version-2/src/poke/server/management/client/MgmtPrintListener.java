package poke.server.management.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.core.Mgmt.Management;

public class MgmtPrintListener implements MgmtListener {
	protected static Logger logger = LoggerFactory.getLogger("connect");
	private String id;

	public MgmtPrintListener(String id) {
		this.id = id;
	}

	@Override
	public String getListenerID() {
		return id;
	}

	@Override
	public void onMessage(Management msg) {
		if (logger.isDebugEnabled())
			System.out.println("got a reply"); // TODO more info needed
	}
}
