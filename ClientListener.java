import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientListener extends Thread{

	private Thread thread;
	private ServerSocket clientServer;
	
	public ClientListener() throws IOException{
		
		//ServerSocket clientServer = new ServerSocket(Integer.parseInt(UdpChat_Client.clientPort));
		//Socket receivingSocket = clientServer.accept();
		
	}
	
	public void run(){
		try {
			while (true){
				ServerSocket clientServer = new ServerSocket(Integer.parseInt(UdpChat_Client.clientPort));
				Socket receivingSocket = clientServer.accept();
				ObjectInputStream receiveIn = new ObjectInputStream(receivingSocket.getInputStream());
				ObjectOutputStream receiveOut = new ObjectOutputStream(receivingSocket.getOutputStream());
				String inMessage = (String) receiveIn.readObject();
				System.out.println(inMessage);
				System.out.print(">>> ");
				receiveOut.writeObject("ack");
				receiveOut.flush();

				receivingSocket.close();
				clientServer.close();
			}
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void start () {
			thread = new Thread(this);
			thread.start ();
	}
	


}
