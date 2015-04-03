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
package poke.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.comm.App.Request;
import poke.server.queue.ChannelQueue;
import poke.server.queue.QueueFactory;

/**
 * As implemented, this server handler does not share queues or worker threads
 * between connections. A new instance of this class is created for each socket
 * connection.
 * 
 * This approach allows clients to have the potential of an immediate response
 * from the server (no backlog of items in the queue); within the limitations of
 * the VM's thread scheduling. This approach is best suited for a low/fixed
 * number of clients (e.g., infrastructure).
 * 
 * Limitations of this approach is the ability to support many connections. For
 * a design where many connections (short-lived) are needed a shared queue and
 * worker threads is advised (not shown).
 * 
 * @author gash
 * 
 */
public class ServerHandler extends SimpleChannelInboundHandler<Request> {
	protected static Logger logger = LoggerFactory.getLogger("server");

	private ChannelQueue queue;

	public ServerHandler() {
		// logger.info("** ServerHandler created **");
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Request req) throws Exception {
		// processing is deferred to the worker threads
		if (logger.isDebugEnabled())
			logger.debug("---> server got a message from " + req.getHeader().getOriginator());
		queueInstance(ctx.channel()).enqueueRequest(req, ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("Unexpected exception from downstream.", cause);
		ctx.close();
	}

	/**
	 * Isolate how the server finds the queue. Note this cannot return null.
	 * 
	 * @param channel
	 * @return
	 */
	private ChannelQueue queueInstance(Channel channel) {
		// if a single queue is needed, this is where we would obtain a
		// handle to it.

		if (queue != null)
			return queue;
		else {
			queue = QueueFactory.getInstance(channel);

			// on close remove from queue
			channel.closeFuture().addListener(new ConnectionClosedListener(queue));
		}

		return queue;
	}

	public static class ConnectionClosedListener implements ChannelFutureListener {
		private ChannelQueue sq;

		public ConnectionClosedListener(ChannelQueue sq) {
			this.sq = sq;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			// Note re-connecting to clients cannot be initiated by the server
			// therefore, the server should remove all pending (queued) tasks. A
			// more optimistic approach would be to suspend these tasks and move
			// them into a separate bucket for possible client re-connection
			// otherwise discard after some set period. This is a weakness of a
			// connection-required communication design.

			if (sq != null)
				sq.shutdown(true);
			sq = null;
		}

	}
}
