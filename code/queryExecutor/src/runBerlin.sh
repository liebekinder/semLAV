#!/bin/bash

STRATS="JENA GUN"
SETUPS="300views"
QUERIES=`seq 8 12`
DATASET=FiveThousand

GUNPATH=`pwd | rev | cut -d"/" -f 4- | rev`
PH=$GUNPATH/code/expfiles/berlinOutput

cp configC.properties.base configC.properties
sed -i".bkp" "s|GUNPATH|$GUNPATH|" configC.properties
sed -i".bkp" "s|DATASET|$DATASET|" configC.properties

for str in $STRATS; do
    sed -i".bkp" "s/jointype=[A-Z]*/jointype=$str/" configC.properties
    for setup in $SETUPS ;do
        sed -i".bkp" "s/[0-9][0-9]*views/$setup/" configC.properties
        for i in $QUERIES ;do
            sed -i".bkp" "s/query[0-9][0-9]*/query$i/" configC.properties
            java -XX:MaxHeapSize=2048m -cp ".:../lib2/*" experimentseswc/Main
            exp=`grep experiments= configC.properties | cut -d"=" -f 2`
            old_ifs=$IFS
            IFS=','
            for e in $exp; do
                sort=`grep sorttypes= configC.properties | cut -d"=" -f 2`
                for s in $sort; do
                    java processFile $PH/$setup/outputquery${i}${str}/${s}_${e}/modelSize $PH/$setup/outputquery${i}${str}/${s}_${e}/modelSizes
                    rm $PH/$setup/outputquery${i}${str}/${s}_${e}/modelSize
                done
            done
            IFS=$old_ifs
            #java processAnswers $PH/${setup}/ outputquery${i}${str}
            #java deleteAnswers $PH/${setup}/ outputquery${i}${str} 
        done
    done
done

rm configC.properties.bkp
rm configC.properties

