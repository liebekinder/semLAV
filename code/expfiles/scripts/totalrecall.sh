#!/bin/bash
#$1 Directory
#$2 Relevant file mains
#$3 Relevant file desserts

rm $1/Recall*

for strat in MINNOTHING MINEQUIVALENCECLASSSORT MAXEQUIVALENCECLASSSORT MAXSEMANTICFILTER MINSEMANTICFILTER
do
	for i in `seq 1 1 35`
	do
		echo  $i `bash recall.sh $1/mains_$strat'_'$i $2` >> $1/RecallMains$strat
		echo  $i `bash recall.sh $1/desserts_$strat'_'$i $3` >> $1/RecallDesserts$strat
	done
done

