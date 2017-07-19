#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Jul 19 11:28:58 2017

@author: jonathan
"""

#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Jun 30 18:26:12 2017

@author: jonathan
"""
from socket import *
import socket
import os

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



############################################################
################# Définition des constantes ################
############################################################
print("Déclaration des constantes.")
k = PyKeyboard()
print("Clavier lancé.")
HOST = find_local_ip() #local host
PORT = 7000 #open port 7000 for connection
mysock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
mysock.bind((HOST, PORT)) #how many connections can it receive at one time
mysock.listen(1)
file2play=""
print("Socket créé à l'adresse "+HOST+":"+str(PORT)+" .")
width=0
height=0
isImage=True
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
    
    #Si l'ordre reçu est de lancer le flux média
    if(std_data[:6]=='launch'):
        if(isImage):
            os.popen("fbi images_uploaded/"+file2play,"w")
        else:
            os.popen("pwomxplayer videos_uploaded/"+file2play,"w")
        
    #Si l'ordre reçu est l'arrêt de la lecture du flux média
    elif(std_data=='stop'):
        conn.close()
        if(isImage):
            os.popen("./close_fbi.sh","w")
        else:
            os.popen("./script.sh","w")
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
        file2play="images_uploaded/"+filename+".jpg"
        
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
        liste_images.sort()
        liste_videos.sort()
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
            file2play='images_uploaded/image'+filename[5:-4]+".jpg"
            isImage=True
            print("Fichier selectionné à lire : " +file2play)
        else:
            file2play='videos_uploaded/video'+filename[5:]
            isImage=False
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



