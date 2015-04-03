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
package poke.server.storage.heap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.comm.App.JobDesc;
import poke.comm.App.NameSpace;
import poke.server.storage.TenantStorage;

/**
 * A memory-based storage primitive. This class acts as a storage model for Job
 * processing using basic hash tables. Adding alternate in-memory storage can be
 * easily achieved by extending and overwriting the CRUD functions.
 * 
 * The intended purpose of this implementation is to bootstrap your storage
 * option. No MT protection or rollback are provided.
 * 
 * @author gash
 * 
 */
public class TenantHeapStorage implements TenantStorage {
	protected static Logger logger = LoggerFactory.getLogger("storage");
	
	private static String sNoName = "";
	private HashMap<Long, DataNameSpace> spaces = new HashMap<Long, DataNameSpace>();

	@Override
	public boolean addJob(String namespace, JobDesc job) {
		if (job == null)
			return false;
		DataNameSpace dns = null;
		if (namespace == null) {
			namespace = sNoName;
			NameSpace.Builder bldr = NameSpace.newBuilder();
			bldr.setNsId(createSpaceKey());
			bldr.setName(sNoName);
			bldr.setOwner("none");
			bldr.setCreated(System.currentTimeMillis());
			dns = new DataNameSpace(bldr.build());
			spaces.put(dns.nsb.getNsId(), dns);
		} else
			dns = lookupByName(namespace);

		if (dns == null)
			throw new RuntimeException("Unknown namspace: " + namespace);

		String key = null;
		if (job.hasJobId()) {
			// likely a job that is forwarded to another node
			key = job.getJobId();
		} else {
			// note because we store the protobuf instance (read-only)
			key = createJobKey();
			JobDesc.Builder bldr = JobDesc.newBuilder(job);
			bldr.setJobId(key);
			job = bldr.build();
		}

		return dns.add(key, job);
	}

	@Override
	public boolean removeJob(String namespace, String jobId) {
		if (namespace == null)
			namespace = sNoName;

		boolean rtn = false;
		DataNameSpace list = spaces.get(namespace);
		if (list != null)
			rtn = list.remove(jobId);

		return rtn;
	}

	@Override
	public boolean updateJob(String namespace, JobDesc job) {
		return addJob(namespace, job);
	}

	@Override
	public List<JobDesc> findJobs(String namespace, JobDesc criteria) {
		DataNameSpace dns = spaces.get(namespace);
		if (dns == null)
			return null;
		else {
			// TODO return the jobs matching a query is not implemented
			return new ArrayList<JobDesc>(dns.jobs.values());
		}
	}

	@Override
	public NameSpace getNameSpaceInfo(long spaceId) {
		DataNameSpace dns = spaces.get(spaceId);
		if (dns != null)
			return dns.getNameSpace();
		else
			return null;
	}

	@Override
	public List<NameSpace> findNameSpaces(NameSpace criteria) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NameSpace createNameSpace(NameSpace space) {
		if (space == null)
			return null;

		DataNameSpace dns = lookupByName(space.getName());
		if (dns != null)
			throw new RuntimeException("Namespace already exists");

		NameSpace.Builder bldr = NameSpace.newBuilder();
		if (space.hasNsId()) {
			dns = spaces.get(space.getNsId());
			if (dns != null)
				throw new RuntimeException("Namespace ID already exists");
			else
				bldr.setNsId(space.getNsId());
		} else
			bldr.setNsId(createSpaceKey());

		bldr.setName(space.getName());
		bldr.setCreated(System.currentTimeMillis());
		bldr.setLastModified(bldr.getCreated());

		if (space.hasOwner())
			bldr.setOwner(space.getOwner());

		if (space.hasDesc())
			bldr.setDesc(space.getDesc());

		NameSpace ns = bldr.build();
		dns = new DataNameSpace(ns);
		spaces.put(dns.getNameSpace().getNsId(), dns);

		return ns;
	}

	@Override
	public boolean removeNameSpace(long spaceId) {
		DataNameSpace dns = spaces.remove(spaceId);
		try {
			return (dns != null);
		} finally {
			if (dns != null)
				dns.release();
			dns = null;
		}
	}

	private DataNameSpace lookupByName(String name) {
		if (name == null)
			return null;

		for (DataNameSpace dns : spaces.values()) {
			if (dns.getNameSpace().getName().equals(name))
				return dns;
		}
		return null;
	}

	private String createJobKey() {
		// TODO time is not discrete so we could end up with duplicate job IDs.
		// We need key generator
		return "job." + System.nanoTime();
	}

	private Long createSpaceKey() {
		// TODO time is not discrete so we could end up with duplicate job IDs.
		// We need key generator
		return System.nanoTime();
	}

	private static class DataNameSpace {
		// store the builder to allow continued updates to the metadata of the
		// space
		NameSpace.Builder nsb;
		HashMap<String, JobDesc> jobs = new HashMap<String, JobDesc>();

		public DataNameSpace(NameSpace ns) {
			nsb = NameSpace.newBuilder(ns);
		}

		public void release() {
			if (jobs != null) {
				jobs.clear();
				jobs = null;
			}

			nsb = null;
		}

		public NameSpace getNameSpace() {
			return nsb.build();
		}

		public boolean add(String key, JobDesc job) {
			jobs.put(key, job);
			nsb.setLastModified(System.currentTimeMillis());
			return true;
		}

		public boolean remove(String key) {
			JobDesc job = jobs.remove(key);
			if (job == null)
				return false;
			else {
				nsb.setLastModified(System.currentTimeMillis());
				return true;
			}
		}
	}

	@Override
	public void init(Properties cfg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void release() {
		// TODO Auto-generated method stub

	}
}
