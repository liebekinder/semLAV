import sys
import string

mainsGT = sys.argv[1]
dessertsGT = sys.argv[2]
resultsGT = sys.argv[3]

m = open(mainsGT, 'r')
r = open(resultsGT, 'w')

thereAreMains = True

while thereAreMains:
    l = m.readline()
    l = string.rstrip(l, '\n')
    thereAreMains = not l == ''
    if not thereAreMains:
        break
    d = open(dessertsGT, 'r')
    thereAreDesserts = True
    while thereAreDesserts:
        k = d.readline()
        k = string.rstrip(k, '\n')
        thereAreDesserts = not k == ''
        if not thereAreDesserts:
            break
        nl = '['+l+', '+k+']'
        r.write(nl+'\n')
    d.close()
m.close()
r.close()
