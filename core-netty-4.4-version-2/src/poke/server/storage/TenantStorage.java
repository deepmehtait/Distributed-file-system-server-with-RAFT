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
package poke.server.storage;

import java.util.List;
import java.util.Properties;

import poke.comm.App.JobDesc;
import poke.comm.App.NameSpace;

/**
 * The managment's storage interface - the persistent storage of the server's
 * tenant-based operations.
 * 
 * This is not intended to represent functionality for managing job and
 * namespace separation of tenants (resources) that are added to the framework.
 * 
 * Resource specific storage should not use this.
 * 
 * @author gash
 * 
 */
public interface TenantStorage {

	void init(Properties cfg);

	void release();

	NameSpace getNameSpaceInfo(long spaceId);

	List<NameSpace> findNameSpaces(NameSpace criteria);

	NameSpace createNameSpace(NameSpace space);

	boolean removeNameSpace(long spaceId);

	boolean addJob(String namespace, JobDesc doc);

	boolean removeJob(String namespace, String jobId);

	boolean updateJob(String namespace, JobDesc doc);

	List<JobDesc> findJobs(String namespace, JobDesc criteria);
}
