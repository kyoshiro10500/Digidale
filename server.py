#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Jun 30 18:26:12 2017

@author: jonathan
"""
from socket import *
import socket
import subprocess
import time
import os
import threading
import spur
import paramiko
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
def get_ips():
    file=open("scan.txt","r")
    addresses=file.readlines()[2:-3]
    file.close()
    nb_ips=len(addresses)
    for i in range(nb_ips):
        j=addresses[i].find('\t')
        addresses[i]=addresses[i][:j]
    return addresses



        
os.popen('sudo arp-scan 169.254.229.20/24 > scan.txt','w').write('digidalepower')
ips=get_ips()
nb_ips=len(ips)
HOST = find_local_ip() #local host
PORT = 7000 #open port 7000 for connection
mysock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
mysock.bind((HOST, PORT)) #how many connections can it receive at one time
mysock.listen(1)
print("Socket created on "+HOST+":"+str(PORT))
    
def run_ssh(i):
    connection = spur.SshShell(hostname=ips[i], username="pi", password="raspberry")
    if(ips[i]=="169.254.229.10"):
        connection.run(["pwomxplayer","--tile-code=42","udp://239.0.1.23:1234?buffer_size=1200000B","|","./fbcp"])
    else:
        connection.run(["pwomxplayer","--tile-code=41","udp://239.0.1.23:1234?buffer_size=1200000B"])

def send_media():
    subprocess.call("avconv -re -i movie.mp4 -vcodec copy -f avi -an udp://239.0.1.23:1234", shell=True)
    
outlock = threading.Lock()
cmd="pwomxplayer --tile-code=41 udp://239.0.1.23:1234?buffer_size=1200000B"

tableau_ssh=[0 for i in range(nb_ips)]
def workon(host,tableau_ssh,i,boolean):
    tableau_ssh[i] = paramiko.SSHClient()
    tableau_ssh[i].set_missing_host_key_policy(paramiko.AutoAddPolicy())
    tableau_ssh[i].connect(host, username='pi', password='raspberry')
    if(boolean):
        if(host=="169.254.229.10"):
            stdin, stdout, stderr = tableau_ssh[i].exec_command("pwomxplayer --tile-code=42 udp://239.0.1.23:1234?buffer_size=1200000B | ./fbcp")
        else:
            stdin, stdout, stderr = tableau_ssh[i].exec_command("pwomxplayer --tile-code=41 udp://239.0.1.23:1234?buffer_size=1200000B")
    else:
         stdin, stdout, stderr = tableau_ssh[i].exec_command("./script.sh")
    stdin.write('xy\n')
    stdin.flush()

    with outlock:
        print(stdout.readlines())
while True:
    conn, addr = mysock.accept() #accept the connection
    print("Connected by: " + str(addr)) #print the address of the person connected
    data = conn.recv(1024) #how many bytes of data will the server receive
    std_data=standart(data)
    print("Received: "+ std_data)
    if(std_data=="ping"):
        os.popen('sudo arp-scan 169.254.229.20/24 > scan.txt','w').write('digidalepower')
        ips=get_ips()
        nb_ips=len(ips)
        print("debut du scan :")
        print("IPs détectées :")
        for i in range(len(ips)):
            print(ips[i])
        conn.sendall((str(nb_ips)+"\n").encode())
        print("message envoyé")
        conn.close()
    elif(std_data=='launch'):
        # Récupération des IP ['192.X.X.X', 'X.X.X.X']
        #--------------------------------------------------------
        #---------- Création des SSH ----------------------------
        #--------------------------------------------------------
        threads = []
        for i in range(nb_ips):
            t = threading.Thread(target=workon, args=(ips[i],tableau_ssh,i,True,))
            t.start()
            threads.append(t)
#==============================================================================
#         for t in threads:
#             t.join()
# 
#==============================================================================
        time.sleep(3)
      
        t = threading.Thread(target=send_media)
        t.start()
        
#==============================================================================
#         subprocess.call("chmod u+x ../../Digidale/Application/essai.sh", shell=True)
#         subprocess.call("../../Digidale/Application/essai.sh", shell=True)
#==============================================================================
    elif(std_data=='stop'):
        threads = []
        for i in range(nb_ips):
            t = threading.Thread(target=workon, args=(ips[i],tableau_ssh,i,False,))
            t.start()
            threads.append(t)
        k.press_key('q')
        k.release_key('q')
#==============================================================================
#         conn.close()
#         mysock.close()
#         break
#==============================================================================
print("Socket closed")
