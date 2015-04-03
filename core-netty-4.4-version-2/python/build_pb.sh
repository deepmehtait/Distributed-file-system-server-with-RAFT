#!/bin/bash
#
# creates the python classes for our .proto
#

project_base="/Users/gash/workspace/messaging/core-netty/python"

rm ${project_base}/src/comm_pb2.py

protoc -I=${project_base}/resources --python_out=./src ../resources/comm.proto 
