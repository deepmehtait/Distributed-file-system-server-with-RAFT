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
package poke.server.managers;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.managers.HeartbeatData.BeatStatus;
import poke.server.monitor.HeartMonitor;

/**
 * This server-side class collects connection monitors (e.g., listeners
 * implement the circuit breaker) that maintain HB communication between nodes
 * (to client/requester).
 * 
 * @author gash
 * 
 */
public class HeartbeatPusher extends Thread {
	protected static Logger logger = LoggerFactory.getLogger("heartbeat");
	protected static AtomicReference<HeartbeatPusher> instance = new AtomicReference<HeartbeatPusher>();

	private ConcurrentLinkedQueue<HeartMonitor> monitors = new ConcurrentLinkedQueue<HeartMonitor>();
	private int sConnectRate = 2000; // msec
	private boolean forever = true;

	public static HeartbeatPusher getInstance() {
		instance.compareAndSet(null, new HeartbeatPusher());
		return instance.get();
	}

	/**
	 * The connector will only add nodes for connections that this node wants to
	 * establish. Outbound (we send HB messages to) requests do not come through
	 * this class.
	 * 
	 * @param node
	 */
	public void connectToThisNode(int iamNode, HeartbeatData node) {
		// null data is not allowed
		if (node == null || node.getNodeId() < 0)
			throw new RuntimeException("Null nodes or negative IDs are not allowed");

		// register the node to the manager that is used to determine if a
		// connection is usable by the public messaging
		HeartbeatManager.getInstance().addAdjacentNode(node);

		// this class will monitor this channel/connection and together with the
		// manager, we create the circuit breaker pattern to separate
		// health-status from usage.
		HeartMonitor hm = new HeartMonitor(iamNode, node.getHost(), node.getMgmtport(), node.getNodeId());
		monitors.add(hm);

		// artifact of the client-side listener - processing is done in the
		// inbound mgmt worker
		HeartbeatStubListener notused = new HeartbeatStubListener(node);
		hm.addListener(notused);
	}

	@Override
	public void run() {
		if (monitors.size() == 0) {
			logger.info("HB connection monitor not started, no connections to establish");
			return;
		} else
			logger.info("HB connection monitor starting, node has " + monitors.size() + " connections");

		while (forever) {
			try {
				Thread.sleep(sConnectRate);

				// try to establish connections to our nearest nodes
				for (HeartMonitor hb : monitors) {
					if (!hb.isConnected()) {
						try {
							if (logger.isDebugEnabled())
								logger.debug("attempting to connect to node: " + hb.getNodeInfo());
							hb.startHeartbeat();
						} catch (Exception ie) {
							// do nothing
						}
					}
				}
			} catch (InterruptedException e) {
				logger.error("Unexpected HB connector failure", e);
				break;
			}
		}
		logger.info("HeartbeatPusher: ending heartbeatMgr connection monitoring thread");
	}

	/**
	 * This functionality has been moved into the HeartbeatManager
	 * 
	 * @deprecated see HeartbeatManager
	 */
	private void validateConnection() {
		// validate connections this node wants to create
		for (HeartbeatData hb : HeartbeatManager.getInstance().incomingHB.values()) {
			// receive HB - need to check if the channel is readable
			if (hb.getChannel() == null) {
				if (hb.getStatus() == BeatStatus.Active || hb.getStatus() == BeatStatus.Weak) {
					hb.setStatus(BeatStatus.Failed);
					hb.setLastFailed(System.currentTimeMillis());
					hb.incrementFailures();
				}
			} else if (hb.getChannel().isOpen()) {
				if (hb.getChannel().isWritable()) {
					if (System.currentTimeMillis() - hb.getLastBeat() >= hb.getBeatInterval()) {
						hb.incrementFailures();
						hb.setStatus(BeatStatus.Weak);
					} else {
						hb.setStatus(BeatStatus.Active);
						hb.setFailures(0);
					}
				} else
					hb.setStatus(BeatStatus.Weak);
			} else {
				if (hb.getStatus() != BeatStatus.Init) {
					hb.setStatus(BeatStatus.Failed);
					hb.setLastFailed(System.currentTimeMillis());
					hb.incrementFailures();
				}
			}
		}

		// validate connections this node wants to create
		for (HeartbeatData hb : HeartbeatManager.getInstance().outgoingHB.values()) {
			// emit HB - need to check if the channel is writable
			if (hb.getChannel() == null) {
				if (hb.getStatus() == BeatStatus.Active || hb.getStatus() == BeatStatus.Weak) {
					hb.setStatus(BeatStatus.Failed);
					hb.setLastFailed(System.currentTimeMillis());
					hb.incrementFailures();
				}
			} else if (hb.getChannel().isOpen()) {
				if (hb.getChannel().isWritable()) {
					if (System.currentTimeMillis() - hb.getLastBeat() >= hb.getBeatInterval()) {
						hb.incrementFailures();
						hb.setStatus(BeatStatus.Weak);
					} else {
						hb.setStatus(BeatStatus.Active);
						hb.setFailures(0);
					}
				} else
					hb.setStatus(BeatStatus.Weak);
			} else {
				if (hb.getStatus() != BeatStatus.Init) {
					hb.setStatus(BeatStatus.Failed);
					hb.setLastFailed(System.currentTimeMillis());
					hb.incrementFailures();
				}
			}
		}
	}
}
