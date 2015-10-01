#!/bin/bash

JAVA_HOME=$HOME/local/java1.8.0_45
PID_FILE=target/universal/stage/RUNNING_PID
SECRET_FILE=secret

if ! [ -f ${SECRET_FILE} ]; then
    echo "secret file is not found. abort to boot server"
    exit 1
fi

if [ -f ${PID_FILE} ]; then
    kill $(cat ${PID_FILE})
fi

activator clean stage
JAVA_HOME=$JAVA_HOME exec target/universal/stage/bin/tefnut -Dconfig.resource=prod.conf -Dapplication.secret=$(cat ${SECRET_FILE})
