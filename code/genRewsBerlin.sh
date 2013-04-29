#!/bin/bash

GUNPATH=`pwd | rev | cut -d"/" -f 2- | rev`
SETUPS="300views"
QUERY=`seq 1 18`
TIMEOUT=2
MAXRWNUMBER=500
DATASET=FiveMillions

sed -i".bkp" "s|GUNPATH|$GUNPATH|" $GUNPATH/code/ssdsat/options.py
sed -i".bkp" "s/max_models = [0-9][0-9]*/max_models = $MAXRWNUMBER/" $GUNPATH/code/ssdsat/options.py
for setup in $SETUPS; do
	echo "Processing setup $setup"
	for query in $QUERY; do
		echo "Query $query"
		timeout ${TIMEOUT}m ./ssdsat/driver.py -t RW -v ./expfiles/berlinData/$DATASET/$setup/mappingsBerlin -q ./expfiles/berlinData/conjunctiveQueries/query$query > ./expfiles/berlinData/$DATASET/$setup/${MAXRWNUMBER}rewritings_query${query}  2> /dev/null
	done
done

mv $GUNPATH/code/ssdsat/options.py.bkp $GUNPATH/code/ssdsat/options.py
