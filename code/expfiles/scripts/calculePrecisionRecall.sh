#!/bin/bash

export Dir=$1
export GroundTruth=$2

rm $Dir/Precision
rm $Dir/Recall
rm $Dir/PrecisionError
rm $Dir/RecallError

for f in `ls $Dir/*`; do
    echo $f `bash ./precision.sh $f $GroundTruth 2>> $Dir/PrecisionError` >> $Dir/Precision 
    echo $f `bash ./recall.sh    $f $GroundTruth 2>> $Dir/RecallError` >> $Dir/Recall 
done
