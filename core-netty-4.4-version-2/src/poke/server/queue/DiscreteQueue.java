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
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.comm.App.NameValueSet;
import poke.comm.App.Request;

/**
 * The discrete simulation queue represents an ordered continuous queuing of
 * events based on a simulation time, queueTime, of the event and of the event
 * engine. Events that are inserted into the queue with a queueTime > engine's
 * queueTime will be rejected.
 * 
 * Event engine: This queue is the event execution engine of a simulation.
 * 
 * Time advancement: queueTime is a monotonically increasing value represented
 * by a float. When events (tasks) are added to to a node, the queueTime of the
 * event determines placement in the queue. Decimal precision of a queueTime
 * allows events to be inserted between enqueued events.
 * 
 * Ties: The application (service) must provide a class-method to be used to for
 * tie breaking.
 * 
 * Tasks with the same queueTime creates a situation where the engine cannot
 * determine ordering. When such a condition occurs, the engine uses the
 * application supplied compare operation.
 * 
 * The simulation queue uses a single queue per node.
 * 
 * Requires Java 1.6
 * 
 * @author gash
 * 
 */
public class DiscreteQueue implements ChannelQueue {
	protected static Logger logger = LoggerFactory.getLogger("server");

	// The inbound queue for simulation cannot be directly added to as the
	// queueTime requires sorting of events. This introduces a saw-tooth
	// execution pattern (execution on lambda intervals).
	private static NavigableMap<Float, QueueTask> queue = new TreeMap<Float, QueueTask>();

	private static LinkedBlockingDeque<OneQueueEntry> inbound = new LinkedBlockingDeque<OneQueueEntry>();
	private static LinkedBlockingDeque<OneQueueEntry> outbound = new LinkedBlockingDeque<OneQueueEntry>();

	// TODO worker instances must be created to use this class
	// Single worker to serialize all requests
	private static OutboundAppWorker oworker;
	private static InboundAppWorker iworker;

	// the queue's current simulation time
	private static float queueTime = 0.0f;

	// not the best method to ensure uniqueness
	private static ThreadGroup tgroup = new ThreadGroup("DiscreteQ-" + System.nanoTime());

	protected DiscreteQueue() {
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
		if (req == null || channel == null)
			return;

		float rQueueTime = this.queueTime;
		int N = req.getHeader().getOptionsCount();
		if (N > 0) {
			for (int n = 0; n < N; n++) {
				NameValueSet nvs = req.getHeader().getOptions(n);
				try {
					if (nvs.getName().equals("queueTime")) {
						String str = nvs.getValue();
						rQueueTime = Float.parseFloat(str);
						break;
					}
				} catch (NumberFormatException e) {
					logger.warn("Received task without a queueTime");
					return;
				}
			}
		}

		// reject a task if the queueTime is greater than or equal to the
		// engine's queueTime
		if (rQueueTime >= this.queueTime) {
			logger.warn("Task rejected as " + rQueueTime + " has already past.");
			return;
		}

		QueueTask qt = new QueueTask();
		qt.queueTime = rQueueTime;
		qt.req = req;
		qt.channel = channel;

		// ordered insertion
		queue.put(qt.queueTime, qt);
	}

	/**
	 * when a queueTime advance is received, the inbound queue can be searched
	 * for tasks that are greater than the new queueTime.
	 * 
	 * @param toQueueTime
	 */
	public synchronized void advanceQueueTime(float toQueueTime) {
		SortedMap<Float, QueueTask> tasks = queue.headMap(toQueueTime, true);
		for (QueueTask qt : tasks.values()) {
			enqueueIn(qt.req, qt.channel);
		}

		// removes these tasks from the queue
		tasks.clear();

		// advance the simulation time
		queueTime = toQueueTime;
	}

	private void enqueueIn(Request req, Channel channel) {
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

	private static class QueueTask implements Comparable<QueueTask> {
		float queueTime;
		Request req;
		Channel channel;

		/**
		 * TODO It would be better to have the config pass in a comparable
		 * implementation than to have the engine determine this.
		 */
		@Override
		public int compareTo(QueueTask other) {
			// tie breaking is performed on creation time
			if (this.req.getHeader().getTime() > other.req.getHeader().getTime())
				return 1;
			else if (this.req.getHeader().getTime() < other.req.getHeader().getTime())
				return -1;
			else {
				// cannot have a tie so need to have another way of determining
				// order. This should be deterministic (repeatable).

				// final compare - the task is from the same node and has
				// the same creation time so, our tie breaker is to always
				// choose myself
				if (this.req.getHeader().getOriginator() == other.req.getHeader().getOriginator())
					return 0;
				else if (this.req.getHeader().getOriginator() > other.req.getHeader().getOriginator())
					return 1;
				else
					return -1;
			}
		}
	}
}
