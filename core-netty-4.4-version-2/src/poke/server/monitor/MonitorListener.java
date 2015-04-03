/*
 * copyright 2013, gash
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
package poke.server.monitor;

import io.netty.channel.ChannelHandlerContext;
import poke.core.Mgmt.Management;

/**
 * to receive management messages
 * 
 * @author gash
 * 
 */
public interface MonitorListener {

	/**
	 * Identifier of the host (node ID) that this listener is associated to.
	 * Note if a client uses multiple listeners to the same host, the ID must be
	 * unique thus, the Node's ID cannot be used without additional
	 * qualification.
	 * 
	 * @return
	 */
	public abstract Integer getListenerID();

	/**
	 * process management messages
	 * 
	 * @param msg
	 */
	public abstract void onMessage(Management msg, ChannelHandlerContext ctx);

	/**
	 * called when the connection fails or is not readable.
	 */
	public abstract void connectionClosed();

	/**
	 * called when the connection to the node is ready. This may occur on the
	 * initial connection or when re-connection after a failure.
	 */
	public abstract void connectionReady();

}