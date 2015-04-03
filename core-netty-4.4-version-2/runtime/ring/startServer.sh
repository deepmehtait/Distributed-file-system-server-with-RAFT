#!/bin/bash
#
# This script is used to start the server from a supplied config file
#

export POKE_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )/../.." && pwd )"
echo "** starting server from ${POKE_HOME} **"

echo poke home = $POKE_HOME
#exit

#cd ${POKE_HOME}

JAVA_MAIN='poke.server.Server'
JAVA_ARGS="$1"
echo -e "\n** config: ${JAVA_ARGS} **\n"

# see http://java.sun.com/performance/reference/whitepapers/tuning.html
JAVA_TUNE='-Xms500m -Xmx1000m'


java ${JAVA_TUNE} -cp .:${POKE_HOME}/lib/'*':${POKE_HOME}/classes ${JAVA_MAIN} ${JAVA_ARGS} 
