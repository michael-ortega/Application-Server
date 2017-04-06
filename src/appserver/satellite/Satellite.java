package appserver.satellite;

/*Various import statments for things like sockets, server sockets, hashtables, strings,
and object stream readers and writers*/ 
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
import java.lang.ClassLoader;
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

	/*Each of these variables hold information for the satellite, server to connect to, the class loader,
	and the hashtable cache for tools*/
    private ConnectivityInfo satelliteInfo = new ConnectivityInfo();
    private ConnectivityInfo serverInfo = new ConnectivityInfo();
    private HTTPClassLoader classLoader = null;
    private Hashtable toolsCache = null;

    public Satellite(String satellitePropertiesFile, String classLoaderPropertiesFile, String serverPropertiesFile) {
		try{
			//Open file readers for each of our property files
			BufferedReader satelliteFile = new BufferedReader(new FileReader(satellitePropertiesFile));
			BufferedReader classLoaderFile = new BufferedReader(new FileReader(classLoaderPropertiesFile));
			BufferedReader serverFile = new BufferedReader(new FileReader(serverPropertiesFile));
        
			//Read from satellite property file and store Name/Port info in satelliteInfo
			satelliteFile.readLine();
			satelliteInfo.setName(satelliteFile.readLine().split("\t")[1]);
			satelliteInfo.setPort(Integer.parseInt(satelliteFile.readLine().split("\t")[1]));
        
        
			//Read from server property file and store Host/Port info in serverInfo
			serverInfo.setHost(serverFile.readLine().split("=")[1].trim());
			serverInfo.setPort(Integer.parseInt(serverFile.readLine().split("=")[1].trim()));
        
			//Read from class loader property file and store Host/Port info into new classLoader
			classLoaderFile.readLine();
			String host = classLoaderFile.readLine().split("\t")[1];
			classLoaderFile.readLine();
			classLoaderFile.readLine();
			int port = Integer.parseInt(classLoaderFile.readLine().split("\t")[1]);
			classLoader = new HTTPClassLoader(host, port);

			//Read form class loader property file and store root directory into classLoader
			classLoaderFile.readLine();
			classLoaderFile.readLine();
			classLoader.classRootDir = classLoaderFile.readLine().split("\t")[1];
        
			//Create tools cache to hold tools already loaded using classLoader
			toolsCache = new Hashtable();
			
			//Close property files
			satelliteFile.close();
			classLoaderFile.close();
			serverFile.close();
			
			
		}catch(IOException e){
			System.out.println("Error: " + e);
		}
        
    }

    @Override
    public void run() {
		try{
			// register this satellite with the SatelliteManager on the server
			// ---------------------------------------------------------------
			// ...
			Socket server = new Socket(serverInfo.getHost(), serverInfo.getPort());
			Job job = new Job(satelliteInfo.getName(), satelliteInfo);
			Message message = new Message(REGISTER_SATELLITE, job);
			ObjectOutputStream writeToNet = new ObjectOutputStream(server.getOutputStream());
			writeToNet.writeObject(message);
        
			//Create satellite server socket
			ServerSocket serverSocket = new ServerSocket(serverInfo.getPort());
			//Display satellite info to user
			System.out.println("Satellite " + satelliteInfo.getName() + " initialized at Port: " + satelliteInfo.getPort());
			
			//Server loop to take job requests, starting with an accepted connection
			while(true){
				Socket clientSocket = serverSocket.accept();
				//Create new thread, pass in connected client socket, and current running object
				new SatelliteThread(clientSocket, this).start();
			}
		}catch(IOException e){	//Catch and display any Input/Output Exceptions
			System.out.println("Error: " + e);
		}
    }

    // inner helper class that is instanciated in above server loop and processes job requests
    private class SatelliteThread extends Thread {

		//These variables will hold info to help us get job requests from clients, and send the results back
        Satellite satellite = null;
        Socket jobRequest = null;
        ObjectInputStream readFromNet = null;
        ObjectOutputStream writeToNet = null;
        Message message = null;

        SatelliteThread(Socket jobRequest, Satellite satellite) { //Store client socket and running satellite object
            this.jobRequest = jobRequest;
            this.satellite = satellite;
        }

        @Override
        public void run() {
			try{
				//Set up input and output streams to both receive and send to client socket
				readFromNet = new ObjectInputStream(jobRequest.getInputStream());
				writeToNet = new ObjectOutputStream(jobRequest.getOutputStream());
            
				//Read message from client
				message = (Message) readFromNet.readObject();
				
				//If message is a job request, process it, else, return error
				switch (message.getType()) {
					case JOB_REQUEST:
						//Get tool object by using information from message, and the func. getToolObject()
						Tool tool = getToolObject(((Job) message.getContent()).getToolName());
						//Write result of using tool on message back to client
						writeToNet.writeObject(tool.go(((Job) message.getContent()).getParameters()));
						break;

					default:
						System.err.println("[SatelliteThread.run] Warning: Message type not implemented");
				}
			}catch(IOException e){	//Catch various possible errors
				System.out.println("Error: " + e);
			}catch(UnknownToolException e){
				System.out.println("Tool could not be found: " + e);
			}catch(ClassNotFoundException e){
				System.out.println("Class not found: " + e);
			}catch(InstantiationException e){
				System.out.println("There was an instantiation exception: " + e);
			}catch(IllegalAccessException e){
				System.out.println("Illegal access: " + e);
			}
        }
    }

    /**
     * Aux method to get a tool object, given the fully qualified class string
     *
     */
    public Tool getToolObject(String toolClassString) throws UnknownToolException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        Tool toolObject = null; //This variable will hold our tool object to be returned

		//If not in hash table, ask HTTPClassLoader to load class
		if ((toolObject = (Tool) toolsCache.get(toolClassString)) == null) {
            System.out.println("\nTool's Class: " + toolClassString);
			//Load auxiliary version of class first
			Class toolClassAux = classLoader.findClass(toolClassString + "Aux");
			//Now load the class specified by toolClassString
            Class toolClass = classLoader.findClass(toolClassString);
			//Create object using loaded class
            toolObject = (Tool) toolClass.newInstance();
			//Put object into hashtable, marked by toolClassString
            toolsCache.put(toolClassString, toolObject);
        } else {	//If in hash table, tell user it is already in the cache
            System.out.println("Tool: \"" + toolClassString + "\" already in Cache");
        }
        
        return toolObject;	//Return tool object
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
