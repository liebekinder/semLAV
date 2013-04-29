#!/bin/bash

SETUPS="75views"
QUERIES=`seq 2 18`
MAXRWNUMBER=10000
GUNPATH=`pwd | rev | cut -d"/" -f 4- | rev`
PH=$GUNPATH/code/expfiles/berlinOutput

sed -i".bkp" "s|GUNPATH|$GUNPATH|" $GUNPATH/code/ssdsat/options.py
sed -i".bkp" "s/max_models = [0-9][0-9]*/max_models = $MAXRWNUMBER/" $GUNPATH/code/ssdsat/options.py
cp configC.properties.base configC.properties
sed -i".bkp" "s|GUNPATH|$GUNPATH|" configC.properties
sed -i".bkp" "s/maxnumberrewritings=[0-9][0-9]*/maxnumberrewritings=$MAXRWNUMBER/" configC.properties

sed -i".bkp" "s/jointype=[A-Z]*/jointype=GUN/" configC.properties
sed -i".bkp" "s/experiments=[A-Z,]*/experiments=FULL/" configC.properties

for setup in $SETUPS ;do
    sed -i".bkp" "s/[0-9][0-9]*views/$setup/" configC.properties
    nv=${setup:0:$((${#setup}-5))}
    sed -i".bkp" "s/numberviews=[0-9][0-9]*/numberviews=$nv/" configC.properties
    for i in $QUERIES ;do
        sed -i".bkp" "s/query[0-9][0-9]*/query$i/" configC.properties
        timeout 35m java -XX:MaxHeapSize=2048m -cp ".:../lib2/*" experimentseswc/calculateRelevantViews > $PH/$setup/relevantViews_query${i}_${setup}_2
        #java processFile $PH/$setup/outputquery${i}GUN/FULL/modelSize $PH/$setup/outputquery${i}GUN/FULL/modelSizes
        #rm $PH/$setup/outputquery${i}GUN/FULL/modelSize
        #java processAnswers $PH/${setup}/ outputquery${i}GUN
        #java deleteAnswers $PH/${setup}/ outputquery${i}GUN
    done
done

rm configC.properties.bkp
rm configC.properties
mv $GUNPATH/code/ssdsat/options.py.bkp $GUNPATH/code/ssdsat/options.py
