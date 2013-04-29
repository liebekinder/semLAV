#!/bin/bash

export Dir=$1
export GroundTruth=$2
export File=$3
Precision=`bash ./precision.sh $File $GroundTruth 2>> $Dir/PrecisionError`
Recall=`bash ./recall.sh    $File $GroundTruth 2>> $Dir/RecallError`
echo $File $Precision >> $Dir/Precision 
echo $File $Recall >> $Dir/Recall
echo $Recall

