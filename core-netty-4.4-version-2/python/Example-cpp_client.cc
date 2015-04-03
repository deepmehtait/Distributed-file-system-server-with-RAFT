#include <iostream>
#include <fstream>
#include <string>
#include "comm.pb.h"
#include <endian.h>

#include <cstring>      // Needed for memset
#include <cstdio>
#include <sys/socket.h> // Needed for the socket functions
#include <netdb.h>      // Needed for the socket functions

using namespace std;

bool debug = false;


void hexDump (char *desc, void *addr, int len) {
    int i;
    unsigned char buff[17];
    unsigned char *pc = (unsigned char*)addr;

    // Output description if given.
    if (desc != NULL)
        printf ("%s:\n", desc);

    // Process every byte in the data.
    for (i = 0; i < len; i++) {
        // Multiple of 16 means new line (with line offset).

        if ((i % 16) == 0) {
            // Just don't print ASCII for the zeroth line.
            if (i != 0)
                printf ("  %s\n", buff);

            // Output the offset.
            printf ("  %04x ", i);
        }

        // Now the hex code for the specific character.
        printf (" %02x", pc[i]);

        // And store a printable ASCII character for later.
        if ((pc[i] < 0x20) || (pc[i] > 0x7e))
            buff[i % 16] = '.';
        else
            buff[i % 16] = pc[i];
        buff[(i % 16) + 1] = '\0';
    }

    // Pad out last line if not exactly 16 characters.
    while ((i % 16) != 0) {
        printf ("   ");
        i++;
    }

    // And print the final ASCII bit.
    printf ("  %s\n", buff);
}

void socket_read_n(int socketfd, int n, char * incomming_data_buffer){

    ssize_t bytes_recieved = 0;
    bytes_recieved = recv(socketfd, incomming_data_buffer,n, 0);

    // If no data arrives, the program will just wait here until some data arrives.
    if (bytes_recieved == 0) cout << "host shut down." << endl ;
    if (bytes_recieved == -1) cout << "recieve error!" << endl ;
}


Request receive_msg_from_server(int sock){

 	//Waiting to recieve framing of Protobuf
 	char * frame  = new char[4];
    socket_read_n(sock,4,frame);
    unsigned int *frame_len = (unsigned int *) frame;
    unsigned int len = be32toh(*frame_len);

    //Waiting to recieve Protobuf Message
    char * msg_buf  = new char[len];
    socket_read_n(sock,len,msg_buf);

	//Parse from string
	Request response;
	response.ParseFromString(msg_buf);
	return response;
	
}

void pack_protobuf(char * framed_output,const char * char_output,unsigned int len_buf){

	unsigned int total_length = len_buf + sizeof(len_buf);
	unsigned int endian_len = htobe32(len_buf);

	memcpy(framed_output, &endian_len, sizeof(len_buf));
	memcpy(framed_output + sizeof(len_buf), char_output, strlen(char_output));
}

Request serialize_and_send(Request *request,string output){

	//Serialize Protobuf
	request->SerializeToString(&output);

	struct addrinfo host_info; 
	struct addrinfo *host_info_list; // Pointer to the to the linked list of host_info's.
   
	memset(&host_info, 0, sizeof host_info);
	host_info.ai_family = AF_INET;     // IP version not specified. Can be both.
  	host_info.ai_socktype = SOCK_STREAM; // Use SOCK_STREAM for TCP or SOCK_DGRAM for UDP.

  	int status = getaddrinfo("192.168.0.64", "5573", &host_info, &host_info_list);

	if (status != 0)  std::cout << "getaddrinfo error" << gai_strerror(status) ;

	//Creating a Socket
	int socketfd ; 
	socketfd = socket(host_info_list->ai_family, host_info_list->ai_socktype, host_info_list->ai_protocol);
	if (socketfd == -1)  std::cout << "socket error " ;

	//Connect to Server
	status = connect(socketfd, host_info_list->ai_addr, host_info_list->ai_addrlen);
	if (status == -1)  std::cout << "connect error" ;

	const char * char_output = output.c_str();	
	unsigned int len_buf = strlen(char_output);
	unsigned int total_length = len_buf + sizeof(len_buf);
	char *framed_output = new char[total_length];

	//Packing the Protobuf packet with frame header
	pack_protobuf(framed_output,char_output,len_buf);

	if(debug){
		cout << endl << endl << endl;
		hexDump(NULL, framed_output, total_length);
		cout <<endl << endl <<endl;
	}

	//Send Over Socket
	ssize_t bytes_sent;
	bytes_sent = send(socketfd, framed_output, total_length, 0);

	if(debug){
		cout << bytes_sent;
	}
		
	Request response;
	response = receive_msg_from_server(socketfd);
	return response;

}


void handle_signup(){

	string email, password, fname, lname;

	cout << endl << "Enter FirstName : ";
	cin >> fname;
	cout << endl << "Enter LastName: " ;
	cin >> lname;
	cout << endl << "Enter Email Address : " ;
	cin >> email;
	cout << endl << "Enter Password : ";
	cin >> password;

	//Request Packet
	Request *request = new Request();
	//Header
	Header *header = new Header();
	header->set_routing_id(Header::JOBS);
	header->set_originator("client");
	//header->set_tag("header for signup");
	//Payload
	Payload *payload = new Payload();
	//JobOperation
	JobOperation *job_operation = new JobOperation();
	job_operation->set_action(JobOperation::ADDJOB);
	//JobDesc
	JobDesc *job_desc = new JobDesc();
	job_desc->set_name_space("signup");
	job_desc->set_owner_id(1);
	job_desc->set_job_id("signup");
	job_desc->set_status(JobDesc::JOBQUEUED);

	//NameValueSet
	NameValueSet *nvm1 ;
	NameValueSet *nvm2 ;
	NameValueSet *nvm3 ;
	NameValueSet *nvm4 ;

	NameValueSet *nvm = new NameValueSet();
	nvm->set_node_type(NameValueSet::NODE);
	nvm1 = nvm->add_node();
	nvm2 = nvm->add_node();
	nvm3 = nvm->add_node();
	nvm4 = nvm->add_node();

	nvm1->set_name("fname");
	nvm1->set_value(fname);
	nvm1->set_node_type(NameValueSet::VALUE);	

	nvm2->set_name("lname");
	nvm2->set_value(lname);
	nvm2->set_node_type(NameValueSet::VALUE);

	nvm3->set_name("email");
	nvm3->set_value(email);
	nvm3->set_node_type(NameValueSet::VALUE);
	
	nvm4->set_name("password");
	nvm4->set_value(password);
	nvm4->set_node_type(NameValueSet::VALUE);

	//Nesting Header
	job_desc->set_allocated_options(nvm);
	job_operation->set_allocated_data(job_desc);
	payload->set_allocated_job_op(job_operation);
	request->set_allocated_body(payload);
	request->set_allocated_header(header);

	Request response;
	string output ;
	response = serialize_and_send(request,output);

	//Handle ouput
	string response_string = response.body().job_op().data().options().value();
	cout <<response_string << endl;
	cout << "Signup Successful !! Welcome" << endl;

}

void handle_signin(){

	string email,password;
	cout << endl <<"Enter Email Address : ";
	cin >> email;
	cout <<  endl <<"Enter Password : ";
	cin >> password;

	//Request Packet
	Request *request = new Request();
	//Header
	Header *header = new Header();
	header->set_routing_id(Header::JOBS);
	header->set_originator("client");
	header->set_tag("hheader for signin");
	//Payload
	Payload *payload = new Payload();
	//JobOperation
	JobOperation *job_operation = new JobOperation();
	job_operation->set_action(JobOperation::ADDJOB);
	//JobDesc
	JobDesc *job_desc = new JobDesc();
	job_desc->set_name_space("signin");
	job_desc->set_owner_id(1);
	job_desc->set_job_id("signin");
	job_desc->set_status(JobDesc::JOBQUEUED);

	//NameValueSet
	NameValueSet *nvm1 ;
	NameValueSet *nvm2 ;
	NameValueSet *nvm = new NameValueSet();
	nvm->set_node_type(NameValueSet::NODE);
	nvm1 = nvm->add_node();
	nvm2 = nvm->add_node();
	
	nvm1->set_name("email");
	nvm1->set_value(email);
	nvm1->set_node_type(NameValueSet::VALUE);
	
	nvm2->set_name("password");
	nvm2->set_value(password);
	nvm2->set_node_type(NameValueSet::VALUE);

	//Nesting Header
	job_desc->set_allocated_options(nvm);
	job_operation->set_allocated_data(job_desc);
	payload->set_allocated_job_op(job_operation);
	request->set_allocated_body(payload);
	request->set_allocated_header(header);

	Request response;
	string output ;
	response = serialize_and_send(request,output);

	//Handle ouput
	string response_string = response.body().job_op().data().options().value();
	cout << endl << response_string <<endl;

}

void list_courses(){
	//Request Packet
	Request *request = new Request();
	//Header
	Header *header = new Header();
	header->set_routing_id(Header::JOBS);
	header->set_originator("1234");
	header->set_tag("header for list Files");
	//Payload
	Payload *payload = new Payload();
	//JobOperation
	JobOperation *job_operation = new JobOperation();
	job_operation->set_action(JobOperation::ADDJOB);
	//JobDesc
	JobDesc *job_desc = new JobDesc();
	job_desc->set_name_space("listcourses");
	job_desc->set_owner_id(1);
	job_desc->set_job_id("listcourses");
	job_desc->set_status(JobDesc::JOBQUEUED);
	//Nesting Header
	job_operation->set_allocated_data(job_desc);
	payload->set_allocated_job_op(job_operation);
	request->set_allocated_body(payload);
	request->set_allocated_header(header);

	Request response;
	string output ;
	response = serialize_and_send(request,output);

	//Parsing Response
	int total_courses = response.body().job_op().data().options().node().size();
	cout << "total_courses :: " << total_courses << endl;

   	cout<< endl <<"*********************List Of Courses *************************" <<endl;

	for(int i = 0; i < total_courses ; i ++ ){
		cout <<response.body().job_op().data().options().node(i).value() << endl ;
	}
	cout << "****************************" << endl;
}

void get_course_description(){
	

	string coursename;

	cout << endl << "Enter Course Name :: ";
	cin >> coursename;

	Request *request = new Request();

	//Header
	Header *header = new Header();
	header->set_routing_id(Header::JOBS);
	header->set_originator("client");
	header->set_tag("header for desc Files");

	//Payload
	Payload *payload = new Payload();
	
	//JobOperation
	JobOperation *job_operation = new JobOperation();
	job_operation->set_action(JobOperation::ADDJOB);

	//JobDesc
	JobDesc *job_desc = new JobDesc();
	job_desc->set_name_space("coursedescription");
	job_desc->set_owner_id(1);
	job_desc->set_job_id("coursedescription");
	job_desc->set_status(JobDesc::JOBQUEUED);

	//NameValueSet
	NameValueSet *nvm = new NameValueSet();
	nvm->set_name("coursename");
	nvm->set_value(coursename);
	nvm->set_node_type(NameValueSet::VALUE);

	//Nesting Header
	job_desc->set_allocated_options(nvm);
	job_operation->set_allocated_data(job_desc);
	payload->set_allocated_job_op(job_operation);
	request->set_allocated_body(payload);
	request->set_allocated_header(header);

	Request response;
	string output ;
	response = serialize_and_send(request,output);
    cout << endl <<"coursedescription ::" <<response.body().job_op().data().options().value() <<endl;
}

// Main function:  
int main(int argc, char* argv[]) {

	// Verify that the version of the library that we linked against is
	// compatible with the version of the headers we compiled against.
	GOOGLE_PROTOBUF_VERIFY_VERSION;

	int reply ;

	while(1){
		cout << endl << "********MOOC*************" << endl;
		cout << endl << "1.Signup 2.Signin 3.List Courses 4.Get Course Description 5.Exit" << endl;
		cin >> reply;

		switch (reply)
		{
		case 1:  handle_signup();
		    break;

		case 2 : handle_signin();
		    break;

		case 3: list_courses();;
		    break;

		case 4: get_course_description();
		    break;
		}

		if(reply == 5)
			break;
	}

	return 0;
}
