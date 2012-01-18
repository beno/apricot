#!/bin/bash

JAVA=java
MVN=mvn

WD=`pwd`
cd ecr-installer
if [ ! -f target/ecr-installer-*.jar ]; then
  ${MVN} install
fi
cd ${WD}

INSTALLER=ecr-installer/target/ecr-installer-*.jar

${JAVA} -cp ${INSTALLER} org.eclipse.ecr.build.felix.Patch "$1"

echo "To deploy in felix remove the org.eclipse.equinox.launcher and org.eclipse.osgi bundles from plugins"
echo "rm $1/org.eclipse.equinox.launcher_*.jar"
echo "rm $1/org.eclipse.osgi_*.jar"
echo ""
echo "Then you can start Apricot from felix by using the fileinstall plugin to deploy the bundles in plugins/ directory"

