### Compile (In "Application-Server" Directory)
javac -d bin -cp bin -s src src/\*\*/\*.java src/appserver/\*\*/\*.java

Fibonacci.java and FibonacciAux.java must be compiled separately

Make sure to reorganize files into the correct directories in "docRoot"
### Run WebServer (In "docRoot/dynNet" Directory)
java web.SimpleWebServer ../../config/WebServer.properties
### Run Server (In "docRoot" Directory)
java appserver.server.Server ../config/Server.properties
### Run Satellite (In "docRoot" Directory)
java appserver.satellite.Satellite ../config/Satellite.Earth.properties ../config/WebServer.properties ../config/Server.properties
### Run Client (In "docRoot" Directory)
java appserver.client.FibonacciClient ../config/Server.properties