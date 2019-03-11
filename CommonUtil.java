package server;
/* Aarti Nimhan (801098198)
 * Deekansha Tandon (801074066)
 * 
 *  This class contains common methods and constants used by client and server 
 *    
 */

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;

/**
 * @author Aarti
 * 
 *
 */
public class CommonUtil {

	public final static String HTTP_LINE_BREAK = "\r\n";//System.lineSeparator() ;
	public final static String HTTP_200_MSG ="HTTP/1.1 200 OK";
	public final static String HTTP_404_MSG ="HTTP/1.1 404 Not Found";
	public final static String HTTP_301_MSG ="HTTP/1.1 301 Bad Request";
	public final static String HTTP_CONTENT_TYPE_HEADER ="Content-type: ";
	public final static String CONTENT_LENGTH = "Content-length";
	public final static String HTTP_CONTENT_LENGTH_HEADER =  CONTENT_LENGTH + ": ";
	public final static String HTTP_DATE_HEADER ="Date: ";
	public final static int BUFFER_SIZE = 2048;
	public static final String SPACE_STR = " ";
	public static final String RECEIVED_RESPONSE = "Response received from server : ";
	public static final String RECEIVED_REQUEST = "Request Received from client : ";
	public static final String SEND_REQUEST_MSG = "Sending Request : ";
	public static final String SEND_RESPONSE_MSG = "Sending Response to client : ";
			
	// The Enum contains the methods supported in this project.
	public static enum HTTP_REQUEST_METHOD{
		GET, PUT
	}
	
	/**
	 * @param input 
	 * @return a boolean true if the input String is blank 
	 * and false when the String contains data.
	 */
	public static boolean isBlank(String input) {
		return (input == null || input.length() == 0 || input.trim().length() == 0);
	}
	
	/**
	 * @param inputFile
	 * @param printStream
	 * @throws IOException
	 * This method reads a file in bytes and writes it to the OutputStream.
	 */
	public static void readFileAndWriteToOutputStream(File inputFile,PrintStream printStream) throws IOException {
		int i = 0;
		/*
		 * byte[] bArray = new byte[BUFFER_SIZE]; FileInputStream fileInputStream = new
		 * FileInputStream(inputFile); while ((i = fileInputStream.read(bArray)) != -1)
		 * { printStream.write(bArray, 0, i); }
		 * 	fileInputStream.close();
		 */
		byte[] bArray = Files.readAllBytes(inputFile.toPath());
		int maxLength = bArray.length > BUFFER_SIZE? BUFFER_SIZE : bArray.length;
		while(i<bArray.length) {
			printStream.write(bArray, i, maxLength);
			i += BUFFER_SIZE;
		}
		printStream.flush();

	}
	
	/**
	 * @param inputFile
	 * @return 
	 * @throws IOException
	 * This method reads all data from the File in the form of bytes and returns a byte array. This is from the nio package of Java 7 version.
	 */
	public static byte[]  readFileAsByteArray(File inputFile) throws IOException {
		return Files.readAllBytes(inputFile.toPath());
	}
	
	
	/**
	 * @param printStream
	 * @param header
	 * @param body
	 * This method writes a message that is the Header as well as the body to the Printstream. 
	 */
	public static void writeRequestAndBodyToPrintStream(PrintStream printStream, String header, byte[] body) {
		printStream.print(header);
		printStream.flush();
		if(body!=null && body.length>0) {
			int offSet=0;
			int maxLength = body.length > BUFFER_SIZE? BUFFER_SIZE : body.length;
			while(offSet<body.length) {
				printStream.write(body, offSet, maxLength);
				offSet += BUFFER_SIZE;
				if(offSet + maxLength > body.length)
					maxLength = body.length - offSet;
			}		
		}
		printStream.flush();
	}
	
	/**
	 * @param dataInputStream
	 * @param isPrint
	 * @return
	 * @throws IOException
	 * This method parses a given data input stream to get Content-Length from the header and returns it.
	 */
	@SuppressWarnings("deprecation")
	public static int parseInputStreamToReadContentLength(DataInputStream dataInputStream, boolean isPrint) throws IOException {
		String inputLine;
		int contentLength=0;
		boolean isHTTP404 = false;
		while ((inputLine = dataInputStream.readLine()) != null) {
			if(isPrint) System.out.println(inputLine);
			if(inputLine.equals(HTTP_404_MSG))
			{
				contentLength = -1;
				isHTTP404 = true;
			}
			String[] headerArg= inputLine.split(":");
			if(!isHTTP404 && headerArg[0].equals(CONTENT_LENGTH)) {
				contentLength = Integer.parseInt(headerArg[1].trim());
			}
			if(isBlank(inputLine)|| inputLine.trim().equals(HTTP_LINE_BREAK)) {
				break;
			}
		}
		return contentLength;
	}
}
