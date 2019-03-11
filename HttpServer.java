package server;
/* Aarti Nimhan (801098198)
 * Deekansha Tandon (801074066)
 * 
 *  This is the HTTP Server class which runs on a port accepted as commandline argument when the Server is started. 
 *  Once started the HTTP Server continuously listens for a connections from Client. Once a connection is requested it creates a thread for each client. 
 *  This thread is then responsible for handling the client requests. The server continues to listen for new connections.
 *  On termination with Ctrl-C from the Command Line the HTTP Server gracefully closes all the sockets and Shuts down.
 *  
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Aarti
 *
 */
public class HttpServer {

	public static void main(String[] args) {
		int port=0;
		ServerSocket serverSocket;
//Checking if port parameter is passed
		if(args.length != 1) {
			System.out.println("Incorrect format. Expected format: HttpServer portNumber.");
			return;
		}else {
			port=Integer.parseInt(args[0]);
		}
		try {
//Creating socket with specified port number.
			serverSocket = new ServerSocket(port); 
			System.out.println("Server started. \nListening for connections on port: " + port);
			while(true) {
//Accepting connections from clients.
				try {
					Socket client = serverSocket.accept();
					//add shut down hook
					shutdownHook(serverSocket,client);
					System.out.println("");
					System.out.println("New connection accepted.");
					if(client != null) {
						HttpClientHandler connection = new HttpClientHandler(client);
						Thread request = new Thread(connection);
						request.start();
					}
				}catch (IOException e) {
					System.err.println("Unable to accept connection.");
				}
				
			}
		}catch (IOException e) {
			System.err.println("Server connection error: "+ e.getMessage());
		}
		
	}
	
	
	private static void shutdownHook(ServerSocket serverSocket, Socket client) {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				System.out.println("Shutdown Hook is running...");
				try {
					System.out.println(" Closing the Thread..");
					Thread.sleep(1000);
					if(client!=null) client.close();
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}));
		
	}
	
	

}
