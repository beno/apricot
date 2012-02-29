1. NUXEO-COMMON:
The following files must use the license as set in apricot sources:
- Path
- ListenerList
- URLStreamHandlerFactoryInstaller

2. runtime-jtajca: contains a hardcoded package in NuxeoContainer: 

System.setProperty(Context.URL_PKG_PREFIXES, "org.eclipse.ecr.runtime.jtajca");

3. core-query: do not forget to sync the flex directory

4. core-convert: OSGI-INF/convert-service-base-contrib.xml & FullTextConverter in apricot but not in nuxeo

5. 


TODO test DateParser from schema which changed


nuxeovcs/h2.sql.txt -> nuxeovcs/h2.sql.txt"resources/nuxeovcs/h2.sql.txt"