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

CATALOG_SERVER_NAME="cs0"

# Start the catalog server process
$JAVA_EXE -classpath "$OG_CLASSPATH" "$OBJECTGRID_ENDORSED_DIRS" com.ibm.ws.objectgrid.InitializationService "$CATALOG_SERVER_NAME" -catalogServiceEndPoints $CATALOG_SERVICE_ENDPOINTS

