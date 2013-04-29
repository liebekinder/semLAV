#!/bin/bash
# $1 Retrieved
# $2 Relevant Assumed ordered

CARDRETRIEVED=`wc -l $1 | sed 's/^[ ^t]*//' | cut -d' ' -f1`

if [ $CARDRETRIEVED -eq 0 ]
then
	echo 1.00
	exit 1
fi

sort $1 > /tmp/tmp.tmp

CARDINTERSECT=`comm -12 /tmp/tmp.tmp $2 | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`  


echo `echo "scale=2; $CARDINTERSECT/$CARDRETRIEVED" | bc`



