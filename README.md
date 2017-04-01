### Compile (In "Application-Server" Directory)
javac -d bin -cp bin -s src src/\*\*/\*.java src/appserver/\*\*/\*.java

Make sure to reorganize files into the correct directories in "docRoot"
### Run WebServer (In "docRoot/dynNet" Directory)
java web.SimpleWebServer ../../config/WebServer.properties
### Run Satellite (In "docRoot" Directory)
java appserver.satellite.Satellite ../config/Satellite.Earth.properties ../config/WebServer.properties ../config/Server.properties
### Run Client (In "docRoot" Directory)
java appserver.client.PlusOneClient ../config/Server.properties