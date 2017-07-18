#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Mon Jul 17 12:14:27 2017

@author: jonathan
"""
import sys

w=int(sys.argv[1])
h=int(sys.argv[2])
nb_col=int(sys.argv[3])
nb_lignes=int(sys.argv[4])
pos=sys.argv[5]

f=open('.piwall', 'w')
f.write("# wall definition for "+str(nb_lignes)+"x"+str(nb_col)+" screens with bezel compensation\n")
nb_screen=nb_col*nb_lignes
f.write("["+str(nb_screen)+"digi_wall]\nwidth="+str(w)+"\nheight="+str(h)+"\nx=0\ny=0")
f.write("\n\n\n")
f.write("# corresponding tile definitions\n")
incr_ho=w//nb_col
incr_ve=h//nb_lignes
pos_ve=0
incr_nb_screen=1
for i in range(nb_lignes):
    pos_ho=0
    for j in range(nb_col):
        f.write("["+str(nb_screen)+"digi_"+str(incr_nb_screen)+"]\n")
        f.write("wall="+str(nb_screen)+"digi_wall\n")
        f.write("width="+str(incr_ho)+"\n")
        f.write("height="+str(incr_ve)+"\n")
        f.write("x="+str(pos_ho)+"\n")
        f.write("y="+str(pos_ve)+"\n")
        pos_ho=incr_ho*j
        f.write("\n\n")
        incr_nb_screen+=1
        pos_ho+=incr_ho
    pos_ve+=incr_ve
f.write("# config\n["+str(nb_screen)+"digi]\n")
for i in range(nb_screen):
    f.write("pi"+str(i+1)+"="+str(nb_screen)+"digi_"+str(i+1)+"\n")
f.close()

g=open('.pitile', 'w')
g.write('[tile]\n')
g.write('id=pi')
g.write(pos)
g.close()
