/*
 * copyright 2012, gash
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
package poke.server.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.core.Mgmt.Management;
import poke.server.management.ManagementQueue.ManagementQueueEntry;
import poke.server.managers.ElectionManager;
import poke.server.managers.HeartbeatManager;
import poke.server.managers.JobManager;
import poke.server.managers.NetworkManager;
import poke.server.managers.RaftManager;

/**
 * The inbound management worker is the cortex for all work related to the
 * Health and Status (H&S) of the node.
 * 
 * Example work includes processing job bidding, elections, network connectivity
 * building. An instance of this worker is blocked on the socket listening for
 * events. If you want to approximate a timer, executes on a consistent interval
 * (e.g., polling, spin-lock), you will have to implement a thread that injects
 * events into this worker's queue.
 * 
 * HB requests to this node are NOT processed here. Nodes making a request to
 * receive heartbeats are in essence requesting to establish an edge (comm)
 * between two nodes. On failure, the connecter must initiate a reconnect - to
 * produce the heartbeatMgr.
 * 
 * On loss of connection: When a connection is lost, the emitter will not try to
 * establish the connection. The edge associated with the lost node is marked
 * failed and all outbound (enqueued) messages are dropped (TBD as we could
 * delay this action to allow the node to detect and re-establish the
 * connection).
 * 
 * Connections are bi-directional (reads and writes) at this time.
 * 
 * @author gash
 * 
 */
public class InboundMgmtWorker extends Thread {
	protected static Logger logger = LoggerFactory.getLogger("management");

	int workerId;
	boolean forever = true;

	public InboundMgmtWorker(ThreadGroup tgrp, int workerId) {
		super(tgrp, "inbound-mgmt-" + workerId);
		this.workerId = workerId;

		if (ManagementQueue.outbound == null)
			throw new RuntimeException("connection worker detected null queue");
	}

	@Override
	public void run() {
		while (true) {
			if (!forever && ManagementQueue.inbound.size() == 0)
				break;

			try {
				// block until a message is enqueued
				ManagementQueueEntry msg = ManagementQueue.inbound.take();

				if (logger.isDebugEnabled())
					logger.debug("Inbound management message received");

				Management mgmt = (Management) msg.req;
				if (mgmt.hasBeat()) {
					/**
					 * Incoming: this is from a node we requested to create a
					 * connection (edge) to. In other words, we need to track
					 * that this connection is healthy by receiving HB messages.
					 * 
					 * Incoming are connections this node establishes, which is
					 * handled by the HeartbeatPusher.
					 */
					logger.info("In Inbound Beat ");
					 HeartbeatManager.getInstance().processRequest(mgmt);

					/**
					 * If we have a network (more than one node), check to see
					 * if a election manager has been declared. If not, start an
					 * election.
					 * 
					 * The flaw to this approach is from a bootstrap PoV.
					 * Consider a network of one node (myself), an event-based
					 * monitor does not detect the leader is myself. However, I
					 * cannot allow for each node joining the network to cause a
					 * leader election.
					 */
					 ElectionManager.getInstance().assessCurrentState(mgmt);
					

				} else if (mgmt.hasRaftmessage()) {
					RaftManager.getInstance().processRequest(mgmt);
					// ElectionManager.getInstance().processRequest(mgmt);
				} else if (mgmt.hasGraph()) {
					NetworkManager.getInstance().processRequest(mgmt, msg.channel);
				} else
					logger.error("Unknown management message");

			} catch (InterruptedException ie) {
				break;
			} catch (Exception e) {
				logger.error("Unexpected processing failure, halting worker.", e);
				break;
			}
		}

		if (!forever) {
			logger.info("connection queue closing");
		}
	}
}
