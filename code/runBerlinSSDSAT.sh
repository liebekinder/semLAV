QUERIES=`seq 1 18`
TIMEOUT="10m"
CODE=`pwd | rev | cut -d"/" -f 1- | rev`
SETUPS=510views
DATASET=TenMillions

for i in $QUERIES; do  
    for t in $TIMEOUT; do
        x=`echo "query${i}" >> executionTimeSSDSAT`
        x=`echo "timeout=${t}" >> executionTimeSSDSAT`
        time timeout ${t} ./ssdsat/driver.py -t RW -v $CODE/expfiles/berlinData/$DATASET/$SETUP/mappingsBerlin -q $CODE/expfiles/berlinData/conjunctiveQueries/query${i} > rewritingsQuery${i}_${t}_SSDSAT 2>> /dev/null
    done
done
