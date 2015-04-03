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

import io.netty.channel.Channel;

import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.core.Mgmt.Management;

/**
 * The management queue exists as an instance per process (node)
 * 
 * @author gash
 * 
 */
public class ManagementQueue {
	protected static Logger logger = LoggerFactory.getLogger("management");

	protected static LinkedBlockingDeque<ManagementQueueEntry> inbound = new LinkedBlockingDeque<ManagementQueueEntry>();
	protected static LinkedBlockingDeque<ManagementQueueEntry> outbound = new LinkedBlockingDeque<ManagementQueueEntry>();

	// TODO static is problematic
	private static OutboundMgmtWorker oworker;
	private static InboundMgmtWorker iworker;

	// not the best method to ensure uniqueness
	private static ThreadGroup tgroup = new ThreadGroup("ManagementQueue-" + System.nanoTime());

	public static void startup() {
		if (iworker != null)
			return;

		iworker = new InboundMgmtWorker(tgroup, 1);
		iworker.start();
		oworker = new OutboundMgmtWorker(tgroup, 1);
		oworker.start();
	}

	public static void shutdown(boolean hard) {
		// TODO shutdon workers
	}

	public static void enqueueRequest(Management req, Channel ch) {
		try {
			ManagementQueueEntry entry = new ManagementQueueEntry(req, ch);
			inbound.put(entry);
		} catch (InterruptedException e) {
			logger.error("message not enqueued for processing", e);
		}
	}

	public static void enqueueResponse(Management reply, Channel ch) {
		try {
			ManagementQueueEntry entry = new ManagementQueueEntry(reply, ch);
			outbound.put(entry);
		} catch (InterruptedException e) {
			logger.error("message not enqueued for reply", e);
		}
	}

	public static class ManagementQueueEntry {
		public ManagementQueueEntry(Management req, Channel ch) {
			this.req = req;
			this.channel = ch;
		}

		public Management req;
		public Channel channel;
	}
}
