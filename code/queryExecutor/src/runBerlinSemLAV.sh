#!/bin/bash

SemLAVPATH=`pwd | rev | cut -d"/" -f 3- | rev`
QUERIES=`seq $1 $1` 
DATASET=WEB
MEMSIZE=20480m
MEMSIZE2=40960m
TIMEOUT=600000
sorted=true
n=1


cp configD.properties.base configD.properties
sed -i".bkp" "s|SemLAVPATH|$SemLAVPATH|" configD.properties
sed -i".bkp" "s|DATASET|$DATASET|" configD.properties
sed -i".bkp" "s|MEMSIZE|$MEMSIZE|" configD.properties
sed -i".bkp" "s|TIMEOUT|$TIMEOUT|" configD.properties
sed -i".bkp" "s/sorted=[a-z]*/sorted=$sorted/" configD.properties

rm $SemLAVPATH/expfiles/berlinData/$DATASET/viewsN3/*.n3

for i in $QUERIES ;do
    for j in `seq 1 $n` ;do
	sed -i".bkp" "s|exec[0-9]|exec${j}|" configD.properties
	sed -i".bkp" "s/query[0-9][0-9]*/query$i/" configD.properties
	java -XX:MaxHeapSize=${MEMSIZE} -cp ".:../lib2/*" semLAV/evaluateQueryThreaded configD.properties
	java -XX:MaxHeapSize=${MEMSIZE2} processAnswersSemLAV $SemLAVPATH/expfiles/berlinOutput/$DATASET/outputSemLAVquery${i}_${MEMSIZE}_exec${j}/NOTHING
    done
  done
rm configD.properties.bkp
rm configD.properties
