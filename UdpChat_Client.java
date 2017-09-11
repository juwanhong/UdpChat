import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;


import java.io.*;

public class UdpChat_Client {

	public Socket receivingSocket;
	public static ServerSocket clientServer;
	public static String clientPort;

	public static void UdpClient(String[] clientArgs) throws UnknownHostException, ClassNotFoundException{
		String nickname = clientArgs[1];
		String serverAddress = clientArgs[2];
		InetAddress serverIP = InetAddress.getByName(serverAddress);
		String clientIP = InetAddress.getLocalHost().getHostAddress();
		int serverPort = Integer.parseInt(clientArgs[3]);
		clientPort = clientArgs[4];
		String clientStatus = "online";

		String clientInfo = nickname+","+clientIP+","+clientPort+","+clientStatus;

		String fromServer;
		String toServer;

		try (
				Socket clientSocket = new Socket(serverIP, serverPort);
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
				)
		{
			// Send UdpServer the client info
			out.writeObject(clientInfo);

			// Wait to receive back ack and userTable from server
			fromServer = (String) in.readObject();
			System.out.println(fromServer);
			HashMap<String, String[]> curTable = new HashMap<String, String[]>();
			HashMap<String, String[]> newTable = new HashMap<String, String[]>();
			curTable = (HashMap<String, String[]>) in.readObject();
			UdpChat_Server.printUserTable(curTable);

			// Initiate offMessages
			ArrayList<String> offMessages = new ArrayList<String>();


			// Go into chat mode. Start serverSock
			String recipientIP, recipientPort, recipientStatus;
			Scanner scanner = new Scanner(System.in);


			ClientListener clientListener = new ClientListener();
			clientListener.start();


			while(true){
				// Listen for any offline messages.
				String offPing = (String) in.readObject();
				if(offPing.equals("offline messages")){
					out.writeObject("off ack");
					out.flush();
					offMessages = (ArrayList<String>) in.readObject();
					out.writeObject("off ack2");
					out.flush();

					System.out.println("<<You have messages>>");
					for(String s:offMessages){
						System.out.println(s);
					}

				}
				else{
					out.writeObject("no off ack");
					out.flush();
				}


				System.out.print(">>> ");
				String command = scanner.next().toString();


				if(command.equals("update")){
					scanner.nextLine();
					out.writeObject("send table");
					out.flush();
					newTable = (HashMap<String, String[]>) in.readObject();
					out.writeObject("ack");
					out.flush();

					// print out userTable
					curTable = newTable;
					UdpChat_Server.printUserTable(curTable);

				}

				else if(command.equals("dereg")){
					scanner.nextLine();
					out.writeObject("dereg");
					out.flush();
					newTable = (HashMap<String, String[]>) in.readObject();
					out.writeObject("ack");
					out.flush();

					curTable = newTable;
					UdpChat_Server.printUserTable(curTable);
				}

				else if(command.equals("reg")){
					scanner.nextLine();
					out.writeObject("reg");
					out.flush();
					newTable = (HashMap<String, String[]>) in.readObject();
					out.writeObject("ack");
					out.flush();

					curTable = newTable;
					UdpChat_Server.printUserTable(curTable);
				}

				else if(command.equals("send")){
					String recipient = scanner.next().toString();
					String message = scanner.nextLine();



					if(curTable.containsKey(recipient) && curTable.get(recipient)[3].equals("online")){

						recipientIP = curTable.get(recipient)[1];
						recipientPort = curTable.get(recipient)[2];
						recipientStatus = curTable.get(recipient)[3];

						Socket chatSocket = new Socket();
						int counter;
						for(counter=0; counter<5; counter++){
							try{
								chatSocket.connect(new InetSocketAddress(recipientIP,Integer.parseInt(recipientPort)),500);
								ObjectOutputStream chatOut = new ObjectOutputStream(chatSocket.getOutputStream());
								ObjectInputStream chatIn = new ObjectInputStream(chatSocket.getInputStream());
								chatOut.writeObject("From: " + nickname + " Message: " + message);
								chatOut.flush();
								String chatAck = (String) chatIn.readObject();
								chatSocket.close();
								
								//Let ServerThread know that we are sending to clientlistner
								out.writeObject("send client");
								out.flush();
								String ack_send = (String) in.readObject();
								System.out.println("<<Message received by " + recipient + ">>");
								
								break;
							}
							catch(SocketTimeoutException e){
								chatSocket.close();
							}
							catch(ConnectException e){
								chatSocket.close();
							}
							catch(SocketException e){
								chatSocket.close();
							}
						}
						
						if(counter > 4){
							System.out.println("<<The recipient is offline. Saving message in server>>");
							out.writeObject("save message");
							out.flush();
							String ack = (String) in.readObject();
							Timestamp time = new Timestamp(System.currentTimeMillis());
							out.writeObject(recipient);
							out.flush();
							String ack2 = (String) in.readObject();
							out.writeObject("<<From: " + nickname + " <" + time + "> Message: " + message + ">>");
							String ack3 = (String) in.readObject();
						}

					}

					else if(curTable.containsKey(recipient) && curTable.get(recipient)[3].equals("offline")){
						System.out.println("<<The recipient is offline. Saving message in server>>");
						out.writeObject("save message");
						out.flush();
						String ack = (String) in.readObject();
						Timestamp time = new Timestamp(System.currentTimeMillis());
						out.writeObject(recipient);
						out.flush();
						String ack2 = (String) in.readObject();
						out.writeObject("<<From: " + nickname + " <" + time + "> Message: " + message + ">>");
						String ack3 = (String) in.readObject();
					}
					else{
						System.out.println("<<Unrecognized recipient!>>");
						out.writeObject("error");
						out.flush();
						in.readObject();

					}

				}

				else{
					System.out.println("<<Unrecognized command!>>");
					out.writeObject("error");
					out.flush();
					in.readObject();

				}

			}



		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
}
