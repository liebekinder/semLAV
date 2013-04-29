#!/bin/bash
# This script is intended to be runned from the mediator
# Put in the expfiles folder and configure accordingly

wget $1 -O $2.n3
sed -i -f cleanwget.sed $2.n3