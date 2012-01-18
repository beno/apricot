Run ./build.sh to build a product based on equinox.
To run on felix uyou need to patch some external third parties that contains bad manifests (not supported by felix)
Run: ./felix-patch.sh target/ecr-default/plugins to launch the patch.
It will patch, org.apache.lucene.core, org.h2 and javax.transaction manifests.

To deploy in felix add the fileinstall plugin - remove the org.eclipse.osgi_* and org.eclipse.equinox.launcher_* from
plugins and configure the fileinstall plugin to load the bundles from plugins/ directory.

