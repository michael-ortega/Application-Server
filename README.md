#Compile (In "Application-Server" Directory)
javac -d bin -cp bin -s src src/**/*.java src/appserver/**/*.java
___
#Run Satellite (In "bin" Directory)
java appserver.satellite.Satellite ../config/Satellite.Earth.properties ../config/WebServer.properties ../config/Server.properties