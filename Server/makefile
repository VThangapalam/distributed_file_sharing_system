JCC=javac

JFLAGS = -g

default:  Utility.class DistributedServer.class DistributedServerOperate.class ServiceClient.class

Utility.class : Utility.java
	$(JCC) $(JFLAGS) Utility.java


DistributedServer.class : DistributedServer.java
	$(JCC) $(JFLAGS) DistributedServer.java

DistributedOperateServer.class : DistributedServerOperate.java
	$(JCC) $(JFLAGS) DistributedServerOperate.java

ServiceClient.class : ServiceClient.java
	$(JCC) $(JFLAGS) ServiceClient.java
clean:
	$(RM) *.class
