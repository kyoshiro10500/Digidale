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
import base64
from base64 import decodestring
from PIL import Image
import struct
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
    subprocess.call("./scripttest.sh "+file2play, shell=True)

def workon(host,i,boolean,place,nb_screen):
    """Mise des écrans en attente du flux média."""
    tableau_ssh=[0 for i in range(nb_ips)]
    tableau_ssh[i] = paramiko.SSHClient()
    tableau_ssh[i].set_missing_host_key_policy(paramiko.AutoAddPolicy())
    tableau_ssh[i].connect(host, username='pi', password='raspberry')
    if(boolean):
        if(host=="169.254.229.10"):
            tableau_ssh[i].exec_command("pwomxplayer --config="+str(nb_screen)+"digi udp://239.0.1.23:1234?buffer_size=1200000B | ./fbcp")
        else:
            tableau_ssh[i].exec_command("pwomxplayer --config="+str(nb_screen)+"digi udp://239.0.1.23:1234?buffer_size=1200000B")
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
    
def decode_base64(data):
    """Decode base64, padding being optional.

    :param data: Base64 data as an ASCII byte string
    :returns: The decoded byte string.

    """
    missing_padding = len(data) % 4
    if missing_padding != 0:
        data += b'='* (4 - missing_padding)
    return base64.decodestring(data)
def delete_n(t):
    i=t.find('\n')
    while i!=-1:
        t=t[:i]+t[i+1:]
        i=t.find('\n')
    return t
    
def set_config_file(host, row,col,w,h,position):
    connection=paramiko.SSHClient()
    connection.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    connection.connect(host, username='pi', password='raspberry')
    connection.exec_command("python gen_config.py "+str(w)+" "+str(h)+" "+str(col)+" "+str(row)+" "+str(position))
    connection.close

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
file2play=""
print("Socket créé à l'adresse "+HOST+":"+str(PORT)+" .")
width=0
height=0

#################################################
########## Mise en écoute du serveur ############
#################################################

while True:
    conn, addr = mysock.accept() #accept the connection
    print("Connection du mobile "+str(addr)+" acceptée.")
    data = conn.recv(10000000) #how many bytes of data will the server receive
    std_data=standart(data) #suppression des caracteres liés au transfert de données via socket
    print("----------------------------------------ORDRE RECU AVANT TRAITEMENT :" + str(data)+"\n-------------------------------------")
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
            ind=std_data.find("|")
            nb_ligne=std_data[ind+1]
            nb_col=std_data[ind+3]
            nb_screen=int(nb_ligne)*int(nb_col)
            for i in range(nb_ips):
                l=threading.Thread(target=set_config_file,args=(ips[i],int(nb_ligne),int(nb_col),width,height,positions[i]))
                l.start()
            time.sleep(10)
            conn.close()
            threads = []
            for i in range(nb_ips):
                print("Lancement des connections SSH, préparation des écrans.")
                if(positions[i]!='0'):
                    t = threading.Thread(target=workon, args=(ips[i],i,True,positions[i],nb_screen))
                    t.start()
                    threads.append(t)
            print("Ecrans en attente du flux média.")
            time.sleep(5)
          
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
            t = threading.Thread(target=workon, args=(ips[i],i,False,'',nb_screen))
            t.start()
            threads.append(t)
        os.popen("./stop_buffer.sh","w")
        print("Flux arrêté.")           
    elif(std_data[:5]=='image'):
        size = std_data[6:]
        size_int=int(size)
        print("TAILLE RECUE = " + str(size))
        num_image=len(os.listdir('images_uploaded'))
        filename='image'+str(num_image)
        with open('images_uploaded/'+filename+'.jpg', 'wb') as f:
            while size_int > 0:
                data = conn.recv(1024)
                f.write(data)
                size_int -= len(data)
        conn.close()
        #saving videos
        im=Image.open('images_uploaded/image'+str(num_image)+'.jpg')
        width,height=im.size
        if width>height:
            scale='1080:960'
        else:
            scale='960:1080'
        duree='5'
        filename_output='video'+str(num_image)
        os.popen('ffmpeg -loop 1 -i images_uploaded/'+filename+'.jpg -c:v libx264 -t '+duree+' -pix_fmt yuv420p -vf scale='+scale+' images_transformed/'+filename_output+'.mp4','w')
        file2play="images_transformed/"+filename_output+".mp4"
    elif(std_data[:5]=='video'):
        size = std_data[6:]
        size_int=int(size)
        print("TAILLE RECUE = " + str(size))
        num_image=len(os.listdir('videos_uploaded'))
        filename='video'+str(num_image)
        with open('videos_uploaded/'+filename+'.mp4', 'wb') as f:
            while size_int > 0:
                data = conn.recv(1024)
                f.write(data)
                size_int -= len(data)
        conn.close()
        file2play="videos_uploaded/"+filename+".mp4"
    elif(std_data=='recherche'):
        liste_videos=os.listdir('videos_uploaded')
        liste_images=os.listdir('images_uploaded')
        liste_fichiers=''
        nb_fichiers=0
        for filename in liste_videos:
            liste_fichiers+=filename+'\n'
            nb_fichiers+=1
        for filename in liste_images:
            liste_fichiers+=filename+'\n'
            nb_fichiers+=1
        conn.sendall((str(nb_fichiers)+'\n'+liste_fichiers).encode())
        conn.close()
    elif(std_data[:7]=='lecture'):
        filename=std_data[8:]
        if(filename[:5]=='image'):
            file2play='images_transformed/video'+filename[5:-4]+".mp4"
            print("Fichier selectionné à lire : " +file2play)
        else:
            file2play='videos_uploaded/video'+filename[5:]
            print("Fichier selectionné à lire : " +file2play)

        conn.close()


#==============================================================================
#         imagestr=std_data[6:]
# 
#         imgdata = base64.b64decode(imagestr)
#         print(imagestr.encode('utf-8'))
#         filename = 'some_image.jpg'  # I assume you have a way of picking unique filenames
#         with open(filename, 'wb') as f:
#              f.write(imagestr.encode('utf-8'))
#              f.close()
# 
#==============================================================================

#==============================================================================
#         conn.close()
#         mysock.close()
#         break
#         print("Socket closed")
#==============================================================================

