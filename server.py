#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Jun 30 18:26:12 2017

@author: jonathan
"""
from socket import *
import socket
import subprocess
##########################
## Test press bar ########
from pykeyboard import PyKeyboard
k = PyKeyboard()
############################
def standart(data):
    str_data=str(data)
    debut=str_data.find('begin')
    fin=str_data.find('end')
    return str_data[debut+6:fin-1]

def find_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect(('8.8.8.8', 1))  # connect() for UDP doesn't send packets
    local_ip_address = s.getsockname()[0]
    s.close()
    return local_ip_address

HOST = find_local_ip() #local host
PORT = 7000 #open port 7000 for connection
mysock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
mysock.bind((HOST, PORT)) #how many connections can it receive at one time
mysock.listen(1)
print("Socket created on "+HOST+":"+str(PORT))
while True:
    conn, addr = mysock.accept() #accept the connection
    print("Connected by: " + str(addr)) #print the address of the person connected
    data = conn.recv(1024) #how many bytes of data will the server receive
    std_data=standart(data)
    print("Received: "+ std_data)
    if(std_data=="ping"):
        print("Ping recu")
        conn.close()
    elif(std_data=='launch'):
        subprocess.call("chmod u+x ../../Digidale/Application/essai.sh", shell=True)
        subprocess.call("../../Digidale/Application/essai.sh", shell=True)
    elif(std_data=='stop'):
        conn.close()
        mysock.close()
        break
print("Socket closed")
