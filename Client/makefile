JCC=javac

JFLAGS = -g

default: PeerClient.class PeerServer.class PeerClientPerfTest.class

PeerClient.class : PeerClient.java
	$(JCC) $(JFLAGS) PeerClient.java

PeerServer.class : PeerServer.java
	$(JCC) $(JFLAGS) PeerServer.java

sPeerClientPerfTest.class : PeerClientPerfTest.java
	$(JCC) $(JFLAGS) PeerClientPerfTest.java

clean:
	$(RM) *.class
