for k in `seq 0 99`; do
    timeout 24h ./runQuery.sh ${k}
done
