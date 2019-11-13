import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Open an SMTP connection to a remote machine and send one mail.
 *
 */
public class SMTPConnection {
    /* The socket to the server */
    private Socket connection;

    /* Streams for reading and writing the socket */
    private BufferedReader fromServer;
    private DataOutputStream toServer;

    private static final int SMTP_PORT = 25;
    private static final String CRLF = "\r\n";

    /* Are we connected? Used in close() to determine what to do. */
    private boolean isConnected = false;

    /* Create an SMTPConnection object. Create the socket and the associated streams. Initialize SMTP connection. */
    public SMTPConnection(Envelope envelope) throws IOException {
	connection = new Socket("127.0.0.1",SMTP_PORT);
	fromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));;
	toServer =   new DataOutputStream(connection.getOutputStream());
	
	/* Read a line from server and check that the reply code is 220. If not, throw an IOException. */
	String checkConn = fromServer.readLine();
	if(parseReply(checkConn) != 220){
		throw new IOException("Connection not established!");
	}

	/* SMTP handshake. We need the name of the local machine.
	   Send the appropriate SMTP handshake command. */
	String localhost = "127.0.0.1";
	//sendCommand("HELLO " + localhost+ CRLF, 250);

	isConnected = true;
    }

    /* Send the message. Write the correct SMTP-commands in the
       correct order. No checking for errors, just throw them to the
       caller. */
    public void send(Envelope envelope) throws IOException {
		sendCommand("MAIL FROM: " + envelope.Sender + CRLF,250);
	    sendCommand("RECIPIENT TO: " + envelope.Recipient + CRLF ,250);
	    sendCommand("DATA"+ CRLF ,354);
	/* Send all the necessary commands to send a message. Call
	   sendCommand() to do the dirty work. Do _not_ catch the
	   exception thrown from sendCommand(). */
	
    }

    /* Close the connection. First, terminate on SMTP level, then
       close the socket. */
    public void close() {
	isConnected = false;
	try {
	    sendCommand("QUIT " + CRLF, 221);
	    connection.close();
	} catch (IOException e) {
	    System.out.println("Unable to close connection: " + e);
	    isConnected = true;
	}
    }

    /* Send an SMTP command to the server. Check that the reply code
       is what is is supposed to be according to RFC 821. */
    private void sendCommand(String command, int rc) throws IOException {
		System.out.println("Command to server: " + command +CRLF);
	/* Write command to server and read reply from server. */
		toServer.writeBytes(command+CRLF);
		System.out.println("Reply from server: " + parseReply(fromServer.readLine()));
	/* Check that the server's reply code is the same as the 
	   parameter rc. If not, throw an IOException. */
	    if (parseReply(fromServer.readLine()) != rc){
	        System.out.println("The rc is not the same as the reply code");
	        System.out.println("Expected RC: " + rc + " Received RC: " + parseReply(fromServer.readLine()));
	        throw new IOException("Expected RC: " + rc + " Received RC: " + parseReply(fromServer.readLine()));
	    }
	/* Fill in */
    }

    /* Parse the reply line from the server. Returns the reply 
       code. */
    private int parseReply(String reply) {
		StringTokenizer madTokenz = new StringTokenizer(reply," ");
	    String replyCode = madTokenz.nextToken();
	    return Integer.parseInt(replyCode);
    }

    /* Destructor. Closes the connection if something bad happens. */
    protected void finalize() throws Throwable {
	if(isConnected) {
	    close();
	}
	super.finalize();
    }
}