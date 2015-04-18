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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.client.comm.CommConnection;
import poke.client.comm.CommListener;
import poke.comm.App.ClientMessage;
import poke.comm.App.Header;
import poke.comm.App.Header.Routing;
import poke.comm.App.Payload;
import poke.comm.App.Ping;
import poke.comm.App.Request;

import com.google.protobuf.ByteString;


/**
 * The command class is the concrete implementation of the functionality of our
 * network. One can view this as a interface or facade that has a one-to-one
 * implementation of the application to the underlining communication.
 * 
 * IN OTHER WORDS (pay attention): One method per functional behavior!
 * 
 * @author gash
 * 
 */
public class ClientCommand {
	protected static Logger logger = LoggerFactory.getLogger("client");

	private String host;
	private int port;
	private CommConnection comm;

	public ClientCommand(String host, int port) {
		this.host = host;
		this.port = port;

		init();
	}

	private void init() {
 		System.out.println("In ClientCommand Init (Host:"+host+"  Port:"+port+")");
 		comm = new CommConnection(host, port);
		if(comm == null)
			System.out.println("Comm is null");
	}

	/**
	 * add an application-level listener to receive messages from the server (as
	 * in replies to requests).
	 * 
	 * @param listener
	 */
	public void addListener(CommListener listener) {
		comm.addListener(listener);
	}
	//Function to Ping from client to Server
	/**
	 * Our network's equivalent to ping
	 * 
	 * @param tag
	 * @param num
	 */
	public void poke(String tag, int num) {
		// data to send
		Ping.Builder f = Ping.newBuilder();
		f.setTag(tag);
		f.setNumber(num);

		// payload containing data
		Request.Builder r = Request.newBuilder();
		Payload.Builder p = Payload.newBuilder();
		p.setPing(f.build());
		r.setBody(p.build());

		// header with routing info
		Header.Builder h = Header.newBuilder();
		h.setOriginator(1000);
		h.setTag("test finger");
		h.setTime(System.currentTimeMillis());
		h.setRoutingId(Header.Routing.PING);
		r.setHeader(h.build());

		Request req = r.build();

		try {
			comm.sendMessage(req);
		} catch (Exception e) {
			logger.warn("Unable to deliver message, queuing");
		}
	}	
	
/*	//Function to Send Request from client to Server
	public void sendRequest(JobAction jAct, String jobId, JobDesc desc) {
		
		//Building Job Operation for request
		JobOperation.Builder j = JobOperation.newBuilder();
		j.setAction(jAct);
		j.setJobId(jobId);
		j.setData(desc);
		
		//Building Payload containing data for job
		Request.Builder r = Request.newBuilder();
		Payload.Builder p = Payload.newBuilder();
		p.setJobOp(j.build());

		//Building Header with routing info
		Header.Builder h = Header.newBuilder();
		h.setOriginator(1000);
		h.setTag("jobs");
		h.setTime(System.currentTimeMillis());
		h.setRoutingId(Header.Routing.JOBS);
		h.setToNode(0);
		h.setIsClusterMsg(false);
		
		//Setting Request parameters
		r.setHeader(h.build());
		r.setBody(p.build());
		
		Request req = r.build();
		
		try {
			comm.sendMessage(req);
		} catch (Exception e) {
			logger.warn("Unable to deliver message, queuing");
		}
	}

*/	public void sendRegisterRequest() {
		Request.Builder r = Request.newBuilder();
		
		
		//Build Header
		Header.Builder h = Header.newBuilder();
		h.setRoutingId(Routing.REGISTER);
		h.setOriginator(1000);
		h.setTag("Register this Client to the node");
		h.setTime(System.currentTimeMillis());
		//NOt setting Poke Status, Reply Message, ROuting Path, Name Value set
		//TO Node is the node to which this client will get connected
		h.setToNode(0);
		
		ClientMessage.Builder clBuilder  = ClientMessage.newBuilder();
		clBuilder.setSenderUserName("client");
		clBuilder.setSenderClientId(1);

		
		//Build Payload
		Payload.Builder p = Payload.newBuilder();
		//Not adding anything as it is just a register message
		p.setClientMessage(clBuilder);

		r.setBody(p);
		r.setHeader(h);
		Request req = r.build();
		try {
			comm.sendMessage(req);
		} catch (Exception e) {
			logger.warn("Unable to deliver message, queuing");
		}
	}

	public void sendJobsRequest(String fileLocation) {
		try{
		//
				Request.Builder r = Request.newBuilder();
		
				//Build Header
				Header.Builder h = Header.newBuilder();
				h.setRoutingId(Routing.JOBS);
				h.setOriginator(1000);
				h.setTag("Sending image to the node");
				h.setTime(System.currentTimeMillis());
				//NOt setting Poke Status, Reply Message, ROuting Path, Name Value set
				//TO Node is the node to which this client will get connected
				h.setToNode(0);
				
				//Build Payload
				Payload.Builder p = Payload.newBuilder();
		        
		        File file = new File(fileLocation);
		        byte []imageInByte = Files.readAllBytes(Paths.get(fileLocation)); 
		        ClientMessage.Builder clientImage = ClientMessage.newBuilder();
		        clientImage.setMsgId("1");
		        clientImage.setSenderUserName("Client1");
		        clientImage.setReceiverUserName("Client2");
		        clientImage.setSenderClientId(1);
		        clientImage.setReceiverClientId(2);
		        System.out.println("Client Receiver ID " + clientImage.getReceiverClientId());
		        clientImage.setMsgText("Hello Client2");
		        clientImage.setMsgImageName(file.getName());
		        clientImage.setMsgImageBits(ByteString.copyFrom(imageInByte));
		        clientImage.setMessageType(poke.comm.App.ClientMessage.MessageType.REQUEST);
		        clientImage.setIsClient(true);
		        clientImage.setBroadcastInternal(true);
		        p.setClientMessage(clientImage);
				r.setBody(p);
				r.setHeader(h);
				Request req = r.build();
				System.out.println("Client Receiver ID " + req.getBody().getClientMessage().getReceiverClientId());
				System.out.println("Client Sender ID " + req.getBody().getClientMessage().getSenderClientId());
				comm.sendMessage(req);
			}
			catch (Exception e) {
				logger.warn("Unable to deliver message, queuing");
			}

		
	}
}
