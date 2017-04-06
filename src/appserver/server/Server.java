package appserver.server;

import appserver.comm.Message;
import static appserver.comm.MessageTypes.JOB_REQUEST;
import static appserver.comm.MessageTypes.REGISTER_SATELLITE;
import appserver.comm.ConnectivityInfo;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import utils.PropertyHandler;
import java.lang.String;
import java.lang.Integer;
import java.io.BufferedReader;
import java.io.FileReader;
import appserver.job.Job;

/**
 *
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class Server {

    // Singleton objects - there is only one of them. For simplicity, this is not enforced though ...
    static SatelliteManager satelliteManager = null;
    static LoadManager loadManager = null;
    static ServerSocket serverSocket = null;

    public Server(String serverPropertiesFile) {
		try{
			// create satellite and load managers
			// ...
			satelliteManager = new SatelliteManager();
			loadManager = new LoadManager();
			
			// read server port from server properties file
			int serverPort = 0;
			BufferedReader serverFile = new BufferedReader(new FileReader(serverPropertiesFile));
			serverFile.readLine();
			serverPort = Integer.parseInt(serverFile.readLine().split("=")[1].trim());
			
			// create server socket
			// ...
			serverSocket = new ServerSocket(serverPort);
		}catch(IOException e){
			System.out.println(e);
		}
    }

    public void run() {
    // start serving clients in server loop ...
    // ...
		while(true){
			try{
				Socket clientSocket = serverSocket.accept();
				new ServerThread(clientSocket);
			}catch(IOException e){
				System.out.println("Failed to establish connection: " + e);
			}
		}
    }

    // objects of this helper class communicate with clients
    private class ServerThread extends Thread {

        Socket client = null;
        ObjectInputStream readFromNet = null;
        ObjectOutputStream writeToNet = null;
        Message message = null;

        private ServerThread(Socket client) {
            this.client = client;
        }

        @Override
        public void run(){
            // setting up object streams
            // ...
			try{
				readFromNet = new ObjectInputStream(client.getInputStream());
				writeToNet = new ObjectOutputStream(client.getOutputStream());
            }catch(IOException e){
				System.out.println("Failed to create object streams: " + e);
			}
            
            // reading message
            try {
                message = (Message) readFromNet.readObject();
            } catch (Exception e) {
                System.err.println("[ServerThread.run] Message could not be read from object stream.");
                e.printStackTrace();
                System.exit(1);
            }

            // processing message
            ConnectivityInfo satelliteInfo = null;
            switch (message.getType()) {
                case REGISTER_SATELLITE:
                    // read satellite info
                    // ...
					String satelliteNameCurrent = ((Job) message.getContent()).getToolName();
                    satelliteInfo = (ConnectivityInfo) ((Job) message.getContent()).getParameters();
                    // register satellite
                    synchronized (Server.satelliteManager) {
                        // ...
						satelliteManager.registerSatellite(satelliteInfo);
                    }

                    // add satellite to loadManager
                    synchronized (Server.loadManager) {
                        // ...
						loadManager.satelliteAdded(satelliteNameCurrent);
                    }

                    break;

                case JOB_REQUEST:
                    System.err.println("\n[ServerThread.run] Received job request");

                    String satelliteName = null;
                    synchronized (Server.loadManager) {
                        // get next satellite from load manager
                        // ...
                        
                        // get connectivity info for next satellite from satellite manager
                        // ...
                    }

                    Socket satellite = null;
                    // connect to satellite
                    // ...

                    // open object streams,
                    // forward message (as is) to satellite,
                    // receive result from satellite and
                    // write result back to client
                    // ...

                    break;

                default:
                    System.err.println("[ServerThread.run] Warning: Message type not implemented");
            }
        }
    }

    // main()
    public static void main(String[] args) {
        // start the application server
        Server server = null;
        if(args.length == 1) {
            server = new Server(args[0]);
        } else {
            server = new Server("../../config/Server.properties");
        }
        server.run();
    }
}
