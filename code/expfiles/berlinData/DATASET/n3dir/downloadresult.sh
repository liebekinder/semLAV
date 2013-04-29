#!/bin/bash
# This script is intended to be runned from the mediator
# Put in the expfiles folder and configure accordingly

#$1 correspond à la chaine associée au nom de la vue (viewX_F)
#cette chaine associé est présente dans catolog (expfiles)
#$2 correspond au nom de la vue

# what should i do if i get a view name?
# generate the view?
# what about the different dataset? Sould each view be specific to each one?


#question fro monday
#what is the factor about?
#are we supposed to follow the notation viewI_J?
#what is the 

echo "java -jar $1 $2 $3 $4"
java -jar $1 $2 $3 $4