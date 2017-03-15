import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DistributedServer {
	public static ConcurrentMap<String, LinkedHashSet<String>> map1= new  ConcurrentHashMap<String,LinkedHashSet<String>>(10000, 0.75f, 8);
	public String keytoBehashed;
	private static Utility utilityObj= new Utility();
	static Socket replicaSocket= null;
	static int numberOfReplica=0;
	

	public static void main(String[] args) throws Exception
	{
		
		Utility.choosePropertyFile(); 
		String filename =Utility.propertyFileLocation();
		System.setProperty("PropertyFile0", filename);
		//opening socket for other servers to communicate
		startServer();
		System.out.println("enter the 100 to start client service");
		Scanner sc = new Scanner(System.in);
		int runClient=0; 
		 numberOfReplica=Integer.parseInt(getvalueFromProperty("numberOfReplica"));
			


		try {
			runClient= Integer.parseInt(new Scanner(System.in).next());
			if(runClient==100)
			{

				int setupResult = utilityObj.connectToServers();
				if(setupResult==0)
				{
					System.out.println("connected to distributed network");
				}
				else

				{
					System.out.println("Error in initial setup some peer maybe down");
				}
			}

			else
				System.out.println("number to start client entered wrongly");
		} catch(NumberFormatException ne) {
			System.out.print("That's not valid a  number.\n");
			System.exit(0);
		}
		startService();

	}

	// opening socket to communicat with other servers in the system
	public static void startServer() {
		(new Thread() {
			@Override
			public void run() {
				try{

					int selfID = Integer.parseInt(getvalueFromProperty("selfID"));
					int clientPort = Integer.parseInt(getvalueFromProperty("ServerPortNum"));
					ServerSocket s = new ServerSocket(clientPort);
					Socket s1 ;
					while(true){
						s1 = s.accept();
						DistributedServerOperate conn_c= new DistributedServerOperate(s1,selfID);
						Thread t = new Thread(conn_c);
						t.start();
					}

				} 
				catch (IOException ioe) {
					System.out.println("IOException on socket listen: " + ioe);
					ioe.printStackTrace();
				}

			}
		}).start();
	}

	/*public static void connectToReplicaServer() {
		(new Thread() {
			@Override
			public void run() {
				try{

					String ReplicaServer_1_IP= getvalueFromProperty("ReplicaServer_1_IP");
					int ReplicaServer_1_Port = Integer.parseInt(getvalueFromProperty("ReplicaServer_1_Port").trim());
					
					try
					{
					    replicaSocket = new Socket(ReplicaServer_1_IP,ReplicaServer_1_Port);
					}
					catch(Exception ex)
					{
						System.out.println("exp "+ex.getMessage());
					}
					

				} 
				catch (Exception ioe) {
					System.out.println("IOException on socket listen: " + ioe);
					ioe.printStackTrace();
				}

			}
		}).start();
	}

*/

	
	// opening socket to server other clients
	public static void startService() {
		(new Thread() {
			@Override
			public void run() {
				try{

					// selfID = Integer.parseInt(getvalueFromProperty("selfID"));
					int servicePort = Integer.parseInt(getvalueFromProperty("ServicePortNum"));
					ServerSocket s = new ServerSocket(servicePort);
					Socket s1 ;
					while(true){
						s1 = s.accept();
						ServiceClient conn_c= new ServiceClient(s1);
						Thread t1 = new Thread(conn_c);
						t1.start();

					}

				} 
				catch (IOException ioe) {
					System.out.println("IOException on socket listen: " + ioe);
					ioe.printStackTrace();
				}

			}
		}).start();
	}

	//propertyfile
	static String getvalueFromProperty(String parameter)
	{
		try
		{
			String proploc  = System.getProperty("PropertyFile0"); 
			Properties prop = new Properties();
			FileInputStream input= new FileInputStream(proploc);

			prop.load(input);
			String value =  prop.getProperty(parameter);
			return value;
		}
		catch(Exception ex)
		{
			System.out.println("Error in retrieving value "+parameter+"from Property File!!");
			System.out.println(ex.getMessage());
		}
		return null;
	}


}
