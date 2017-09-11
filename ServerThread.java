import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends Thread{
	protected Socket threadSocket;
	
	public ServerThread(Socket udpSocket){
		this.threadSocket = udpSocket;
	}
	
	public void run(){
		System.out.println("<<ServerThread running...>>");
		try {
			ObjectInputStream in = new ObjectInputStream(threadSocket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(threadSocket.getOutputStream());
			String[] clientInfo = ((String) in.readObject()).split(",");
			if(UdpChat_Server.checkUser(clientInfo[0]) == false){
				UdpChat_Server.addUser(clientInfo);
				UdpChat_Server.printUserTable(UdpChat_Server.userTable);
				out.writeObject("<<User " + clientInfo[0] + " has been registered>>");
			}
			else{
				UdpChat_Server.addUser(clientInfo);
				out.writeObject("<<User " + clientInfo[0] + " is already registered>>");
			}
			UdpChat_Server.sendUserTable(out);
			
			String nickname = clientInfo[0];
			
			// At log on, check offlineMessage for this user and send messages if any exists
			ArrayList<String> offlineMessages = new ArrayList<String>();
			
			
			
			// keep serverthread going as long as the client connected to it is online.
			// keep checking for userTable update - if change, send out to client
			while(true){
				offlineMessages = UdpChat_Server.getMessages(nickname);
				if(offlineMessages != null){
					for(String s:offlineMessages){
						System.out.println(s);
					}
					
					out.writeObject("offline messages");
					out.flush();
					String offAck = (String) in.readObject();
					out.reset();
					out.writeObject(offlineMessages);
					out.flush();
					String offFinish = (String) in.readObject();
					
					UdpChat_Server.deleteMessages(nickname);
				}
				else{
					out.writeObject("no offline messages");
					out.flush();
					String offAck = (String) in.readObject();
				}
				
				System.out.println("listening for request...");
				String request = (String) in.readObject();
				if(request.equals("send table")){
					UdpChat_Server.printUserTable(UdpChat_Server.getTable());
					//UdpChat_Server.sendUserTable(out);
					out.reset();
					out.writeObject(UdpChat_Server.getTable());
					out.flush();
					String ack = (String) in.readObject();
				}
				
				if(request.equals("dereg")){
					UdpChat_Server.deregUser(nickname);
					out.reset();
					out.writeObject(UdpChat_Server.getTable());
					out.flush();
					String ack = (String) in.readObject();
				}
				
				if(request.equals("reg")){
					UdpChat_Server.regUser(nickname);
					out.reset();
					out.writeObject(UdpChat_Server.getTable());
					out.flush();
					String ack = (String) in.readObject();
				}
				
				if(request.equals("save message")){
					out.writeObject("ack save message");
					out.flush();
					String recipient = (String) in.readObject();
					out.writeObject("ack recipient");
					out.flush();
					String offlineMessage = (String) in.readObject();
					out.writeObject("ack received message");
					out.flush();
					UdpChat_Server.addMessage(recipient, offlineMessage);
				}
				
				if(request.equals("send client")){
					//do nothing and send ack
					out.writeObject("ack");
					out.flush();
				}
				
				if(request.equals("error")){
					//do nothing and send ack
					out.writeObject("ack");
					out.flush();
				}
			}

		} catch (EOFException e){
			System.out.println("Client disconnected");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		

	}
	

}
