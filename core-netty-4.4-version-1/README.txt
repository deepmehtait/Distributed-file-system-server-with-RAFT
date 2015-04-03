This project provides you with a basic framework to build your 
project 1 nodes. This is not a complete project nor a fully 
tested and robust solution. It is your responsibility to 
extend and validate the project.

Communication:

A common approach for distributed systems is to separate 
domain requests from management (internal) requests. The 
motivation is often driven by a need to provide the highest 
potential to prioritize and control resources. This project 
provides a design for two networks. The connections are:

   1) public for satisfying data requests
   2) private (mgmt) for internal synchronization and 
      coordination

Thundering Herd Problem:

The queue management uses a single thread to manage the 
inbound requests. A scaling option to use a reactive 
model that increases the number of threads as the entries
in the queue increase to handle the increased work load.

This approach is used rather than having many pre-created
threads as we do not want to have resources taken and
awakened all at once to vie for a single task (this is the
thundering heard problem).

Storage:

A storage framework is provided to show you a way (others exists) 
to decouple domain logic from backend persistence.
