import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.io.*;

public class UdpChat_Server {
	public static HashMap userTable = new HashMap<String, String[]>();
	public static HashMap offlineMessage = new HashMap<String, ArrayList<String> >();
	
	public static void UdpServer(int portNumber) throws ClassNotFoundException{
		
		try {
			ServerSocket udpSocket = new ServerSocket(portNumber);
			while(true){
				// Listen for socket connection
				Socket clientSocket = udpSocket.accept();
				// Start ServerThreads
				ServerThread serverThread = new ServerThread(clientSocket);
				serverThread.start();
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static synchronized void addUser(String[] clientInfo) throws IOException{
		userTable.put(clientInfo[0], clientInfo);
	}
	
	public static synchronized HashMap<String,String[]> getTable(){
		return userTable;
	}
	
	public static boolean checkUser(String nickname) throws IOException{		
		if(userTable.containsKey(nickname)){
			return true;
		}
		else{
			return false;
		}
		
	}
	
	public static void printUserTable(HashMap<String, String[]> curTable){
		Set<String> keySet = curTable.keySet();
		String[] keys = keySet.toArray(new String[keySet.size()]);
		for(String key:keys){	
			System.out.println(Arrays.toString(curTable.get(key)));
		}
	}
	
	public static void sendUserTable(ObjectOutputStream out) throws IOException{
		out.writeObject(getTable());
		out.flush();
	}
	
	public static synchronized void deregUser(String nickname){
		String[] curValues = (String[]) userTable.get(nickname);
		curValues[3] = "offline";
		userTable.put(nickname, curValues);
	}
	
	public static synchronized void regUser(String nickname){
		String[] curValues = (String[]) userTable.get(nickname);
		curValues[3] = "online";
		userTable.put(nickname, curValues);
	}
	
	public static synchronized void addMessage(String nickname, String message){
		if(offlineMessage.containsKey(nickname)){
			ArrayList<String> messages = (ArrayList<String>) offlineMessage.get(nickname);
			messages.add(message);
		}
		else{
			ArrayList<String> messages = new ArrayList<String>();
			messages.add(message);
			offlineMessage.put(nickname, messages);
		}
	}
	
	public static synchronized ArrayList<String> getMessages(String nickname){
		ArrayList<String> messages = new ArrayList<String>();
		if(offlineMessage.containsKey(nickname)){
			messages = (ArrayList<String>) offlineMessage.get(nickname);
		}
		else{
			messages = null;
		}
		
		return messages;
	}
	
	public static synchronized void deleteMessages(String nickname){
		offlineMessage.remove(nickname);
	}
}
