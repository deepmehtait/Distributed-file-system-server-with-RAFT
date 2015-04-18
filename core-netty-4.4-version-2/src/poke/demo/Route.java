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
 * DEMO: how to send register and jobs to server. Run this file using startClient.sh
 * 
 * @author ankit
 * 
 */
public class Route {
	private String tag;

	public Route(String tag) {
		this.tag = tag;
	}
	public static void main(String[] args) {
		try {
				ClientCommand cc = new ClientCommand("localhost", 5570);
				CommListener listener = new ClientPrintListener("Client demo");
				cc.addListener(listener);
				System.out.println("Client sending Register Request");
				cc.sendRegisterRequest();
				int value = 0;
				while (true) {
					System.out.println("Press 1 to Send Image");
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					value = Integer.parseInt(br.readLine());
					switch (value) {
					case 1:
						cc.sendJobsRequest("/home/ankit/Downloads/test.jpg");
						break;
					}
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
