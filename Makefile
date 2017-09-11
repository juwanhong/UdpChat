JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
		UdpChat.java \
		UdpChat_Server.java \
		ServerThread.java \
		UdpChat_Client.java \
		ClientListener.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
		$(RM) *.class