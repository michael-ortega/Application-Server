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
import utils.PropertyHandler;

/**
 * Class [Satellite] Instances of this class represent computing nodes that
 * execute jobs by calling the callback method of tool implementation, loading
 * the tools code dynamically over a network or locally, if a tool got executed
 * before.
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
        PropertyHandler satelliteProperties = null;
        try {
            satelliteProperties = new PropertyHandler(satellitePropertiesFile);
        } catch (Exception e) {
            // no use carrying on, so bailing out ...
            e.printStackTrace();
            System.exit(1);
        }

        // create a socket info object that will be sent to the server
        try {
            satelliteInfo.setHost((InetAddress.getLocalHost()).getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }
        satelliteInfo.setPort(Integer.parseInt(satelliteProperties.getProperty("PORT")));
        satelliteInfo.setName(satelliteProperties.getProperty("NAME"));

        // get connectivity information of the server
        try {
            PropertyHandler configurationServer = new PropertyHandler(serverPropertiesFile);
            serverInfo.setHost(configurationServer.getProperty("HOST"));
            serverInfo.setPort(Integer.parseInt(configurationServer.getProperty("PORT")));
        } catch (Exception e) {
            // no use carrying on, so bailing out ...
            e.printStackTrace();
            System.exit(1);
        }

        // create class loader
        // -------------------
        PropertyHandler classLoaderProperties = null;

        // read class loader config
        try {
            classLoaderProperties = new PropertyHandler(classLoaderPropertiesFile);
        } catch (Exception e) {
            // no use carrying on, so bailing out ...
            e.printStackTrace();
            System.exit(1);
        }

        // get class loader connectivity properties
        classLoader = new HTTPClassLoader(classLoaderProperties.getProperty("HOST"), Integer.parseInt(classLoaderProperties.getProperty("PORT")));

        if (classLoader != null) {
            System.err.println("[Satellite.Satellite] HTTPClassLoader created on " + satelliteInfo.getName());
        } else {
            System.err.println("[Satellite.Satellite] Could not create HTTPClassLoader, exiting ...");
            System.exit(1);
        }

        // create tools cache
        // -------------------
        toolsCache = new Hashtable();

    }

    @Override
    public void run() {

        // register this satellite with the SatelliteManager on the server
        // ---------------------------------------------------------------
        ObjectOutputStream writeToNet = null;
        Message message = null;

        // connect to the server
        Socket server = null;
        try {
            server = new Socket(serverInfo.getHost(), serverInfo.getPort());
        } catch (IOException ex) {
            System.err.println("[Satellite.run] Opening socket to server failed");
            ex.printStackTrace();
            System.exit(1);
        }
        System.out.println("[Satellite.run] Satellite " + satelliteInfo.getName() + " connected to server, transfer connectivity information ...");

        // setting up output stream
        try {
            writeToNet = new ObjectOutputStream(server.getOutputStream());
            //readFromNet = new ObjectInputStream(server.getInputStream());
        } catch (IOException ex) {
            System.err.println("[Satellite.run] Opening object stream to server failed");
            ex.printStackTrace();
            System.exit(1);
        }

        // creating message containing satellite connectivity information
        message = new Message();
        message.setType(REGISTER_SATELLITE);
        message.setContent(satelliteInfo);

        // sending message object with connectivity info to server
        try {
            writeToNet.writeObject(message);
            writeToNet.flush();
        } catch (Exception ex) {
            System.err.println("[Satellite.run] Writing SatelliteInfo to server failed");
            ex.printStackTrace();
            System.exit(1);
        }

        // create server socket
        // ---------------------------------------------------------------
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(satelliteInfo.getPort());
        } catch (IOException ex) {
            System.err.println("[Satellite.run] Could not create server socket");
            ex.printStackTrace();
            System.exit(1);
        }

        // start taking job requests
        // ---------------------------------------------------------------
        while (true) {
            try {
                (new SatelliteThread(serverSocket.accept(), this)).start();
            } catch (IOException ex) {
                System.err.println("[Server.Server] Warning: Error accepting client");
            }
        }
    }

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
            try {
                readFromNet = new ObjectInputStream(jobRequest.getInputStream());
                writeToNet = new ObjectOutputStream(jobRequest.getOutputStream());
            } catch (IOException ex) {
                System.err.println("[SatelliteThread.run] Failed to open object streams");
                ex.printStackTrace();
                System.exit(1);
            }

            // reading message
            try {
                message = (Message) readFromNet.readObject();
            } catch (Exception e) {
                System.err.println("[SatelliteThread.run] Message could not be read from object stream.");
                e.printStackTrace();
                System.exit(1);
            }

            // processing message
            switch (message.getType()) {
                case JOB_REQUEST:
                    Job job = (Job) message.getContent();
                    String classString = job.getToolName();
                    Object arguments = job.getParameters();

                    // get tool object
                    Tool tool = null;
                    try {
                        tool = satellite.getToolObject(classString);
                    } catch (Exception ex) {
                        System.err.println("[SatelliteThread.run] Error occurred when retrieving class object for: " + classString);
                        try {
                            readFromNet.close();
                            writeToNet.close();
                            System.err.println("... closing streams and returning");
                            return;
                        } catch (IOException ex1) {
                            System.err.println("[SatelliteThread.run] Error closing object streams");
                        }
                    }

                    // do job and return result
                    Object result = tool.go(arguments);
                    try {
                        writeToNet.writeObject(result);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        System.err.println("[SatelliteThread.run] Error when writing object to output stream");
                    }

                    break;

                default:
                    System.err.println("[SatelliteThread.run] Warning: Message type not implemented");
            }
        }
    }

    /**
     * Aux method to get a tool object, given the fully qualified class string
     */
    public Tool getToolObject(String toolClassString) throws UnknownToolException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        Tool toolObject = null;

        if ((toolObject = (Tool) toolsCache.get(toolClassString)) == null) {
            Class toolClass = classLoader.loadClass(toolClassString);
            toolObject = (Tool) toolClass.newInstance();
            toolsCache.put(toolClassString, toolObject);
        } else {
            System.out.println("[Satellite.getToolObject] Tool: \"" + toolClassString + "\" already in Cache");
        }

        return toolObject;
    }

    public static void main(String[] args) {
        // start a satellite
        Satellite satellite = new Satellite(args[0], args[1], args[2]);
        satellite.run();
    }
}
