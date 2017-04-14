package appserver.client;

import appserver.comm.Message;
import appserver.comm.MessageTypes;
import appserver.job.Job;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Properties;

import utils.PropertyHandler;

public class FibonacciClient extends Thread implements MessageTypes{
    
	//Variables to hold host IPs, port numbers, and fibonacci sequence nums
    String host = null;
    int port;
	int numToCalculate = 0;

    Properties properties;

    public FibonacciClient(String serverPropertiesFile, int number) {
        try {
			numToCalculate = number;	//Put fibonacci element number into global variable
			//Store info for server connection in global variables
            properties = new PropertyHandler(serverPropertiesFile);
            host = properties.getProperty("HOST");
            System.out.println("[FibonacciClient.FibonacciClient] Host: " + host);
            port = Integer.parseInt(properties.getProperty("PORT"));
            System.out.println("[FibonacciClient.FibonacciClient] Port: " + port);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void run() {
        try { 
            // connect to application server
            Socket server = new Socket(host, port);
			
            // hard-coded string of class, aka tool name ... plus one argument
            String classString = "appserver.job.impl.Fibonacci";
            Integer number = new Integer(numToCalculate);
            
            // create job and job request message
            Job job = new Job(classString, number);
            Message message = new Message(JOB_REQUEST, job);
            
            // sending job out to the application server in a message
            ObjectOutputStream writeToNet = new ObjectOutputStream(server.getOutputStream());
            writeToNet.writeObject(message);
            
            // reading result back in from application server
            // for simplicity, the result is not encapsulated in a message
            ObjectInputStream readFromNet = new ObjectInputStream(server.getInputStream());
            Integer result = (Integer) readFromNet.readObject();
            System.out.println("FIB RESULT OF " + numToCalculate + ": " + result);
        } catch (Exception ex) {
            System.err.println("[PlusOneClient.run] Error occurred");
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        
        FibonacciClient client = null;
		for(int i = 1; i < 49; i++){	//This loops 48 times, and find fibonnaci sequence numbers 1-48
			if(args.length == 1) {
				//Create thread to retrieve a specific fibonacci number from server
				client = new FibonacciClient(args[0], i);
			} else {
				//Create generic thread to retrieve a specific fibonacci number from server
				client = new FibonacciClient("../../config/Server.properties", i);
			}
			client.start();	//Start thread
		}
    }  
}
