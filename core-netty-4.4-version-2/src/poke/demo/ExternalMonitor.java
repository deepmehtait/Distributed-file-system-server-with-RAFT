/*
 * copyright 2014, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package poke.demo;

import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.core.Mgmt.Management;
import poke.server.monitor.HeartMonitor;
import poke.server.monitor.MonitorListener;

/**
 * DEMO: how to listen to a heartbeat.
 * 
 * @author gash
 * 
 */
public class ExternalMonitor {
	protected static Logger logger = LoggerFactory.getLogger("client");

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String host = "localhost";
		int mport = 5670;

		if (args.length == 2) {
			try {
				host = args[0];
				mport = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				logger.warn("Unable to set port numbes, using default: 5670/5680");
			}
		}

		logger.info("trying to connect monitor to " + host + ":" + mport);

		int destNode = 0;
		HeartMonitor hm = new HeartMonitor(2000, host, mport, destNode);
		hm.addListener(new HeartPrintListener(null));
		hm.waitForever();
	}

	public static class HeartPrintListener implements MonitorListener {
		protected static Logger logger = LoggerFactory.getLogger("client");

		// for filtering
		private Integer nodeID;

		/**
		 * create a listener of messages - demonstration
		 * 
		 * @param nodeID
		 *            IF not null, filter message by this node ID
		 */
		public HeartPrintListener(Integer nodeID) {
			this.nodeID = nodeID;
		}

		@Override
		public Integer getListenerID() {
			return nodeID;
		}

		@Override
		public void onMessage(Management msg, ChannelHandlerContext notused) {
			if (logger.isDebugEnabled())
				logger.debug("HeartMonitor got HB from node " + msg.getHeader().getOriginator());

			// if the nodeID is set, we filter messages on it
			if (!nodeID.equals(msg.getHeader().getOriginator()))
				return;
			else if (msg.hasGraph()) {
				logger.info("Received graph responses from " + msg.getHeader().getOriginator());
			} else if (msg.hasBeat()) {
				logger.info("Received HB response: " + msg.getHeader().getOriginator());
			} else
				logger.error("Received management response from unexpected host: " + msg.getHeader().getOriginator());
		}

		@Override
		public void connectionClosed() {
			logger.error("Management port connection failed");
		}

		@Override
		public void connectionReady() {
			logger.info("Management port is ready to receive messages");
		}

	}

}
