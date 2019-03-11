package server;
/* Aarti Nimhan (801098198)
 * Deekansha Tandon (801074066)
 * 
 *  This class contains methods to handle request from single client. An instance of this class is created by the Server for each client.
 *  That is new thread is created for a new client.
 *  The Requests supported are GET and PUT. Any request other than this is replied with 301 Bad Request.
 *  Closes the client connection once a request is served.
 *    
 */
import static server.CommonUtil.BUFFER_SIZE;
import static server.CommonUtil.HTTP_200_MSG;
import static server.CommonUtil.HTTP_301_MSG;
import static server.CommonUtil.HTTP_404_MSG;
import static server.CommonUtil.HTTP_CONTENT_LENGTH_HEADER;
import static server.CommonUtil.HTTP_CONTENT_TYPE_HEADER;
import static server.CommonUtil.HTTP_DATE_HEADER;
import static server.CommonUtil.HTTP_LINE_BREAK;
import static server.CommonUtil.RECEIVED_REQUEST;
import static server.CommonUtil.SEND_RESPONSE_MSG;
import static server.CommonUtil.SPACE_STR;
import static server.CommonUtil.isBlank;
import static server.CommonUtil.parseInputStreamToReadContentLength;
import static server.CommonUtil.readFileAsByteArray;
import static server.CommonUtil.writeRequestAndBodyToPrintStream;
import static server.CommonUtil.HTTP_REQUEST_METHOD.GET;
import static server.CommonUtil.HTTP_REQUEST_METHOD.PUT;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Aarti
 *
 */
public class HttpClientHandler implements Runnable {
	Socket client;
	String serverPath;
	
	public HttpClientHandler(Socket client) {
		this.client = client;
		this.serverPath = createServerDirectory();
	}
		
	public Socket getClient() {
		return client;
	}
	
	public String getServerPath() {
		return serverPath;
	}

	/**
	 * @return
	 * This method creates a directory named ServerDir for the server to get and store files to, if it does not already exist. 
	 */
	private String createServerDirectory() {
		String path = System.getProperty("user.dir")+ File.separator +"ServerDir";
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
			System.out.println("ServerDir does not exist. Creating ServerDir");
		}
		return path;
	}
	@Override
	public void run() {

		DataInputStream dataInputStream = null;
		try {
			dataInputStream = new DataInputStream(getClient().getInputStream());
			//Could have used BufferedInputStream, but that works only for text files not for images.
			handleRequest(dataInputStream);
			
		}catch (IOException e) {
			System.out.println("Error occured while handling client request "+e.toString());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {			
				try {
					if(dataInputStream!=null) dataInputStream.close();
				} catch (IOException e) {
					System.out.println("Error occured while closing streams :" + e.toString());
				}
		}
	}
	/**
	 * @param dataInputStream
	 * @throws IOException
	 * @throws InterruptedException
	 *  This method is called whenever a new request is to be handled by a server. It reads the inputstream and extracts the Method the client requested. 
	 *  IF the Client requests a GET method this method will search for the requested file in the "ServerDir" directory and all its child directories. Reads the file.
	 *  Builds and sends a response based on whether the file is found. 
	 *  IF the Client requests a PUT method this method will read the file sent by the client and save it in the "ServerDir" directory. It then builds and sends a response. 
	 */
	@SuppressWarnings("deprecation")
	public void handleRequest(DataInputStream dataInputStream) throws IOException, InterruptedException {
		String serverResponseTxt;
		//Read the first line of request received from Client
		String inputLine = dataInputStream.readLine();
		File serverFile = null;
		byte[] body=null;
		if(null != inputLine) {
			System.out.println(RECEIVED_REQUEST);
			System.out.println(inputLine);
			//parse the request to find out the httpRequestMethod
			String tokens[] = inputLine.split(SPACE_STR);
			String httpRequestMethod = tokens[0];
			String directories[] = tokens[1].split("/");
			String fileName = directories[directories.length - 1];
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			String relativeFilePath = "";
			for(int i =1 ; i< directories.length-1; i++) {
				relativeFilePath = relativeFilePath + File.separator + directories[i];
			}

			String absoluteFilePath = getServerPath() + relativeFilePath + File.separator + fileName;
			if(httpRequestMethod.equals(GET.name())) {
				serverFile = new File(absoluteFilePath);
				if(serverFile.exists() && serverFile.isFile()) {
					serverResponseTxt = HTTP_200_MSG + HTTP_LINE_BREAK + 
							HTTP_DATE_HEADER + sdf.format(new Date()) + HTTP_LINE_BREAK+
							HTTP_CONTENT_TYPE_HEADER + URLConnection.guessContentTypeFromName(fileName) + HTTP_LINE_BREAK +
							HTTP_CONTENT_LENGTH_HEADER +serverFile.length() + HTTP_LINE_BREAK + HTTP_LINE_BREAK;
					body = readFileAsByteArray(serverFile);
							
				}else {
					String errorMessage = "Unable to find " + fileName + " on this server.";
					System.out.println(errorMessage);
					serverResponseTxt = HTTP_404_MSG + HTTP_LINE_BREAK+
							HTTP_DATE_HEADER + sdf.format(new Date()) + HTTP_LINE_BREAK + HTTP_LINE_BREAK;
				}
			}
			else if(httpRequestMethod.equals(PUT.name())) {
				if(!isBlank(relativeFilePath)) {
					serverFile = new File(relativeFilePath);
					if(!serverFile.exists())
						serverFile.mkdir();
				}
				serverFile = new File(absoluteFilePath);
				if(!serverFile.exists()) {
					serverFile.createNewFile();
				}
				int contentLength = 0;
		        contentLength = parseInputStreamToReadContentLength(dataInputStream,false);
				contentLength = (int) Math.ceil((double) contentLength / BUFFER_SIZE);
		        byte[] bArray = new byte[BUFFER_SIZE]; int i=0;
				FileOutputStream fileOutputStream = new FileOutputStream(absoluteFilePath);
		        while(contentLength>0) {
		        	i=dataInputStream.read(bArray);
		        	fileOutputStream.write(bArray, 0, i);
		        	contentLength--;
		        }
				fileOutputStream.close();
				String displayMsg ="File Created Successfully";
				serverResponseTxt = HTTP_200_MSG +" File Created Successfully"+ HTTP_LINE_BREAK + 
						HTTP_DATE_HEADER + sdf.format(new Date()) + HTTP_LINE_BREAK+
						HTTP_CONTENT_TYPE_HEADER + URLConnection.guessContentTypeFromName(fileName) + HTTP_LINE_BREAK +
						HTTP_CONTENT_LENGTH_HEADER + displayMsg.length() + HTTP_LINE_BREAK + HTTP_LINE_BREAK + displayMsg;
			
			}else {
				serverResponseTxt = HTTP_301_MSG + HTTP_LINE_BREAK + 
						HTTP_DATE_HEADER + sdf.format(new Date()) + HTTP_LINE_BREAK;
			}
			sendServerResponse(serverResponseTxt,body);
			System.out.println("Connection: " + getClient().getRemoteSocketAddress() + " Closed");
			getClient().close();
			
		}
	}

	/**
	 * @param response
	 * @param body
	 * @throws IOException
	 */
	public void sendServerResponse(String response, byte[] body) throws IOException {
		System.out.println(SEND_RESPONSE_MSG);
		System.out.println(response);
		PrintStream printStream = new PrintStream(getClient().getOutputStream());
		writeRequestAndBodyToPrintStream(printStream,response,body);
		printStream.close();
	}
}
