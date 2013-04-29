#!/bin/bash

export File=$1
Number=`wc -l $File | sed 's/^[ ^t]*//' | cut -d' ' -f1`
echo $Number
