import socket
import struct
import time
import comm_pb2



#get destination IP Address and port no
def get_server_ip_address():
	global hostaddress
	global portaddress
	hostaddress= raw_input(" \n Please enter the host address:")
	portaddress= int(raw_input(" \n Please enter the port number:"))

# Request-Response
# while making the protobuf request : 
# Length field to be prepended
# > = BigEndian, i.e. the network byte order
# L = unsigned long - 4 bytes

# Ping Request-Response
def request_ping():
    request = comm_pb2.Request()
    request.header.routing_id = request.header.PING
    request.header.originator = "client"
    request.header.tag = "header tag"
    request.header.time = long(time.time())
    request.header.toNode = "zero" # Sending to node with node.id "zero"
    request.body.ping.number = 1234
    request.body.ping.tag = "test"
    pingRequest = request.SerializeToString()
    
    packed_len = struct.pack('>L', len(pingRequest))
    # Sending Ping Request to the server's public port
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    #host = socket.gethostname() # Testing on own computer
    #port = 5570 # Public Port
    s.connect((hostaddress,portaddress))
    # Prepending the length field and sending
    s.sendall(packed_len + pingRequest)
    s.close()

# Sign up Request-Response
def request_sign_up(fullName, username, password):
    # Building Sign up Request
    request = comm_pb2.Request()
    request.header.routing_id = request.header.JOBS
    request.header.originator = "client"
    request.header.tag = "SignUp"
    request.header.time = long(time.time())
    # Sending to node with node.id as nothing, leader will decide to which node the request should be sent
    request.header.toNode = ""
    request.body.sign_up.full_name=fullName
    request.body.sign_up.user_name=username
    request.body.sign_up.password=password

    signUpRequest = request.SerializeToString()
    packed_len = struct.pack('>L', len(signUpRequest))
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    #host = socket.gethostname() # Testing on own computer
    #port = 5570 # Public Port
    s.connect((hostaddress,portaddress))
    #	 Prepending the length field and sending
    s.sendall(packed_len + signUpRequest)
    # Receive at s, a message of type Request and close connection
    print_str= get_message(s, comm_pb2.Request);
    print "\n", print_str.header.reply_msg
    s.close()
    return print_str.header.reply_msg


# Sign In Request-Response
def request_sign_in(username, password):
    # Building Sign in Request
    request = comm_pb2.Request()
    request.header.routing_id = request.header.JOBS
    request.header.originator = "client"
    request.header.tag = "SignIn"
    request.header.time = long(time.time())
    request.header.toNode = "zero" # Sending to node with node.id "zero"
    request.body.sign_in.user_name=username
    request.body.sign_in.password=password

    signinRequest = request.SerializeToString()
    packed_len = struct.pack('>L', len(signinRequest))
    
    # Sending Sign In Request to the server's public port
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    #host = socket.gethostname() # Testing on own computer
    #port = 5570 # Public Port
    s.connect((hostaddress,portaddress))
    # Prepending the length field and sending
    s.sendall(packed_len + signinRequest)
    # Receive at s, a message of type Request and close connection
    print_str= get_message(s, comm_pb2.Request);
    #print "\n",print_str
    print print_str.header.reply_msg
    s.close()
    return print_str.header.reply_msg
    

# Get Course List Request-Response
def request_course_list():
    # Building getcourse Request
    request = comm_pb2.Request()
    request.header.routing_id = request.header.JOBS
    request.header.originator = "client"
    request.header.tag = "RequestList"
    request.header.time = long(time.time())
    request.header.toNode = "zero" # Sending to node with node.id "zero"
    request.body.get_course.course_id=-1

    courseListRequest = request.SerializeToString()
    packed_len = struct.pack('>L', len(courseListRequest))
    
    # Sending courseList Request to the server's public port
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    #host = socket.gethostname() 		 # Testing on own computer
    #port = 5570 				 # Public Port
    s.connect((hostaddress,portaddress))
    #	 Prepending the length field and sending
    s.sendall(packed_len + courseListRequest)
    # Receive at s, a message of type Request and close connection
    print_str= get_message(s, comm_pb2.Request);
    print "\n",print_str
    s.close()
    
 # Search a Course Request-Response
def request_search_course(courseId):
    # Building getcourse Request
    request = comm_pb2.Request()
    request.header.routing_id = request.header.JOBS
    request.header.originator = "client"
    request.header.tag = "SearchCourse"
    request.header.time = long(time.time())
    request.header.toNode = "zero" # Sending to node with node.id "zero"
    request.body.get_course.course_id=courseId

    courseSearchRequest = request.SerializeToString()
    packed_len = struct.pack('>L', len(courseSearchRequest))
    
    # Sending courseList Request to the server's public port
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    #host = socket.gethostname() # Testing on own computer
    #port = 5570 # Public Port
    s.connect((hostaddress,portaddress))
    #	 Prepending the length field and sending
    s.sendall(packed_len + courseSearchRequest)
    # Receive at s, a message of type Request and close connection
    print_str= get_message(s, comm_pb2.Request);
    #print print_str
    print "\n"
    print print_str.header.reply_msg
    print print_str.body.get_course.course_name
    print print_str.body.get_course.course_description
    s.close()

#intra-cluster voting request
def request_voting():
    request = comm_pb2.Request()
    request.header.routing_id = request.header.JOBS
    request.header.originator = "server"
    request.header.tag = "Voting"
    request.body.init_voting.host_ip=""
   
    signUpRequest = request.SerializeToString()
    packed_len = struct.pack('>L', len(signUpRequest))
    
    # Sending Sign up Request to the server's public port
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    #host = socket.gethostname() # Testing on own computer
    #port = 5570 # Public Port
    s.connect((host,port))
    #	 Prepending the length field and sending
    s.sendall(packed_len + signUpRequest)
    # Receive at s, a message of type Request and close connection
    print get_message(s, comm_pb2.Request);
    print get_message(s, comm_pb2.Request);
    s.close()

    
#Read a receiving message from a socket. msgtype is a subclass of protobuf Message.       
def get_message(sock, msgtype):
    len_buf = socket_read_n(sock, 4)
    msg_len = struct.unpack('>L', len_buf)[0]
    msg_buf = socket_read_n(sock, msg_len)

    msg = msgtype()
    msg.ParseFromString(msg_buf)
    return msg

""" Read exactly n bytes from the socket.
        Raise RuntimeError if the connection closed before
        n bytes were read.
    """
def socket_read_n(sock, n):
    buf = ''
    while n > 0:
        data = sock.recv(n)
	#print "received"
        if data == '':
            raise RuntimeError('unexpected connection close')
        buf += data
        n -= len(data)
    return buf
