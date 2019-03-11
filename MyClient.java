package server;
/* Aarti Nimhan (801098198)
 * Deekansha Tandon (801074066)
 * 
 *  This is the HTTP Client class which sends GET or PUT requests to the HTTP server.
 *  When a GET request is sent, the client requests for a file from the server. When it receives a 200 OK response from the server it extracts the file from the response
 *  stream and creates a file and puts the contents from the response into this file in the form of bytes.
 *  When a PUT request is sent, the client requests to save a file into the servers directory. This file is read and sent in the form of a byte stream along with the PUT
 *  request. The response from the server is printed on the console.
 *    
 */
import static server.CommonUtil.BUFFER_SIZE;
import static server.CommonUtil.HTTP_CONTENT_LENGTH_HEADER;
import static server.CommonUtil.HTTP_LINE_BREAK;
import static server.CommonUtil.RECEIVED_RESPONSE;
import static server.CommonUtil.SEND_REQUEST_MSG;
import static server.CommonUtil.SPACE_STR;
import static server.CommonUtil.parseInputStreamToReadContentLength;
import static server.CommonUtil.readFileAsByteArray;
import static server.CommonUtil.writeRequestAndBodyToPrintStream;
import static server.CommonUtil.HTTP_REQUEST_METHOD.GET;
import static server.CommonUtil.HTTP_REQUEST_METHOD.PUT;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
public class MyClient {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {

		String hostName = "";
		String methodGetPutType = "";
		String filePath = "";
		int portNum = 0;

		Socket client = null;
		String response = "";
		String request = "";
		String data = "";
		BufferedInputStream inputStream = null;
		PrintStream printStream = null;
		DataInputStream dataInputStream = null;
		
		//checking whether the input parameters are valid or not

		if (args.length == 4) {
			hostName = args[0];
			portNum = Integer.parseInt(args[1]);
			methodGetPutType = args[2];
			filePath = args[3];

		} else {
			System.out.println("Invalid Parameters");
		}

		try {
			System.out.println("Connecting to: " + hostName + " on port " + portNum);
			client = new Socket(hostName, portNum);
			printStream = new PrintStream(client.getOutputStream());
			inputStream = new BufferedInputStream(client.getInputStream());
			dataInputStream = new DataInputStream(client.getInputStream());
			// if the method type is GET
			if (methodGetPutType.equalsIgnoreCase(GET.name())) {
				request = GET.name() + SPACE_STR + filePath + SPACE_STR + "HTTP/1.1" + HTTP_LINE_BREAK + "Host: " + hostName + HTTP_LINE_BREAK;
				System.out.println(SEND_REQUEST_MSG);
				System.out.println(request);
				printStream.println(request);
				System.out.println(RECEIVED_RESPONSE);
				int contentLength = parseInputStreamToReadContentLength(dataInputStream,true);
				if(contentLength > 0) {
					String[] filePathTokens = filePath.split("/");
					String fileName = filePathTokens[filePathTokens.length - 1];
					File getFile = new File(fileName);
					if(!getFile.exists()) {
						getFile.createNewFile();
					}
			        byte[] bArray = new byte[BUFFER_SIZE]; int i=0;
					FileOutputStream fileOutputStream = new FileOutputStream(getFile);
					contentLength = (int) Math.ceil((double) contentLength / BUFFER_SIZE);
			        while(contentLength>0) {
			        	i=dataInputStream.read(bArray);
			        	fileOutputStream.write(bArray, 0, i);
			        	contentLength--;
			        }
					fileOutputStream.close();
				}				
			}
			else if (methodGetPutType.equalsIgnoreCase(PUT.name())) {
				File putFile = new File(filePath);				
				if (!putFile.exists() || !putFile.isFile()) {
					System.out.println("File does not exist in the given path: " + filePath);
					return;
				}
				request = PUT.name() + SPACE_STR + filePath + SPACE_STR + "HTTP/1.1" + HTTP_LINE_BREAK + "Host: " + hostName + HTTP_LINE_BREAK + HTTP_CONTENT_LENGTH_HEADER + putFile.length()+ HTTP_LINE_BREAK + HTTP_LINE_BREAK;
				System.out.println(SEND_REQUEST_MSG);
				System.out.println(request);
				byte[] fileInByte = readFileAsByteArray(putFile);
				writeRequestAndBodyToPrintStream(printStream, request, fileInByte);
				while ((data = dataInputStream.readLine()) != null) {
					response += data + "\n";
				}
				System.out.println(RECEIVED_RESPONSE + response);
			} else {
				System.out.println("Invalid Method!!!!!");
				return;
			}

		} catch (IOException e) {
			System.out.println("Unable to connect to the server : " +e.getMessage());

		} finally {

			try {
				if(inputStream!=null) inputStream.close();
				if(printStream!=null) printStream.close();
				if(client!=null) client.close();
			} catch (IOException e) {
				System.out.println("Error occured while closing streams :" + e.toString());
			}

		}

	}

}

