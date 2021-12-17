#!/bin/sh

# execute only if 1. parameter exists
if [ "$1" ]; then
  # remove $JAVA_HOME from $PATH
  export PATH=$(echo $PATH | sed -E -e "s;:$JAVA_HOME;;" -e "s;$JAVA_HOME:?;;")  

  # set $JAVA_HOME to 1. parameter
  export JAVA_HOME=$1

  # set $JDK_HOME to $JAVA_HOME
  export JDK_HOME=$JAVA_HOME
fi

# execute only if 2. parameter exists
if [ "$2" ]; then
  # set $PATH_TO_FX to 2. parameter
  export PATH_TO_FX=$2
fi

if [ "$1" ]; then
  # export new $PATH
  export PATH=$JAVA_HOME/bin:$PATH
fi