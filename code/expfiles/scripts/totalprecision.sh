#!/bin/bash
#$1 Directory
#$2 Relevant file mains
#$3 Relevant file desserts

for strat in MINNOTHING MINEQUIVALENCECLASSSORT MAXEQUIVALENCECLASSSORT MAXSEMANTICFILTER MINSEMANTICFILTER
do
	for i in `seq 1 1 35`
	do
		echo  $i `bash precision.sh $1/mains_$strat'_'$i $2` >> $1/PrecisionMains$strat
		echo  $i `bash precision.sh $1/desserts_$strat'_'$i $3` >> $1/PrecisionDesserts$strat
	done
done

