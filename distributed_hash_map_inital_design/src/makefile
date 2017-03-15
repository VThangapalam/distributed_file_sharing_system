JCC=javac

JFLAGS = -g

default: Utility.class ServerOperate.class Peer.class PerfTest.class ServerOperateTest.class

Utility.class : Utility.java
	$(JCC) $(JFLAGS) Utility.java

ServerOperate.class : ServerOperate.java
	$(JCC) $(JFLAGS) ServerOperate.java

Peer.class : Peer.java
	$(JCC) $(JFLAGS) Peer.java


ServerOperateTest.class : ServerOperateTest.java
	$(JCC) $(JFLAGS) ServerOperateTest.java


PerfTest.class : PerfTest.java
	$(JCC) $(JFLAGS) PerfTest.java



clean:
	$(RM) *.class
