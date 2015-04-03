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

import io.netty.channel.ChannelFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.management.ManagementQueue.ManagementQueueEntry;

public class OutboundMgmtWorker extends Thread {
	protected static Logger logger = LoggerFactory.getLogger("management");

	int workerId;
	boolean forever = true;

	public OutboundMgmtWorker(ThreadGroup tgrp, int workerId) {
		super(tgrp, "outbound-mgmt-" + workerId);
		this.workerId = workerId;

		if (ManagementQueue.outbound == null)
			throw new RuntimeException("management worker detected null queue");
	}

	@Override
	public void run() {
		while (true) {
			if (!forever && ManagementQueue.outbound.size() == 0)
				break;

			try {
				// block until a message is enqueued
				ManagementQueueEntry msg = ManagementQueue.outbound.take();

				if (logger.isDebugEnabled())
					logger.debug("Outbound management message routing to node " + msg.req.getHeader().getToNode());

				if (msg.channel.isWritable()) {
					boolean rtn = false;
					if (msg.channel != null && msg.channel.isOpen() && msg.channel.isWritable()) {
						ChannelFuture cf = msg.channel.write(msg);

						// blocks on write - use listener to be async
						cf.awaitUninterruptibly();
						rtn = cf.isSuccess();
						if (!rtn)
							ManagementQueue.outbound.putFirst(msg);
					}

				} else {
					logger.info("channel to node " + msg.req.getHeader().getToNode() + " is not writable");
					ManagementQueue.outbound.putFirst(msg);
				}
			} catch (InterruptedException ie) {
				break;
			} catch (Exception e) {
				logger.error("Unexpected management communcation failure", e);
				break;
			}
		}

		if (!forever) {
			logger.info("management outbound queue closing");
		}
	}

}
