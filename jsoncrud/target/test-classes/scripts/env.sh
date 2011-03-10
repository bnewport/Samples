#!/bin/sh

#  This sample program is provided AS IS and may be used, executed, copied and modified
#  without royalty payment by customer
#  (a) for its own instruction and study,
#  (b) in order to develop applications designed to run with an IBM WebSphere product,
#  either for customer's own internal use or for redistribution by customer, as part of such an
#  application, in customer's own products.
#  Licensed Materials - Property of IBM
#  5724-J34 (C) COPYRIGHT International Business Machines Corp. 2009, 2010

# *********************************************************
# ***    As needed, uncomment the following line and    ***
# ***    change JAVA_HOME to match your environment     ***
# *********************************************************
# JAVA_HOME="/opt/java"

# MAVEN Local repository path
M2REPO=/Users/ibm/.m2/repository

# *********************************************************
# *** The following variables do not need to be changed ***
# *********************************************************

# The path to the sample
SAMPLE_DIR=`dirname ${0}`
cd "${SAMPLE_DIR}"
SAMPLE_HOME=`pwd`

# The path to the ObjectGrid runtime root directory.
OBJECTGRID_HOME="/Users/ibm/Documents/Development/og71"

# The path to the endorsed directory where the ORB is located.
OBJECTGRID_ENDORSED_DIRS="-Djava.endorsed.dirs=${OBJECTGRID_HOME}/lib/endorsed"

# The classpath for application client class files.
SAMPLE_CLIENT_CLASSPATH="${SAMPLE_HOME}/client/bin"

# The classpath for application server class files.
SAMPLE_SERVER_CLASSPATH=

# The ObjectGrid runtime class path.
OG_CLASSPATH="${OBJECTGRID_HOME}/properties:${OBJECTGRID_HOME}/lib/objectgrid.jar"

# The location of the catalog server host in which the client will connect.
CATALOGSERVER_HOST="localhost"

# The bootstrap port of the catalog server in which the client will connect.
CATALOGSERVER_PORT="2809"

# The endpoints passed to the catalog server when started.
CATALOG_SERVICE_ENDPOINTS="cs0:localhost:6600:6601"

# If JAVA_HOME is not defined above or external to this script.
if [ -z "$JAVA_HOME" ]
then
    JAVA_HOME="${OBJECTGRID_HOME}/../java"
fi

# The path and filename of the Java executable.
if [ -f ${JAVA_HOME}/jre/bin/java ]; then
    JAVA_EXE="${JAVA_HOME}/jre/bin/java"
else
    JAVA_EXE="${JAVA_HOME}/bin/java"
fi

if [ ! -f ${JAVA_EXE} ]; then
	echo "Change or set the JAVA_HOME environment variable to a valid Java Runtime Environment directory."
	exit
fi

export JAVA_HOME OBJECTGRID_HOME OBJECTGRID_ENDORSED_DIRS SAMPLE_CLIENT_CLASSPATH OG_CLASSPATH CATALOGSERVER_HOST CATALOGSERVER_PORT CATALOG_SERVICE_ENDPOINTS JAVA_EXE M2REPO

