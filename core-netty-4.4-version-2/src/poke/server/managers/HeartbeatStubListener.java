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

import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.core.Mgmt.Management;
import poke.server.management.ManagementQueue;
import poke.server.monitor.MonitorListener;

/**
 * This class bridges (glues) the server's processing to the general heart
 * monitor that is shared between external clients and internal use.
 * 
 * @author gash
 * 
 */
public class HeartbeatStubListener implements MonitorListener {
	protected static Logger logger = LoggerFactory.getLogger("management");

	private HeartbeatData data;

	public HeartbeatStubListener(HeartbeatData data) {
		this.data = data;
	}

	public HeartbeatData getData() {
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see poke.server.monitor.MonitorListener#getListenerID()
	 */
	@Override
	public Integer getListenerID() {
		return data.getNodeId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see poke.server.monitor.MonitorListener#onMessage(eye.Comm.Management)
	 */
	@Override
	public void onMessage(Management msg, ChannelHandlerContext ctx) {
		if (logger.isDebugEnabled())
			logger.debug("HB from node " + msg.getHeader().getOriginator());

		// forward for processing
		ManagementQueue.enqueueRequest(msg, ctx.channel());
	}

	@Override
	public void connectionClosed() {
		// note a closed management port is likely to indicate the primary port
		// has failed as well
	}

	@Override
	public void connectionReady() {
		// do nothing at the moment
	}
}
