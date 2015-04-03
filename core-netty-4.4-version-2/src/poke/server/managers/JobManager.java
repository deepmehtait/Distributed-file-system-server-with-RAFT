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
package poke.server.managers;

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.comm.App.JobBid;
import poke.comm.App.JobProposal;
import poke.server.conf.ServerConf;

/**
 * The job manager class is used by the system to hold (enqueue) jobs and can be
 * used in conjunction to the voting manager for cooperative, de-centralized job
 * scheduling. This is used to ensure leveling of the servers take into account
 * the diversity of the network.
 * 
 * @author gash
 * 
 */
public class JobManager {
	protected static Logger logger = LoggerFactory.getLogger("job");
	protected static AtomicReference<JobManager> instance = new AtomicReference<JobManager>();

	private static ServerConf conf;

	public static JobManager initManager(ServerConf conf) {
		JobManager.conf = conf;
		instance.compareAndSet(null, new JobManager());
		return instance.get();
	}

	public static JobManager getInstance() {
		// TODO throw exception if not initialized!
		return instance.get();
	}

	public JobManager() {
	}

	/**
	 * a new job proposal has been sent out that I need to evaluate if I can run
	 * it
	 * 
	 * @param req
	 *            The proposal
	 */
	public void processRequest(JobProposal req) {
		if (req == null)
			return;
	}

	/**
	 * a job bid for my job
	 * 
	 * @param req
	 *            The bid
	 */
	public void processRequest(JobBid req) {
	}
}
