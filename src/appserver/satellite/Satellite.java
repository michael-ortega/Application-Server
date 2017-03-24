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

        // read the configuration information from the file name passed in
        // ---------------------------------------------------------------
        // ...
        
        
        // create a socket info object that will be sent to the server
        // ...
        
        
        // get connectivity information of the server
        // ...
        
        
        // create class loader
        // -------------------
        // ...

        // read class loader config
        // ...
        
        
        // get class loader connectivity properties and create class loader
        // ...
        
        
        // create tools cache
        // -------------------
        // ...
        
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
