#!/bin/sh
if [ "$JAVA_HOME" == "" ]
then
		java -jar webinloop.jar $1 $2 $3 $4 $5 $6 $7 $8 $9
else
		$JAVA_HOME/bin/java -jar webinloop.jar $1 $2 $3 $4 $5 $6 $7 $8 $9
fi
