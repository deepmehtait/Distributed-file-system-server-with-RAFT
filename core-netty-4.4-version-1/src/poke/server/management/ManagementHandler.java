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
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.core.Mgmt.Management;

/**
 * The Netty pipeline delegates message handling to this class that in turn
 * enqueues it for managers to process. This provides the ability to scale
 * (within a process) by deferring execution of tasks when all threads (inbound
 * workers) are busy.
 * 
 * @author gash
 * 
 */
public class ManagementHandler extends SimpleChannelInboundHandler<Management> {
	protected static Logger logger = LoggerFactory.getLogger("management");

	public ManagementHandler() {
		// logger.info("** HeartbeatHandler created **");
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Management req) throws Exception {
		// processing is deferred to the worker threads
		 logger.info("---> management got a message from " + req.getHeader().getOriginator());
		ManagementQueue.enqueueRequest(req, ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.error("management channel inactive");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("Unexpected exception from downstream.", cause);
		ctx.close();
	}

	/**
	 * usage:
	 * 
	 * <pre>
	 * channel.getCloseFuture().addListener(new ManagementClosedListener(queue));
	 * </pre>
	 * 
	 * @author gash
	 * 
	 */
	public static class ManagementClosedListener implements ChannelFutureListener {
		// private ManagementQueue sq;

		public ManagementClosedListener(ManagementQueue sq) {
			// this.sq = sq;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			// if (sq != null)
			// sq.shutdown(true);
			// sq = null;
		}

	}
}
