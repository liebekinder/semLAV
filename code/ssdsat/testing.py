#!/usr/bin/env python

from os import listdir
from commands import getoutput
import sys

i = sys.argv[1] #'/home/gmontoya/proyecto/Daniel/ssdsat/examples/experimento1'
archivos = listdir(i)
s = 'python driver.py -t RW -q '
f = open('salida', 'w')

for a in archivos:
   if a.startswith('query-'):
       p = a.partition('-')
       c = p[2]
       b = 'views-'+c
       e = s + i+'/'+a + ' -v ' + i+'/'+b
       salida = getoutput(e)
       f.write(e+'\n')
       f.write(salida+'\n\n')

