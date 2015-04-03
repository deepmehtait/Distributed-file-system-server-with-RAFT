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
package poke.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.comm.App.Payload;
import poke.comm.App.Ping;
import poke.comm.App.PokeStatus;
import poke.comm.App.Request;
import poke.server.resources.Resource;
import poke.server.resources.ResourceUtil;

public class PingResource implements Resource {
	protected static Logger logger = LoggerFactory.getLogger("server");

	public PingResource() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see poke.server.resources.Resource#process(eye.Comm.Finger)
	 */
	public Request process(Request request) {
		// TODO add code to process the message/event received
		logger.info("poke: " + request.getBody().getPing().getTag());

		Request.Builder rb = Request.newBuilder();

		// metadata
		rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(), PokeStatus.SUCCESS, null));

		// payload
		Payload.Builder pb = Payload.newBuilder();
		Ping.Builder fb = Ping.newBuilder();
		fb.setTag(request.getBody().getPing().getTag());
		fb.setNumber(request.getBody().getPing().getNumber());
		pb.setPing(fb.build());
		rb.setBody(pb.build());

		Request reply = rb.build();

		return reply;
	}
}
