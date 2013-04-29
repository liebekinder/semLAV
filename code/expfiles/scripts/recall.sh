#!/bin/bash
# $1 Retrieved
# $2 Relevant Assumed ordered

CARDRELEVANT=`wc -l $2 | sed 's/^[ ^t]*//' | cut -d' ' -f1`

sort $1 > /tmp/tmp.tmp

CARDINTERSECT=`comm -12 /tmp/tmp.tmp $2 | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`  

echo `echo "scale=2; $CARDINTERSECT/$CARDRELEVANT" | bc`


