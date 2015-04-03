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
package poke.server.voting;


/**
 * this data represents a basic majority vote, multi-election or other voting
 * strategies will require this to be subclassed and refactored.
 * 
 * @author gash
 * 
 */
@SuppressWarnings("unused")
public abstract class VoteState {
	protected String desc;
	protected int version = 0;
	protected int state = -1; // should come from the proto state directly?
	protected Long electionID;
	protected Long startedOn, lastVoteOn;

	public VoteState() {
	}

	public abstract void castVote(Vote vote);
}