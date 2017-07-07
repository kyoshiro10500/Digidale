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
import paramiko
from pykeyboard import PyKeyboard

#######################################################
############## Définition des fonctions  ##############
#######################################################

def standart(data):
    """Garantit la conformité des messages reçus aux messages attendus."""
    str_data=str(data)
    debut=str_data.find('begin')
    fin=str_data.find('end')
    return str_data[debut+6:fin-1]

def find_local_ip():
    """Crée un socket pour obtenir l'IP locale puis le supprime."""
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect(('8.8.8.8', 1))  # connect() for UDP doesn't send packets
    local_ip_address = s.getsockname()[0]
    s.close()
    return local_ip_address
def get_ips():
    """A partir du fichier scan.txt issu d'un scan réseau, renvoie ["ip1", "ip2", "ip3", ...]."""
    file=open("scan.txt","r")
    addresses=file.readlines()[2:-3]
    file.close()
    nb_ips=len(addresses)
    for i in range(nb_ips):
        j=addresses[i].find('\t')
        addresses[i]=addresses[i][:j]
    return addresses

def set_ips():
    """Renvoie la liste des IPs sous forme d'une chaîne de caractère prête à l'envoi via socket."""
    str_ips=""
    for ip in ips:
        str_ips+=ip+"\n"
    return str_ips

def send_media():
    """Envoi du flux média par le serveur."""
    subprocess.call("avconv -re -i movie.mp4 -vcodec copy -f avi -an udp://239.0.1.23:1234", shell=True)

def workon(host,i,boolean,place):
    """Mise des écrans en attente du flux média."""
    tableau_ssh=[0 for i in range(nb_ips)]
    tableau_ssh[i] = paramiko.SSHClient()
    tableau_ssh[i].set_missing_host_key_policy(paramiko.AutoAddPolicy())
    tableau_ssh[i].connect(host, username='pi', password='raspberry')
    if(boolean):
        if(host=="169.254.229.10"):
            tableau_ssh[i].exec_command("pwomxplayer --tile-code=4"+place+" udp://239.0.1.23:1234?buffer_size=1200000B | ./fbcp")
        else:
            tableau_ssh[i].exec_command("pwomxplayer --tile-code=4"+place+" udp://239.0.1.23:1234?buffer_size=1200000B")
    else:
         tableau_ssh[i].exec_command("./script.sh")
         
def print_num(host,i):
    """Affiche sur un écran son numéro pendant 5 secondes."""
    connection=paramiko.SSHClient()
    connection.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    connection.connect(host, username='pi', password='raspberry')
    connection.exec_command("sudo fbi -T 1 "+str(i+1)+".jpg")
    print("Affichage du numéro "+str(i+1)+" sur l'écran d'IP : "+host+" .")
    time.sleep(5)
    connection.exec_command("sudo ./close_fbi.sh")
    print("Numéro effacé.")
    connection.close
    
def get_setting(std_data):
    """A partir des data standardisées reçues, retourne [place ip1, place ip2, ...] ."""
    setting=[]
    i=std_data.find('/')
    while i!=-1:
        setting+=[std_data[i-1]]
        std_data=std_data[i+1:]
        i=std_data.find('/')
    return setting
############################################################
################# Définition des constantes ################
############################################################
print("Déclaration des constantes.")
k = PyKeyboard()
print("Clavier lancé.")
os.popen('sudo arp-scan 169.254.229.20/24 > scan.txt','w').write('digidalepower')
print("Scan initial du réseau terminé.")
ips=get_ips()
nb_ips=len(ips)
HOST = find_local_ip() #local host
PORT = 7000 #open port 7000 for connection
mysock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
mysock.bind((HOST, PORT)) #how many connections can it receive at one time
mysock.listen(1)
print("Socket créé à l'adresse "+HOST+":"+str(PORT)+" .")


#################################################
########## Mise en écoute du serveur ############
#################################################

while True:
    conn, addr = mysock.accept() #accept the connection
    print("Connection du mobile "+str(addr)+" acceptée.")
    data = conn.recv(1024) #how many bytes of data will the server receive
    std_data=standart(data) #suppression des caracteres liés au transfert de données via socket
    print("Ordre "+ std_data+" reçu.")
    
    #Si l'ordre reçu est un scan du réseau et l'affichage des écrans
    if(std_data=="ping"):
        print("Début du scan du réseau.")
        os.popen('sudo arp-scan 169.254.229.20/24 > scan.txt','w').write('digidalepower')
        ips=get_ips()
        nb_ips=len(ips)
        print("IPs détectées :")
        for i in range(len(ips)):
            print("----------------" + ips[i])
        conn.sendall((str(nb_ips)+"\n"+set_ips()).encode())
        print("IPs envoyées au mobile connecté.")
        conn.close()
        
    #Si l'ordre reçu est l'affichage du numéro d'un écran
    elif(std_data[:-1]=="idscreen"):
        conn.close()
        num_ip=std_data[-1]
        ip=ips[int(num_ip)]
        t = threading.Thread(target=print_num, args=(ips[int(num_ip)],int(num_ip),))
        t.start()
        
    #Si l'ordre reçu est de lancer le flux média
    elif(std_data[:6]=='launch'):
        if(len(std_data)!=6):
            positions=get_setting(std_data[7:])
            conn.close()
            threads = []
            for i in range(nb_ips):
                print("Lancement des connections SSH, préparation des écrans.")
                if(positions[i]!='0'):
                    t = threading.Thread(target=workon, args=(ips[i],i,True,positions[i]))
                    t.start()
                    threads.append(t)
            print("Ecrans en attente du flux média.")
            time.sleep(3)
          
            t = threading.Thread(target=send_media)
            t.start()
            print("Flux média en cours d'envoi.")    
        else:
            print("Erreur d'envoi du flux média : pas de positions reçues.")
        
    #Si l'ordre reçu est l'arrêt de la lecture du flux média
    elif(std_data=='stop'):
        conn.close()
        threads = []
        for i in range(nb_ips):
            t = threading.Thread(target=workon, args=(ips[i],i,False,''))
            t.start()
            threads.append(t)
        k.press_key('q')
        k.release_key('q')
        print("Flux arrêté.")
#==============================================================================
#         conn.close()
#         mysock.close()
#         break
#         print("Socket closed")
#==============================================================================

