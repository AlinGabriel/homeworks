#!/bin/bash

if [[ $1 == 1 ]]; then
	/usr/lib/jvm/jre/bin/java -cp build/classes/ Client alin IT localhost 7001
fi

if [[ $1 == 2 ]]; then
	/usr/lib/jvm/jre/bin/java -cp build/classes/ Client andrei ACCOUNTING localhost 7001
fi