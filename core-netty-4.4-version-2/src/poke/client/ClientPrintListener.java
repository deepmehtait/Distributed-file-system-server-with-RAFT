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
package poke.client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.client.comm.CommListener;
import poke.client.util.ClientUtil;
import poke.comm.App.Header;
import poke.comm.App.Request;

/**
 * example listener that an application would use to receive events.
 * 
 * @author gash
 * 
 */
public class ClientPrintListener implements CommListener {
	protected static Logger logger = LoggerFactory.getLogger("connect");

	private String id;

	public ClientPrintListener(String id) {
		this.id = id;
	}

	@Override
	public String getListenerID() {
		return id;
	}

	@Override
	public void onMessage(Request msg) {
		System.out.println("Received Message for Client");
		if (logger.isDebugEnabled())
			ClientUtil.printHeader(msg.getHeader());

		if (msg.getHeader().getRoutingId().getNumber() == Header.Routing.PING_VALUE)
			ClientUtil.printPing(msg.getBody().getPing());
		else if (msg.getHeader().getRoutingId().getNumber() == Header.Routing.NAMESPACES_VALUE) {
			// namespace responses
		} else if (msg.getHeader().getRoutingId().getNumber() == Header.Routing.JOBS_VALUE) {
			try{
			System.out.println("Recieved Image from Client1");
			 File downloadFile2 = new File("/home/ankit/Downloads/CMPE280/Hello");
	            OutputStream outputStream2 = new BufferedOutputStream(new FileOutputStream(downloadFile2));
	            msg.getBody().getClientMessage().getMsgIdBytes().writeTo(outputStream2);;
	            
			}
			catch(Exception e)
			{
				System.out.println("Exception aaala");
			}
			
			
			
		} else if (msg.getHeader().getRoutingId().getNumber() == Header.Routing.MANAGE_VALUE) {
			// management responses
		} else if(msg.getHeader().getRoutingId().getNumber() == Header.Routing.REGISTER_VALUE){
			// process the response
			System.out.println(msg.getHeader().getReplyMsg());
			System.out.println("Connection is established... Now you can send images to this node");
		}
	}
}
