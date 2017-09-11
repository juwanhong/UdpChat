JH3831

Compile Instructions:
	1. sudo apt-get update
	2. sudo apt-get install default-jdk
	3. sudo apt-get install make
	4. Put the following files into a directory:
		- UdpChat.java
		- UdpChat_Server.java
		- ServerThread.java
		- UdpChat_Client.java
		- ClientListener.java
		- Makefile
	5. make


Program Details:
	1. To start server: >>> java UdpChat -s [port number] &
	2. To start client: >>> java UdpChat -c [nickname] [server ip] [server port] [client port]
	3. Once user is connected with a nickname, the user will either be registered or be notified that it is already registered. The user table will be printed to the console.
	4. In order to see new changes to the user table, the command is: >>> update
	5. In order to deregister, the command is: >>> dereg [nickname]
		- Note that one can only deregister itself
	6. In order to register, the command is: >>> reg [nickname]
		- Note that one can only register itself
	7. In order to send a message to another user, the command is: >>> send [recipient] [message]
		- Note that the message can be any length
	8. If the recipient's status if 'offline', the sender will be notified and the message will be saved to the server. When the recipient registers itself again, the offline messages will be printed to recipient.
	9. If the recipient's status is 'online' but is disconnected, the sender will be notified and the message will be saved to server. When the recipient reconnects, the offline messages will be printed to recipient.


Program Structures:
	- UdpChat.main() calls UdpChat_Server or UdpChat_Client depending on the arguments
	- UdpChat_Server waits for a client to connect. Once connected, starts a thread by ServerThread.start()
	- The started UdpChat_Client runs a thread ClientListener in the background to listen for any messages from fellow clients
	- UdpChat_Client sends the messages to other clients and communicates with its server counterpart, ServerThread to fetch updated user table and offline messages
	- User table is a hashmap<String, String[]> with nickname as key and the [nickname, client ip, client port, online status] as the value
	- Offline messages are stored in a hashmap<String, ArrayList<String> > with recipient as key and offline messages as value. Since the length of arraylist is dynamic and can be used as FIFO, it is a good way to store offline messages chronologically for each recipient. The whole entry is erased after the offline messages are relayed to recipient.
	- known bugs:
		* dereg doesn't try 5 times - if an issue occurs during dereg, the client fails
		* offline messages to a recipient is relayed to recipient when the recipient client types anything and presses return. For example, when user2 is 'offline' and has offline messages waiting, user2 can press anything to receive the offline messages.
		


Test case:
	Shell 1:
		>>> java UdpChat -s 9600 &
	
	Shell 2:
		>>> java UdpChat -c user1 127.0.0.1 9600 9601
		
	Shell 3:
		>>> java UdpChat -c user2 127.0.0.1 9600 9602
	
	Shell 4:
		>>> java UdpChat -c user3 127.0.0.1 9600 9603
	
	Shell 2:
		>>> update
		
		[user1 127.0.0.1 9601 online]
		[user2 127.0.0.1 9602 online]
		[user3 127.0.0.1 9603 online]
		
		>>> send user2 hi
		<<Message received by user2>>
	
	Shell 3:
		From: user1 Message: hi
	
	Shell 2:
		
		>>> send user123 hi
		<<Unrecognized recipient!>>
		
		>>> asdf
		<<Unrecognized command!>>
		
		>>> dereg user1
		
		[user1 127.0.0.1 9601 offline]
		[user2 127.0.0.1 9602 online]
		[user3 127.0.0.1 9603 online]
	
	Shell 3:
		>>> update
		
		[user1 127.0.0.1 9601 offline]
		[user2 127.0.0.1 9602 online]
		[user3 127.0.0.1 9603 online]
		
		>>> send user1 hello
		<<The recipient is offline. Saving message in server>>
		
	Shell 2:
		>>> reg user1
		
		[user1 127.0.0.1 9601 online]
		[user2 127.0.0.1 9602 online]
		[user3 127.0.0.1 9603 online]
		<<You have messages>>
		<<From: user2 <2017-03-02 04:43:43.446> Message: hello>>
	
	Shell 3:
		>>> update
		
		[user1 127.0.0.1 9601 online]
		[user2 127.0.0.1 9602 online]
		[user3 127.0.0.1 9603 online]
		
		>>
		
	# UdpChat
