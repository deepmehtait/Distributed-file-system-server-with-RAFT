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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import poke.client.ClientCommand;
import poke.client.ClientPrintListener;
import poke.client.comm.CommListener;


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

/*	public void run() {
		ClientCommand cc = new ClientCommand("192.168.0.5", 5570);
		CommListener listener = new ClientPrintListener("Route demo");
		cc.addListener(listener);
		//Send Poke message to Connect to Server
		cc.sendRegisterRequest();

		//After Poke message send a file to server
		cc.sendJobsRequest("/home/ankit/Downloads/test.jpg");
		
	}*/

	public static void main(String[] args) {
		try {
				ClientCommand cc = new ClientCommand("192.168.0.5", 5570);
				CommListener listener = new ClientPrintListener("Route demo");
				cc.addListener(listener);
				cc.sendRegisterRequest();
				
				int value = 0;
				while (true) {
					System.out.println("1. Poke");
					System.out.println("2. Send Image");
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					value = Integer.parseInt(br.readLine());
					switch (value) {
					case 1:
						//After Poke message send a file to server
						cc.sendJobsRequest("/home/ankit/Downloads/test.jpg");
						break;
					}
				}
//				jab.run();
			
			// we are running asynchronously
			
			//System.exit(0);
				

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
