import java.io.IOException;
import java.net.*;
import java.io.*;

public class UdpChat {
	
	public static void main(String[] args) throws UnknownHostException, ClassNotFoundException{
		
		String mode = args[0];
		
		switch (mode){
		case "-s":
			// start server. check length of args.
			if(args.length == 2){
				//start server
				int port = Integer.parseInt(args[1]);
				UdpChat_Server.UdpServer(port);
			}
			else{
				//print error statement
				System.out.println("To start a server, input in this format: UdpChat -s [portNumber]");
			}
		case "-c":
			//start client. check length of args
			if(args.length == 5){
				UdpChat_Client.UdpClient(args);
			}
			else{
				System.out.println("To start a client, input in this format: UdpChat -c [nickname] [server ipaddress] [server port] [client port]");
			}
		}
			
	}
	
}