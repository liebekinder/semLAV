#!/bin/bash

GUNPATH=`pwd | rev | cut -d"/" -f 4- | rev`
SETUPS='views'
QUERIES=`seq 6 6`

cp configD.properties.base configD.properties
sed -i".bkp" "s|GUNPATH|$GUNPATH|" configD.properties
for setup in $SETUPS ;do
    sed -i".bkp" "s/[0-9][0-9]*views/$setup/" configD.properties
    for i in $QUERIES ;do
        sed -i".bkp" "s/query[0-9][0-9]*/query$i/" configD.properties
        #timeout 30m 
	java -XX:MaxHeapSize=2048m -cp ".:../lib2/*" experimentseswc/evaluateQueryThreaded configD.properties 
	java processAnswersSemLAV $GUNPATH/code/expfiles/berlinOutput/DATASET/$setup/outputRelViewsquery${i}/NOTHING
    done
done
rm configD.properties.bkp
rm configD.properties
#/home/seb/Documents/TER/V3/gun2012/
