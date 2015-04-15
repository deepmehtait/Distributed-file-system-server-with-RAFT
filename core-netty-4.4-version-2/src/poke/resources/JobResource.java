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
package poke.resources;

import io.netty.channel.Channel;

import java.io.FileInputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.comm.App.ClientMessage;
import poke.comm.App.Header;
import poke.comm.App.Header.Routing;
import poke.comm.App.Payload;
import poke.comm.App.Request;
import poke.ftp.ftpConnection;
import poke.server.managers.ConnectionManager;
import poke.server.managers.RaftManager;
import poke.server.resources.Resource;

public class JobResource implements Resource {
	protected static Logger logger = LoggerFactory.getLogger("server");
	@Override
	public void process(Request request, Channel ch) {
		
		logger.info("Received Image from Client");
		ftpConnection ftp = new ftpConnection();
		//Upload File
		ftp.connect();
		InputStream image=request.getBody().getClientMessage().getMsgIdBytes().newInput();
		ftp.uploadFile(image);
		
		Header reqHeader = request.getHeader();
		ClientMessage reqClientMsg=request.getBody().getClientMessage();
		if(RaftManager.getInstance().whoIsTheLeader() == reqHeader.getToNode())
		{
			System.out.println("Job ma receiver " + reqClientMsg.getReceiverClientId());
			ForwardResource fs = new ForwardResource();
			fs.process(request, ch);
		}
		else
		{
			//Send a APP message to leader that new image is uploaded to FTP server
			ClientMessage.Builder cBuilder  = ClientMessage.newBuilder();
			
			
			
			cBuilder.setMsgId(reqClientMsg.getMsgId());
			cBuilder.setSenderUserName(reqClientMsg.getSenderUserName());
			cBuilder.setReceiverUserName(reqClientMsg.getReceiverUserName());
			cBuilder.setSenderClientId(reqClientMsg.getSenderClientId());
			cBuilder.setReceiverClientId(reqClientMsg.getReceiverClientId());
			cBuilder.setMsgText(reqClientMsg.getMsgText());
			cBuilder.setMsgImageName(reqClientMsg.getMsgImageName());
			//cBuilder.setMsgImageBits(reqClientMsg.getMsgImageBits());
			cBuilder.setMessageType(reqClientMsg.getMessageType());
			cBuilder.setIsClient(true);
			cBuilder.setBroadcastInternal(true);
			
			
			Header.Builder h = Header.newBuilder();
			
			
			h.setRoutingId(Routing.FORWARD);
			h.setOriginator(reqHeader.getOriginator());
			h.setTag(reqHeader.getTag());
			h.setTime(System.currentTimeMillis());
			h.setToNode(reqHeader.getToNode());
			
			//add client msg to body
			Payload.Builder payload = Payload.newBuilder();
			payload.setClientMessage(cBuilder);
				
			
			//add body to request
			Request.Builder req = Request.newBuilder();
			req.setBody(payload);
			req.setHeader(h);
			Request r = req.build();
			ConnectionManager.sendToNode(r, RaftManager.getInstance().whoIsTheLeader());
		}
	}
		/*
		//DownLOad File
		try {
			ftp.connect();
			InputStream is1 = ftp.retrieveImage();
			Request.Builder rb = Request.newBuilder();
			Header.Builder h = Header.newBuilder();
			h.setOriginator(1000);
			h.setTag("jobs");
			h.setTime(System.currentTimeMillis());
			h.setRoutingId(Header.Routing.JOBS);
			h.setToNode(0);
			h.setIsClusterMsg(false);
			// metadata
			rb.setHeader(h);
			// payload
			Payload.Builder p = Payload.newBuilder();
			//	Client Message
		    ClientMessage.Builder clientImage = ClientMessage.newBuilder();
	        clientImage.setMsgId("1");
	        clientImage.setSenderUserName("Client1");
	        clientImage.setReceiverUserName("Client2");
	        clientImage.setMsgText("Hello Client2");
	        clientImage.setMsgImageName("Scott.jpg");
	        System.out.println(is1);
			clientImage.setMsgImageBits(ByteString.readFrom(is1));
			p.setClientMessage(clientImage);
		    rb.setHeader(h);
		    rb.setBody(p);
		    Request reply = rb.build();
		    return reply;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}*/		

}
