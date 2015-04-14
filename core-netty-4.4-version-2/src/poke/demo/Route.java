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
package poke.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import poke.client.ClientCommand;
import poke.client.ClientPrintListener;
import poke.client.comm.CommListener;
import poke.comm.App.ClientMessage;
import poke.comm.App.JobDesc;
import poke.comm.App.JobDesc.JobCode;
import poke.comm.App.JobOperation.JobAction;
import poke.comm.App.NameValueSet;
import poke.comm.App.NameValueSet.NodeType;

import com.google.protobuf.ByteString;


/**
 * DEMO: how to use the command class to implement a ping
 * 
 * @author gash
 * 
 */
public class Route {
	private String tag;
	private int count;

	public Route(String tag) {
		this.tag = tag;
	}

	public void run() {
		ClientCommand cc = new ClientCommand("localhost", 5570);
		CommListener listener = new ClientPrintListener("Route demo");
		cc.addListener(listener);
		//Send Poke message to Connect to Server
		cc.sendRegisterRequest();

		//After Poke message send a file to server
		cc.sendJobsRequest();
		
	}

	public static void main(String[] args) {
		try {
			Route jab = new Route("route");
			jab.run();

			// we are running asynchronously
			System.out.println("\nExiting in 5 seconds");
			Thread.sleep(50000);
			//System.exit(0);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
