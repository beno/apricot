#!/bin/sh

JAVA=java
MVN=mvn

#JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y"

WD=`pwd`
cd ecr-installer
if [ ! -f target/ecr-installer-*.jar ]; then
  ${MVN} install
fi
cd ${WD}

INSTALLER=ecr-installer/target/ecr-installer-*.jar

rm -rf target
mkdir target
${JAVA} ${JAVA_OPTS} -jar ${INSTALLER} -p core,web,h2 -r ../repository/target/repository  target/ecr-default

