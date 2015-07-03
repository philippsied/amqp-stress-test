#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <targetName> <URI without amqp://>"
    exit 0
fi

CLIENTDIR=rabbitmq-java-client-bin-3.5.1
TESTENV=environment
TEMPLATE=publish-consume-spec.template
SPEC=publish-consume-spec.js
TARGET=$1
URI=$2

bash setupSpecs.sh $TEMPLATE $SPEC $URI
cd $CLIENTDIR
sh runjava.sh com.rabbitmq.examples.PerfTestMulti ../$SPEC ../$TARGET
cd ..
rm $SPEC
