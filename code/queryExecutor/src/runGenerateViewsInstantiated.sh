#!/bin/bash

GUNPATH=`pwd | rev | cut -d"/" -f 4- | rev`
FACTOR="20"
DATASET=FiveThousand
DATASETFILE=$GUNPATH/code/expfiles/berlinData/datasets/dataset${DATASET}.nt

for f in $FACTOR ;do
    SETUP=$(($f*15))views
    cp configData.properties.base configData.properties
    sed -i".bkp" "s|GUNPATH|$GUNPATH|" configData.properties
    sed -i".bkp" "s|DATASET|$DATASET|" configData.properties
    sed -i".bkp" "s/[0-9][0-9]*views/$SETUP/" configData.properties
    sed -i".bkp" "s/factor=[0-9][0-9]*/factor=$f/" configData.properties
    sed -i".bkp" "s|dataSetFile=[^\n]*|dataSetFile=$DATASETFILE|" configData.properties

    java -XX:MaxHeapSize=2560m -cp ".:../lib2/*" experimentseswc/generateViewsInstantiated $GUNPATH/code/expfiles/berlinData/$DATASET/$SETUP/usedViewInstantiation
done

rm configData.properties
rm configData.properties.bkp
