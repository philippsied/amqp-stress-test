#!/bin/bash

CLIENTDIR=rabbitmq-java-client-bin-3.5.1
TESTENV=environment
TEMPLATE=publish-consume-spec.template
SPEC=publish-consume-spec.js
TARGET=publish-consume-result.json

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <URI without amqp://>"
    exit 0
fi

cd $TESTENV
bash setupSpecs.sh $TEMPLATE $SPEC $1
cd ..
cd $CLIENTDIR
sh runjava.sh com.rabbitmq.examples.PerfTestMulti ../$TESTENV/$SPEC ../$TESTENV/$TARGET
cd ..
rm $TESTENV/$SPEC

