WXS_DIR=
CRUDJAR=

java -Djava.endorsed.dirs=$WXS_DIR/lib/endorsed -Dcom.sun.management.jmxremote -cp $WXS_DIR/lib/ogclient.jar:$CRUDJAR 
