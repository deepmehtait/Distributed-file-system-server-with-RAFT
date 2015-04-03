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
package poke.server.queue;

import io.netty.channel.Channel;

import java.lang.Thread.State;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.comm.App.Request;

/**
 * A literal single queue per server allows the server to use a
 * first-come-first-fulfillment policy. This policy is not desirable as it
 * complicates enforcement of fairness and removing 'dead' tasks. Considerations
 * for a better design is a tiered (buckets) of queues and a per-client designs.
 * 
 * Note this prototype to demonstrate a single queue implementation - this class
 * is incomplete
 * 
 * @author gash
 * 
 */
public class OnlyOneQueue implements ChannelQueue {
	protected static Logger logger = LoggerFactory.getLogger("server");

	// The queues feed work to the inbound and outbound threads (workers). The
	// threads perform a blocking 'get' on the queue until a new event/task is
	// enqueued. This design prevents a wasteful 'spin-lock' design for the
	// threads
	private static LinkedBlockingDeque<OneQueueEntry> inbound = new LinkedBlockingDeque<OneQueueEntry>();
	private static LinkedBlockingDeque<OneQueueEntry> outbound = new LinkedBlockingDeque<OneQueueEntry>();

	// TODO modify to support a pool of workers. Note: with a pool, event
	// completion is not guaranteed to be ordered. For instance, given tasks A,
	// B, C. During execution task A takes longer to complete. This would allow
	// tasks B and C to run before A completes (potentially before saving its
	// work). If the work is order dependent, inconsistent, unexpected behavior
	// will likely occur. With a single worker this is not possible.
	private static OutboundAppWorker oworker;
	private static InboundAppWorker iworker;

	// not the best method to ensure uniqueness
	private static ThreadGroup tgroup = new ThreadGroup("OnlyOneQ-" + System.nanoTime());

	protected OnlyOneQueue() {
		init();
	}

	private void init() {
	}

	@Override
	public void shutdown(boolean hard) {
		// Two choices are possible:
		// 1. go through the queues removing messages from this channel
		// 2. mark the channel as dead, to inform the workers to ignore enqueued
		// requests/responses - not the best approach as we are forced to hold
		// onto the the channel instance. Use a hash?

		if (hard) {
			// drain queues, don't allow graceful completion
			inbound.clear();
			outbound.clear();
		}

		if (iworker != null) {
			iworker.forever = false;
			if (iworker.getState() == State.BLOCKED || iworker.getState() == State.WAITING)
				iworker.interrupt();
			iworker = null;
		}

		if (oworker != null) {
			oworker.forever = false;
			if (oworker.getState() == State.BLOCKED || oworker.getState() == State.WAITING)
				oworker.interrupt();
			oworker = null;
		}
		;
	}

	@Override
	public void enqueueRequest(Request req, Channel channel) {
		try {
			OneQueueEntry oqe = new OneQueueEntry(req, channel);
			inbound.put(oqe);
		} catch (InterruptedException e) {
			logger.error("message not enqueued for processing", e);
		}
	}

	@Override
	public void enqueueResponse(Request reply, Channel channel) {
		if (reply == null)
			return;

		try {
			OneQueueEntry oqe = new OneQueueEntry(reply, channel);
			outbound.put(oqe);
		} catch (InterruptedException e) {
			logger.error("message not enqueued for reply", e);
		}
	}

	public static class OneQueueEntry {
		public Channel channel;
		public Request req;

		public OneQueueEntry(Request req, Channel channel) {
			this.req = req;
			this.channel = channel;
		}
	}
}
