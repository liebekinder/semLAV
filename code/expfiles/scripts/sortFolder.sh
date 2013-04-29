#!/bin/bash

export Dir=$1

for f in `ls $Dir/*`; do
    sort $f > /tmp/tmp.tmp
    mv /tmp/tmp.tmp $f
done
