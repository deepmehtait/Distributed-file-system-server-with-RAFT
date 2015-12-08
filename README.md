# Distributed-file-system-server-with-RAFT

### INTRODUCTION
The project  Snapchat  is  aimed  at  enabling people to  connect, share,  explore,  and  interact  exclusively through pictures. Snapchat allows non‐stop and guaranteed access to the user. The system is enforced to gather, store and stream pictures by building a distributed file system that provides scalable  client operations. The fundamental task is to allow the clients to communicate with each other by transferring images  to  each  other.  This  is  achieved  through  enabling  the  client  with  the  features  of  upload and download of the images to and from the server.

### GOAL OF SNAPCHAT
Snapchat is designed to provide a highly scalable system that supports massively parallel processing of client  requests  and  highly  connected  data  sets.  The  system  implements  the  Raft  algorithm  that essentially elects a leader to perform tasks such as delegating tasks to other servers in the cluster and perform log replication. The design ensures balanced distribution of work.

### COMMUNICATION BACKBONE
Snapchat requires flexible network to support different client operations that are cooperative in nature. This  arrangement  provides  the  quickest  possible  communication  inside  an  unrestricted  network topology. Thus the group of clusters are enabled to communicate and cooperate with one another and solve problems in a collaborative manner.

### TECHNOLOGIES USED
- Programming languages
  - Client Side :  Java
  - Server Side : Core Java (Netty API)
- FTP Server
  - The File Transfer Protocol (FTP) is a standard network protocol used to transfer computer files
- Netty API
  - Netty  is  a  Non‐Blocking  Client‐Server  Framework  in  Java  .Its  built  over  an  API  for  Channel programming (An abstraction above the usual Socket programming) and thus imparts its non‐blocking nature to Netty). 
  - In addition to that the Concurrency model of core Java is coupled with Netty API for parallely listening on ports and separately serving the requests. 
- Google ProtoBuf
  - It is a multi‐platform data inter‐exchange format, compatible with Java, Python, C++. Data flow over the network  is  being  taken  care  of  by  the  Protobuf  with  development  being  done  over  their  respective interfaces assumed as beans.

### FEATURES OF THE SOLUTION
We processed ***10K files in 98 seconds*** with a leader node distributing jobs among 3 follower nodes. The distributed file system implements Raft Algorithm. We had 2 clients, each connected to one server and an FTP server. The client sends the file transfer request to a node in the cluster. The server then uploads the file to the FTP server and checks if it is the leader and if it is, it broadcasts the request and the logs to all the other nodes in the cluster. If it is not the leader, it forwards the request to the leader and then the  leader  broadcasts  the  request  and  logs.  The  node  that  is  attached  to  the  receiver  client  then downloads the image file from the FTP server and sends it to the receiver client. The leader ensures the log of the images is maintained on all the servers in a synchronized manner.

#### Raft Algorithm
A consensus algorithm is: a cluster of servers should record a series of records ("log entries") in response to requests from clients of the cluster. (It may also take action based on those entries.) It does so in a way that guarantees that the responses seen by clients of the cluster will be consistent EVEN in the face of servers crashing in unpredictable ways (but not losing data that was synced to disk), and networks introducing unpredictable delays or communication blockages.

Raft works as follows. First, it elects a leader, then the leader records the master version of the log, telling other cluster servers what's in that master record and "committing" a log entry then responding to the client of the cluster to acknowledge that entry only when more than half the cluster has recorded a given entry. That works unless the leader crashes or loses communication with too many others; in such a case Raft elects a new leader. The election process is designed to guarantee that any newly elected leader will have (at least) all of the already-committed entries. 	

Raft nodes are always in one of three states: follower, candidate or leader. All nodes initially start out as a follower. In this state, nodes can accept log entries from a leader and cast votes. If no entries are received for some time, nodes self-promote to the candidate state. In the candidate state nodes request votes from their peers. If a candidate receives a quorum of votes, then it is promoted to a leader. The leader must accept new log entries and replicate to all the other followers. In addition, if stale reads are not acceptable, all queries must also be performed on the leader.

Once a cluster has a leader, it is able to accept new log entries. A client can request that a leader append a new log entry, which is an opaque binary blob to Raft. The leader then writes the entry to durable storage and attempts to replicate to a quorum of followers. Once the log entry is considered committed, it can be applied to a finite state machine. The finite state machine is application specific, and in Consul's case, we use LMDB to maintain cluster state. 

#### Optimisation in Favour of Speed, Fast Response to minimise fault tolerance
- **Fair leader election:** Leader is being elected efficiently and fairly. The candidate’s term is matched with the follower’s term every time a vote request is made. Also the candidate with the most updated logs is favoured.
-	**Faster Recovery:** If the network is currently without a leader, the election is started as soon as first HeartBeat (after the leader went down) is transmitted in the network, this allows an efficient network recovery.
-	**App transmission is separate from Management transmission:** Management ports are used only for leader election and Append RPCs and app ports are used only for serving client requests.
-	**Minimal Network Clogging during App network communication:** Although we are using a mesh topology followers respond directly to Leaders and not through any other path (not via any node) so in effect its point to point communication for serving client requests.
-	**Faster fault recovery:** If a node is down immediately the node is removed from the management connections hashMap. Once the failed node joins back the network, its logs are updated with the current leader’s log.
-	**Minimal Payload:** The accepted message format in pipeline is of type “poke.comm.App.Request” so as to reduce payload weight over the network.

#### FTP STORAGE
The FTP server is used as a storage for storing all the image files that the clients send to other clients. It acts as a central repository for the nodes in the cluster to store files that need to be transferred to the client. FTP server also provides for a secure storage medium. 

### IMPLEMENTATION 
##### Leader election using Raft Algorithm
1.  Each node joins in a FOLLOWER state. A timer starts for each node. The nodes wait for the Append RPC’s until timeout, if it receives a Append RPC’s before timeout, then Leader exists, else it does not exist.
2. If Leader exists, the node functions as a follower.
3. If Leader does not exist, the node changes its state to CANDIDATE, increments the term and sends a RequestVote message.
4. Once the RequestVote message is received by the other nodes in the network, it checks if the candidate’s log is as up-to-date as its log and if the Candidate’s term is greater than or equal to its term.
5. If both the conditions are satisfied, then it casts its vote to the candidate.
6. If the Candidate receives majority, it declares itself as Leader, changes the state to LEADER and sends the message to all the other nodes about its Leadership.
7. If the Candidate does not get the majority, then no Leader is elected for this term, the term ends and new term starts.
8. For the next term, new Election process starts, where all the nodes wait until the timeout and the election process is carried about as stated above.

##### Log Replication

1.	The Leader creates the log file whenever it receives the request to transfer the images. 
2.	The Log created by the Leader contains the log index and the imageName. 
3.	Before appending, the Leader increments the index and updates the prevIndex to current index.
4.	Once the log file is created, it is sent to the other nodes in the cluster by appending the log to the Append RPC message.
5.	Keeping a Boolean variable appendLogs to check if the log needs to be added to the Append RPC or not. It is set to true when a server receives a request to transfer an image.
6.	If appendLogs = true, then append the log file to the heartbeat and then it set to false.
7.	If appendLogs = false, then there is no new log to append. 
8.	When a node receives the logs, it checks the prevIndexes until the log entry matches with the entry in the Leader’s log file.  It then updates its log file as per the Leader log file.

##### High level flow of the solution
1.	Two channels are established when a node joins the network, a management channel and an App channel.
2.	An App channel is also established when the client registers with the node.
3.	Client then sends the request to the server node with Routing_Id= JOBS
4.	The node checks if it is the Leader. If the node is not the leader then the node forwards the request via APP channel to the leader using Routing_Id = FORWARD (every node saves the current term’s LeaderId). Then it creates the log entries, appends the updated log to the heartbeat and sends the message through the management channel and  broadcast the request to the other nodes via app channel with Routing_Id = REPORTS.
5.	 If the node is a Leader, then as explained above Leader does the log creation and broadcasts the request.
6.	All the nodes receive the request from the Leader and check if the request has the receiverclientID, if it contains the receiverClientId, then the nodes check if the receiverClientId is connected to itself. If it is connected, then it sends the request to that particular client.
7.	If the request does not contain the receiverClientId, then all the nodes broadcast the request to all the clients via APP channel.

##### Cluster Communication
A cluster is a connection of nodes in the network. The cluster components are connected to each other through LAN. Performance and availability is increased by the use of cluster.

- **Intra-Cluster Communication:** A server is selected among the nodes connected which works as a cluster leader. It is responsible to listen to the requests from the network. Job bidding is done further inside the cluster  
- **Inter-Cluster Communication:** The clusters connect with each other only after the Leader is elected in all the clusters.
Then the Nodes in the cluster sends a JOIN Request to nodes of the other cluster, then it establishes a connection. The channel information and nodeId are stored in a hashMap. This information is used to communicate in future. 
The client Request contains the information whether the message is for intra-cluster or inter-cluster. If the message is for intra-cluster, then it flows through the cluster as explained above. If the message is for inter-cluster, then the request is sent to all nodes in the other clusters. When request is received by the node in our cluster, it first uploads the image to the FTP server, then the flow continues as explained above (as explained in the high level flow of solution). 


#### ARCHITECTURE
![alt tag](https://cloud.githubusercontent.com/assets/8681531/11649793/9cfbec5a-9d37-11e5-84de-ced6acaa3791.png)
