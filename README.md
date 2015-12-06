# Distributed-file-system-server-with-RAFT

### INTRODUCTION
The project  Snapchat  is  aimed  at  enabling people to  connect, share,  explore,  and  interact  exclusively through pictures. Snapchat allows non‐stop and guaranteed access to the user. The system is enforced to gather, store and stream pictures by building a distributed file system that provides scalable  client operations. The fundamental task is to allow the clients to communicate with each other by transferring images  to  each  other.  This  is  achieved  through  enabling  the  client  with  the  features  of  upload and download of the images to and from the server.

### GOAL OF SNAPCHAT
Snapchat is designed to provide a highly scalable system that supports massively parallel processing of client  requests  and  highly  connected  data  sets.  The  system  implements  the  Raft  algorithm  that essentially elects a leader to perform tasks such as delegating tasks to other servers in the cluster and perform log replication. The design ensures balanced distribution of work.

### COMMUNICATION BACKBONE
Snapchat requires flexible network to support different client operations that are cooperative in nature. This  arrangement provides  the  quickest  possible  communication  inside  an  unrestricted  network topology. Thus the group of clusters are enabled to communicate and cooperate with one another and solve problems in a collaborative manner.

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
