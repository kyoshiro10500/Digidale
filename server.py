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
import spur
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


while True:
    conn, addr = mysock.accept() #accept the connection
    print("Connected by: " + str(addr)) #print the address of the person connected
    data = conn.recv(1024) #how many bytes of data will the server receive
    std_data=standart(data)
    print("Received: "+ std_data)
    ssh_fermeture=[]
    ssh_lecture=[]
    if(std_data=="ping"):
        print("debut du scan :")
        print("IPs détectées :")
        for i in range(len(ips)):
            print(ips[i])
        conn.close()
    elif(std_data=='launch'):
        # Récupération des IP ['192.X.X.X', 'X.X.X.X']
        #--------------------------------------------------------
        #---------- Création des SSH ----------------------------
        #--------------------------------------------------------
        for i in range(nb_ips):
            ssh_fermeture+=[spur.SshShell(hostname=ips[i], username="pi", password="raspberry")]
            ssh_lecture+=[spur.SshShell(hostname=ips[i], username="pi", password="raspberry")]
            print(ssh_fermeture[i])
            if(ips[i]=='169.254.229.10'):
                with ssh_lecture[i]:
                    (ssh_lecture[i]).run(["pwomxplayer","--tile-code=42","udp://239.0.1.23:1234?buffer_size=1200000B"])
            else:
                with ssh_lecture[i]:
                    (ssh_lecture[i]).run(["pwomxplayer","--tile-code=41","udp://239.0.1.23:1234?buffer_size=1200000B"])        
        
        subprocess.call("avconv -re -i movie.mp4 -vcodec copy -f avi -an udp://239.0.1.23:1234", shell=True)
#==============================================================================
#         subprocess.call("chmod u+x ../../Digidale/Application/essai.sh", shell=True)
#         subprocess.call("../../Digidale/Application/essai.sh", shell=True)
#==============================================================================
    elif(std_data=='stop'):
        for i in range(nb_ips):
            with ssh_fermeture[i]:
                ssh_fermeture[i].run(["./script.sh"])
        conn.close()
        mysock.close()
        break
print("Socket closed")
