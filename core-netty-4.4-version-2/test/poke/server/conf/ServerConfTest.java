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
package poke.server.conf;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import org.junit.Test;

import poke.comm.App.Header;
import poke.resources.ForwardResource;
import poke.server.conf.ServerConf.ResourceConf;
import poke.server.election.FloodMaxElection;

import com.vividsolutions.jts.util.Assert;

public class ServerConfTest {
	private static String outSample = "test/poke/server/conf/example.json";

	@Test
	public void testReadingConf() throws Exception {
		File cfg = new File(outSample);
		byte[] raw = new byte[(int) cfg.length()];
		BufferedInputStream br = new BufferedInputStream(new FileInputStream(cfg));
		br.read(raw);
		ServerConf conf = JsonUtil.decode(new String(raw), ServerConf.class);

		Assert.equals(5570, conf.getPort());
		Assert.equals(0, conf.getNodeId());
		Assert.equals(2, conf.getAdjacent().getAdjacentNodes().size());
	}

	@Test
	public void testBasicConf() throws Exception {
		ServerConf conf = new ServerConf();

		// general configuration
		conf.setNodeId(0);
		conf.setNodeName("zero");
		conf.setPort(5570);
		conf.setMgmtPort(5571);

		// data storages - picks up the no-op default values
		ServerConf.StorageConf sc = new ServerConf.StorageConf();
		conf.setStorage(sc);

		// internal resources
		conf.setForwardingImplementation(ForwardResource.class.getName());
		conf.setElectionImplementation(FloodMaxElection.class.getName());

		// adjacent nodes that this server has a connection to
		NodeDesc node = new NodeDesc();
		node.setNodeId(1);
		node.setNodeName("one");
		node.setPort(5571);
		node.setMgmtPort(5671);
		conf.addAdjacentNode(node);

		node = new NodeDesc();
		node.setNodeId(2);
		node.setNodeName("two");
		node.setPort(5572);
		node.setMgmtPort(5672);
		conf.addAdjacentNode(node);

		// there are a couple of ways to manage routing. One is to have a
		// resource-per-request (a class per message) or a
		// resource-per-category. This implementation uses a per-category.
		//
		// Note a resource can support multiple requests by having duplicate
		// entries map to the same class

		ResourceConf rsc = new ResourceConf();
		rsc.setName("finger");
		rsc.setId(Header.Routing.PING_VALUE);
		rsc.setClazz("poke.resources.PingResource");
		conf.addResource(rsc);

		rsc = new ResourceConf();
		rsc.setName("namespaces");
		rsc.setId(Header.Routing.NAMESPACES_VALUE);
		rsc.setClazz("poke.resources.NameSpaceResource");
		conf.addResource(rsc);

		rsc = new ResourceConf();
		rsc.setName("jobs");
		rsc.setId(Header.Routing.JOBS_VALUE);
		rsc = new ResourceConf();
		rsc.setClazz("poke.resources.JobResource");
		conf.addResource(rsc);

		// rsc.setName("manage");
		// rsc.setId(Header.Routing.MANAGE_VALUE);
		// rsc.setClazz("poke.resources.NameSpaceResource");
		// conf.addResource(rsc);

		String json = JsonUtil.encode(conf);
		FileWriter fw = null;
		try {
			fw = new FileWriter(new File("/tmp/poke.cfg"));
			fw.write(json);
			fw.close();

			System.out.println("JSON: " + json);
		} finally {
			fw.close();
		}
	}
}
