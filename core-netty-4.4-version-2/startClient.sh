#!/bin/bash
#
# This script is used to start the Client from a supplied config file
#


export POKE_CLIENT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "** starting client from ${POKE_CLIENT} **"

echo poke home = $POKE_CLIENT
#exit

#cd ${POKE_CLIENT}

JAVA_MAIN='poke.demo.Route'
#JAVA_ARGS="$1"
echo -e "\n** config: ${JAVA_ARGS} **\n"

# see http://java.sun.com/performance/reference/whitepapers/tuning.html
JAVA_TUNE='-Xms500m -Xmx1000m'


java ${JAVA_TUNE} -cp .:${POKE_CLIENT}/lib/'*':${POKE_CLIENT}/classes ${JAVA_MAIN} 
