#include<iostream> //printf
#include<string.h>    //strlen
#include<sys/socket.h>    //socket
#include<arpa/inet.h> //inet_addr
#include "comm.pb.h"

using namespace std;
typedef vector<char> data_buffer;

//Method to serialize size in big endian format
void encode_header(data_buffer& buf, unsigned size)
{
        assert(buf.size() >= 4);
        buf[0] = static_cast<char>((size >> 24) & 0xFF); //storing first byte in position 0
        buf[1] = static_cast<char>((size >> 16) & 0xFF); //storing 2nd byte in position 1
        buf[2] = static_cast<char>((size >> 8) & 0xFF);
        buf[3] = static_cast<char>(size & 0xFF);
}

//Method to sign up
void signUp(std::string userName, std::string password, std::string firstName, std::string lastName)
    {
	Request r = Request::default_instance();
        Payload* p = r.mutable_body(); //mutable functions give default instances of everything
        JobOperation* jobOp = p->mutable_job_op();
	jobOp->set_action(JobOperation_JobAction_ADDJOB);

        JobDesc* jBuilder = jobOp->mutable_data();
        jBuilder->set_name_space("signUp");
        jBuilder->set_job_id("signUp");
        jBuilder->set_owner_id(1);
        jBuilder->set_status(JobDesc_JobCode_JOBRUNNING);

        NameValueSet* nameValueBuilder1 = jBuilder->mutable_options();
        nameValueBuilder1->set_name("userName");
        nameValueBuilder1->set_value(userName);
        nameValueBuilder1->set_node_type(NameValueSet_NodeType_NODE);

	NameValueSet* nameValueBuilder2 = nameValueBuilder1->add_node(); //Repeated
        nameValueBuilder2->set_name("password");
        nameValueBuilder2->set_value(password);
        nameValueBuilder2->set_node_type(NameValueSet_NodeType_NODE);

	NameValueSet* nameValueBuilder3 = nameValueBuilder1->add_node(); //Repeated
        nameValueBuilder3->set_name("firstName");
        nameValueBuilder3->set_value(firstName);
        nameValueBuilder3->set_node_type(NameValueSet_NodeType_NODE);

	NameValueSet* nameValueBuilder4 = nameValueBuilder1->add_node(); //Repeated
        nameValueBuilder4->set_name("lastName");
        nameValueBuilder4->set_value(lastName);
        nameValueBuilder4->set_node_type(NameValueSet_NodeType_NODE);

        Header* h = r.mutable_header();
        h->set_routing_id(Header_Routing_JOBS);
        h->set_originator("client");
        h->set_tag("signUp");
        h->set_tonode("3");

	vector<char> buffer;        
	unsigned msg_size = r.ByteSize(); //size of the request
        buffer.resize(4 + msg_size);
        encode_header(buffer, msg_size); //The length of the msg also should be serialized. This method does that: Equivalent to struct.pack 
//('>L') > meaning big endian
        r.SerializeToArray(&buffer[4], msg_size); //The first 4 bytes contain the length of the request. 
						//So, start the actual data from 4th byte and serialize that

        try
        {
            cout << "Calling send method" << endl;
            int sock;
    	    struct sockaddr_in server;
            //Create socket
            sock = socket(AF_INET , SOCK_STREAM , 0);
            if (sock == -1)
            {
		cout <<"Could not create socket";
                return;
            }
            server.sin_addr.s_addr = inet_addr("127.0.0.1");
            server.sin_family = AF_INET;
            server.sin_port = htons(5573);
            //Connect to remote server
            int y = connect(sock , (struct sockaddr *)&server , sizeof(server));
		cout << "Connect : " << y << endl;	

	    int x = send(sock , &buffer[0] , (msg_size + 4) , 0); 
        	cout << "Send : " << x << endl;
	    //Code to receive
	    int numbytes = 0;
	    char buf[255];
	    memset(buf, '\0', sizeof(buf));
       	    numbytes = read(sock, buf, sizeof(buf));
	    if(numbytes)
	    {
		cout << "READ " << numbytes << " bytes of response." << endl;
		cout << "Response Received... " <<endl;
		for(int ind=0;ind<numbytes;ind++)
			cout<<buf[ind];
		cout<<endl;
	    } 
	    else
		cout << "Nothing returned " <<endl; 
            }
            catch (int e)
        {
            cout << "Unable to deliver message, queuing" << endl;
        }
    }


//Method to sign in
void signIn(std::string userName, std::string password)
    {
	Request r = Request::default_instance();
        Payload* p = r.mutable_body(); 
        JobOperation* jobOp = p->mutable_job_op();
	jobOp->set_action(JobOperation_JobAction_ADDJOB);

        JobDesc* jBuilder = jobOp->mutable_data();
        jBuilder->set_name_space("signIn");
        jBuilder->set_job_id("signIn");
        jBuilder->set_owner_id(1);
        jBuilder->set_status(JobDesc_JobCode_JOBRUNNING);

        NameValueSet* nameValueBuilder1 = jBuilder->mutable_options();
        nameValueBuilder1->set_name("userName");
        nameValueBuilder1->set_value(userName);
        nameValueBuilder1->set_node_type(NameValueSet_NodeType_NODE);

	NameValueSet* nameValueBuilder2 = jBuilder->mutable_options();
        nameValueBuilder2->set_name("password");
        nameValueBuilder2->set_value(password);
        nameValueBuilder2->set_node_type(NameValueSet_NodeType_NODE);

        Header* h = r.mutable_header();
        h->set_routing_id(Header_Routing_JOBS);
        h->set_originator("client");
        h->set_tag("signIn");
        h->set_tonode("3");

	vector<char> buffer;        
	unsigned msg_size = r.ByteSize(); //size of the request
        buffer.resize(4 + msg_size);
        encode_header(buffer, msg_size); //The length of the msg also should be serialized. This method does that: Equivalent to struct.pack 
//('>L') > meaning big endian
        r.SerializeToArray(&buffer[4], msg_size); 

        try
        {
            cout << "Calling send method" << endl;

            int sock;
    	    struct sockaddr_in server;

            //Create socket
            sock = socket(AF_INET , SOCK_STREAM , 0);
            if (sock == -1)
            {
		cout <<"Could not create socket";
                return;
            }
     
            server.sin_addr.s_addr = inet_addr("127.0.0.1");
            server.sin_family = AF_INET;
            server.sin_port = htons(5573);
 
            //Connect to remote server
            int y = connect(sock , (struct sockaddr *)&server , sizeof(server));
		cout << "Connect : " << y << endl;	

	    int x = send(sock , &buffer[0] , (msg_size + 4) , 0); 
        	cout << "Send : " << x << endl; 

	//Code to receive
	int numbytes = 0;
	char buf[255];
	memset(buf, '\0', sizeof(buf));

       	numbytes = read(sock, buf, sizeof(buf));

	if(numbytes)
	{
		cout << "READ " << numbytes << " bytes of response." << endl;
		cout << "Response Received... " <<endl;
		for(int ind=0;ind<numbytes;ind++)
			cout<<buf[ind];
		cout<<endl;
	}
	else
		cout << "Nothing returned " <<endl;
        }
        catch (int e)
        {
            cout << "Unable to deliver message, queuing" << endl;
        }
    }


//List courses
void listCourseName()
    {
	Request r = Request::default_instance();
        Payload* p = r.mutable_body(); 
        JobOperation* jobOp = p->mutable_job_op();
	jobOp->set_action(JobOperation_JobAction_ADDJOB);

        JobDesc* jBuilder = jobOp->mutable_data();
        jBuilder->set_name_space("listCourses");
        jBuilder->set_job_id("listCourses");
        jBuilder->set_owner_id(1);
        jBuilder->set_status(JobDesc_JobCode_JOBRUNNING);

        Header* h = r.mutable_header();
        h->set_routing_id(Header_Routing_JOBS);
        h->set_originator("client");
        h->set_tag("listCourses");
        h->set_tonode("3");

	vector<char> buffer;        
	unsigned msg_size = r.ByteSize(); //size of the request
        buffer.resize(4 + msg_size);
        encode_header(buffer, msg_size); //The length of the msg also should be serialized. This method does that: Equivalent to struct.pack 
//('>L') > meaning big endian
        r.SerializeToArray(&buffer[4], msg_size); 

        try
        {
            cout << "Calling send method" << endl;

            int sock;
    	    struct sockaddr_in server;

            //Create socket
            sock = socket(AF_INET , SOCK_STREAM , 0);
            if (sock == -1)
            {
		cout <<"Could not create socket";
                return;
            }
     
            server.sin_addr.s_addr = inet_addr("127.0.0.1");
            server.sin_family = AF_INET;
            server.sin_port = htons(5573);
 
            //Connect to remote server
            int y = connect(sock , (struct sockaddr *)&server , sizeof(server));
		cout << "Connect : " << y << endl;	

	    int x = send(sock , &buffer[0] , (msg_size + 4) , 0); 
        	cout << "Send : " << x << endl; 

	//Code to receive
	int numbytes = 0;
	char buf[255];
	memset(buf, '\0', sizeof(buf));

       	numbytes = read(sock, buf, sizeof(buf));

	if(numbytes)
	{
		cout << "READ " << numbytes << " bytes of response." << endl;
		cout << "Response Received... " <<endl;
		for(int ind=0;ind<numbytes;ind++)
			cout<<buf[ind];
		cout<<endl;
	}
	else
		cout << "Nothing returned " <<endl;
        }
        catch (int e)
        {
            cout << "Unable to deliver message, queuing" << endl;
        }
    }


//Method to get description
void getCourseDesc(std::string courseID)
    {
	Request r = Request::default_instance();
        Payload* p = r.mutable_body(); 
        JobOperation* jobOp = p->mutable_job_op();
	jobOp->set_action(JobOperation_JobAction_ADDJOB);

        JobDesc* jBuilder = jobOp->mutable_data();
        jBuilder->set_name_space("getCourseDesc");
        jBuilder->set_job_id("getCourseDesc");
        jBuilder->set_owner_id(1);
        jBuilder->set_status(JobDesc_JobCode_JOBRUNNING);

        NameValueSet* nameValueBuilder1 = jBuilder->mutable_options();
        nameValueBuilder1->set_name("courseID");
        nameValueBuilder1->set_value(courseID);
        nameValueBuilder1->set_node_type(NameValueSet_NodeType_NODE);

        Header* h = r.mutable_header();
        h->set_routing_id(Header_Routing_JOBS);
        h->set_originator("client");
        h->set_tag("getCourseDesc");
        h->set_tonode("1");

	vector<char> buffer;        
	unsigned msg_size = r.ByteSize(); //size of the request
        buffer.resize(4 + msg_size);
        encode_header(buffer, msg_size); //The length of the msg also should be serialized. This method does that: Equivalent to struct.pack 
//('>L') > meaning big endian
        r.SerializeToArray(&buffer[4], msg_size); 

        try
        {
            cout << "Calling send method" << endl;

            int sock;
    	    struct sockaddr_in server;

            //Create socket
            sock = socket(AF_INET , SOCK_STREAM , 0);
            if (sock == -1)
            {
		cout <<"Could not create socket";
                return;
            }
     
            server.sin_addr.s_addr = inet_addr("127.0.0.1");
            server.sin_family = AF_INET;
            server.sin_port = htons(5573);
 
            //Connect to remote server
            int y = connect(sock , (struct sockaddr *)&server , sizeof(server));
		cout << "Connect : " << y << endl;	

	    int x = send(sock , &buffer[0] , (msg_size + 4) , 0); 
        	cout << "Send : " << x << endl;

	//Code to receive
	int numbytes = 0;
	char buf[255];
	memset(buf, '\0', sizeof(buf));

       	numbytes = read(sock, buf, sizeof(buf));

	if(numbytes)
	{
		cout << "READ " << numbytes << " bytes of response." << endl;
		cout << "Response Received... " <<endl;
		for(int ind=0;ind<numbytes;ind++)
			cout<<buf[ind];
		cout<<endl;
	}
	else
		cout << "Nothing returned " <<endl; 
        }
        catch (int e)
        {
            cout << "Unable to deliver message, queuing" << endl;
        }
    }

int main()
{
int i;
std::string userName;
std::string password;
std::string firstName;
std::string lastName;
std::string courseID;
do{
	cout << "1.Sign up" << endl;
	cout << "2.Sign in" << endl;
	cout << "3.List courses" << endl;
	cout << "4.Get course description" << endl;
	cout << "9.Exit" << endl;
	cin >> i;
	
	switch(i){
	case 1: 
		cout << "Enter username" << endl;
		cin >> userName;
		cout << "Enter password" << endl;
		cin >> password;
		cout << "Enter first name" << endl;
		cin >> firstName;
		cout << "Enter last name" << endl;
		cin >> lastName;
		signUp(userName, password, firstName, lastName);
		break;	
		
	case 2: 
		cout << "Enter username" << endl;
		cin >> userName;
		cout << "Enter password" << endl;
		cin >> password;
		signIn(userName, password);
		break;
	case 3: cout << "Listing courses" << endl;
		listCourseName();
		break;
	case 4: cout << "Getting course description" << endl;
		cout << "Enter courseID" << endl;
		cin >> courseID;
		getCourseDesc(courseID);
		break;
	case 9: break;
	default: cout << "Wrong input" << endl;
		 break;

}
} while(i != 9);

return 0;
}
