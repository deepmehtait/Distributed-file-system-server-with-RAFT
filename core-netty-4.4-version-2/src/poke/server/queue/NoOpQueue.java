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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.comm.App.Request;

/**
 * an implementation that does nothing with incoming requests.
 * 
 * @author gash
 * 
 */
public class NoOpQueue implements ChannelQueue {
	protected static Logger logger = LoggerFactory.getLogger("server");
	private String queueName;

	public NoOpQueue() {
		queueName = this.getClass().getName();
	}

	@Override
	public void shutdown(boolean hard) {
		logger.info(queueName + ": queue shutting down");
	}

	@Override
	public void enqueueRequest(Request req, Channel notused) {
		logger.info(queueName + ": request received");
	}

	@Override
	public void enqueueResponse(Request reply, Channel notused) {
		logger.info(queueName + ": response received");
	}

}
