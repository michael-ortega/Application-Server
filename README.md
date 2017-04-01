# Compile (In "Application-Server" Directory)
javac -d bin -cp bin -s src src/\*\*/\*.java src/appserver/\*\*/\*.java
# Run Satellite (In "bin" Directory)
java appserver.satellite.Satellite ../config/Satellite.Earth.properties ../config/WebServer.properties ../config/Server.properties
# Run WebServer (In "bin" Directory)
java web.SimpleWebServer ../config/WebServer.properties
# Run Client (In "bin" Directory)
java appserver.client.PlusOneClient ../config/Server.properties