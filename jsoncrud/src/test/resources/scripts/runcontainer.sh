#!/bin/sh

#  This sample program is provided AS IS and may be used, executed, copied and modified
#  without royalty payment by customer
#  (a) for its own instruction and study,
#  (b) in order to develop applications designed to run with an IBM WebSphere product,
#  either for customer's own internal use or for redistribution by customer, as part of such an
#  application, in customer's own products.
#  Licensed Materials - Property of IBM
#  5724-J34 (C) COPYRIGHT International Business Machines Corp. 2009

# Setup our environment variables.
fileDir=`dirname ${0}`
. "$fileDir"/env.sh

# Ensure there is at least 1 arguments.
usage ()
{
    echo Missing argument.
    echo Command line syntax:  runcontainer [Unique Container Name]
}

if [ $# -lt 1 ]
then
    usage
    exit
fi

CONTAINER_NAME="$1"

APPCP="$M2REPO/args4j/args4j/2.0.12/args4j-2.0.12.jar:$M2REPO/com/google/code/gson/gson/1.4/gson-1.4.jar:$M2REPO/rhino/js/1.7R2/js-1.7R2.jar:$M2REPO/com/billynewport/wxsutils/wxsutils/1.8-SNAPSHOT/wxsutils-1.8-SNAPSHOT.jar:$M2REPO/com/devwebsphere/jsoncrud/1.0-SNAPSHOT/jsoncrud-1.0-SNAPSHOT.jar:../../../../target/test-classes"

# Start the container server process
$JAVA_EXE -classpath "$OG_CLASSPATH:$APPCP" "$OBJECTGRID_ENDORSED_DIRS" com.ibm.ws.objectgrid.InitializationService "$CONTAINER_NAME" -catalogServiceEndPoints $CATALOGSERVER_HOST:$CATALOGSERVER_PORT -objectgridFile ../objectgrid.xml -deploymentPolicyFile ../deployment.xml
