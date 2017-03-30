package appserver.satellite;

import appserver.job.Job;
import appserver.comm.ConnectivityInfo;
import appserver.job.UnknownToolException;
import appserver.comm.Message;
import static appserver.comm.MessageTypes.JOB_REQUEST;
import static appserver.comm.MessageTypes.REGISTER_SATELLITE;
import appserver.job.Tool;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.PropertyHandler;
import java.lang.String;
import java.lang.Integer;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Class [Satellite] Instances of this class represent computing nodes that execute jobs by
 * calling the callback method of tool implementation, loading the tools code dynamically over a network
 * or locally, if a tool got executed before.
 *
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class Satellite extends Thread {

    private ConnectivityInfo satelliteInfo = new ConnectivityInfo();
    private ConnectivityInfo serverInfo = new ConnectivityInfo();
    private HTTPClassLoader classLoader = null;
    private Hashtable toolsCache = null;

    public Satellite(String satellitePropertiesFile, String classLoaderPropertiesFile, String serverPropertiesFile) {
		try{
        // read the configuration information from the file name passed in
        // ---------------------------------------------------------------
        // ...
			BufferedReader satelliteFile = new BufferedReader(new FileReader(satellitePropertiesFile));
			BufferedReader classLoaderFile = new BufferedReader(new FileReader(classLoaderPropertiesFile));
			BufferedReader serverFile = new BufferedReader(new FileReader(serverPropertiesFile));
        
			satelliteFile.readLine();
			satelliteInfo.setName(satelliteFile.readLine().split("\t")[1]);
			satelliteInfo.setPort(Integer.parseInt(satelliteFile.readLine().split("\t")[1]));
        // create a socket info object that will be sent to the server
        // ...(this should be done in the run function I think)
        
        
        // get connectivity information of the server
        // ...
			serverInfo.setHost(serverFile.readLine().split("=")[1].trim());
			serverInfo.setPort(Integer.parseInt(serverFile.readLine().split("=")[1].trim()));
        
        // create class loader
        // -------------------
        // ...
			classLoaderFile.readLine();
			String host = classLoaderFile.readLine().split("\t")[1];
			classLoaderFile.readLine();
			classLoaderFile.readLine();
			int port = Integer.parseInt(classLoaderFile.readLine().split("\t")[1]);
			classLoader = new HTTPClassLoader(host, port);

        // read class loader config
        // ...
			classLoaderFile.readLine();
			classLoaderFile.readLine();
			classLoader.classRootDir = classLoaderFile.readLine().split("\t")[1];
        
        // get class loader connectivity properties and create class loader
        // ...(already done?)
        
        
        // create tools cache
        // -------------------
        // ...
			toolsCache = new Hashtable();
		
		}catch(IOException e){
			System.out.println("Error: " + e);
		}
        
    }

    @Override
    public void run() {

        // register this satellite with the SatelliteManager on the server
        // ---------------------------------------------------------------
        // ...
        
        
        // create server socket
        // ---------------------------------------------------------------
        // ...
        
        
        // start taking job requests in a server loop
        // ---------------------------------------------------------------
        // ...
    }

    // inner helper class that is instanciated in above server loop and processes job requests
    private class SatelliteThread extends Thread {

        Satellite satellite = null;
        Socket jobRequest = null;
        ObjectInputStream readFromNet = null;
        ObjectOutputStream writeToNet = null;
        Message message = null;

        SatelliteThread(Socket jobRequest, Satellite satellite) {
            this.jobRequest = jobRequest;
            this.satellite = satellite;
        }

        @Override
        public void run() {
            // setting up object streams
            // ...
            
            // reading message
            // ...
            
            // processing message
            switch (message.getType()) {
                case JOB_REQUEST:
                    // ...
                    break;

                default:
                    System.err.println("[SatelliteThread.run] Warning: Message type not implemented");
            }
        }
    }

    /**
     * Aux method to get a tool object, given the fully qualified class string
     *
     */
    public Tool getToolObject(String toolClassString) throws UnknownToolException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        Tool toolObject = null;

        // ...
		//If not in hash table, ask HTTPClassLoader to load class
        
        return toolObject;
    }

    public static void main(String[] args) {
        // start a satellite
        Satellite satellite = new Satellite(args[0], args[1], args[2]);
        satellite.run();
        
        //(new Satellite("Satellite.Earth.properties", "WebServer.properties")).start();
        //(new Satellite("Satellite.Venus.properties", "WebServer.properties")).start();
        //(new Satellite("Satellite.Mercury.properties", "WebServer.properties")).start();
    }
}
