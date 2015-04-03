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

import poke.server.management.client.MgmtClientCommand;
import poke.server.management.client.MgmtListener;
import poke.server.management.client.MgmtPrintListener;

/**
 * DEMO: how to use the command class
 * 
 * @author gash
 * 
 */
public class StartElection {

	public StartElection(String ip, int port) {

		MgmtClientCommand cc = new MgmtClientCommand(ip, port);
		MgmtListener listener = new MgmtPrintListener("demo");

		cc.addListener(listener);
		cc.startElection(ip, port);
	}

	public static void main(String[] args) {
		try {
			if (args.length != 2) {
				System.out.println("usage: ip-address port");
				System.exit(0);
			}

			String ip = args[0];
			int port = Integer.parseInt(args[1]);
			//StartElection se = new StartElection(ip, port);

			// we are running asynchronously
			System.out.println("\nExiting in 5 seconds");
			Thread.sleep(5000);
			System.exit(0);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
