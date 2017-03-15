/*
 * @author Vaishnavi
 * Peer.java
 * This class has the main function , boots peer and intiates both server and client thread
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;



public class Peer {

	// to store Key-value pair
	public static ConcurrentMap<String, LinkedHashSet<String>> map1= new  ConcurrentHashMap<String,LinkedHashSet<String>>(10000, 0.75f, 8);
	public String keytoBehashed;


	public static void main(String[] args) throws Exception
	{

		Utility.choosePropertyFile(); 
		String filename = Utility.propertyFileLocation();
		System.setProperty("PropertyFile0", filename);


		startServer();
		System.out.println("enter the 100 to start client service of peer /n"
				+ "please ensure after all servers on the distributed network are up");
		Scanner sc = new Scanner(System.in);
		int runClient=0;      

		try {
			runClient= Integer.parseInt(new Scanner(System.in).next());
			if(runClient==100)
				startClient();
			else
				System.out.println("number to start client entered wrongly");
		} catch(NumberFormatException ne) {
			System.out.print("That's not valid a  number.\n");
			System.exit(0);
		}


	}


	//method to start a thread for Peer to act as a client
	public static void startClient() {
		(new Thread() {
			@Override
			public void run() {
				Peer peer= new Peer();
				try
				{


					Utility utilityObj= new Utility();
					int setupResult = utilityObj.connectToServers();
					DataInputStream din;
					DataOutputStream dos;

					String selfIDstr=getvalueFromProperty("selfID");
					int selfID = Integer.parseInt(getvalueFromProperty("selfID"));

					if(setupResult==0)
					{
						System.out.println("connected to distributed network");
					}
					else

					{
						System.out.println("Error in initial setup some peer maybe down");
					}

					//used for exit condition of client peer
					int exitcode=0;
					while(exitcode==0)
					{
						System.out.println("enter 1 to get a value /n"
								+ " 2 to put a value /n"
								+ "3 to delete a key /n"
								+ "4 to exit ");
						Scanner sc = new Scanner(System.in);

						//	int choice = sc.nextInt();
						int choice=0;
						try {
							choice= Integer.parseInt(new Scanner(System.in).next());
						} catch(NumberFormatException ne) {
							System.out.print("That's not valid a  number.\n");
							continue;
						}


						switch(choice)
						{
						// case 1 get a value
						case 1:

							System.out.println("Enter the key you want to search");
							String key= sc.next();
							peer.setHashKey(key);
							int serverId=peer.hashCode();


							if(serverId==selfID)
							{
								if(map1.containsKey(key))
								{
									LinkedHashSet<String> searchresult = map1.get(key);
									Iterator itr = searchresult.iterator();
									System.out.println("value for key");
									while (itr.hasNext()){
										System.out.println(itr.next());
									}
								}
								else
								{
									System.out.println("key not found!");
								}
							}
							else
							{
								din = new DataInputStream(utilityObj.ServerConnect(serverId).getInputStream());
								dos = new DataOutputStream(utilityObj.ServerConnect(serverId).getOutputStream());
								dos.writeInt(choice);
								// sending the key to server
								dos.writeUTF(key);
								int res = din.readInt();
								if(res==1)
								{
									ObjectInputStream objin = new ObjectInputStream(utilityObj.ServerConnect(serverId).getInputStream());

									LinkedHashSet<String> getres = (LinkedHashSet<String>) objin.readObject();
									Iterator itr = getres.iterator();
									while (itr.hasNext()){
										System.out.println(itr.next());
									}
								}
								else
								{
									System.out.println("the key is not found on the server!!");
								}

							}

							break;


							//put function
						case 2:

							System.out.println("Enter the key you want to store");
							String putkey= sc.next();
							System.out.println("Enter the value you want to store");
							String putvalue=sc.next();

							peer.setHashKey(putkey);
							int storeInId=peer.hashCode();


							if(storeInId==selfID)
							{


								if(map1.containsKey(putkey))
								{

									LinkedHashSet<String> temp = map1.get(putkey);
									temp.add(putvalue);
									//map1.put(Serverhashkey, temp);
									map1.put(putkey, temp);
								}
								else
								{

									LinkedHashSet<String> storeValue = new LinkedHashSet<>();
									storeValue.add(putvalue);
									map1.put(putkey,storeValue);

								}
							}
							else
							{

								//System.out.println("into storing on remote server");
								din = new DataInputStream(utilityObj.ServerConnect(storeInId).getInputStream());
								dos = new DataOutputStream(utilityObj.ServerConnect(storeInId).getOutputStream());
								dos.writeInt(choice);
								// sending the key to server
								dos.writeUTF(putkey);
								dos.writeUTF(putvalue);
								int res=din.readInt();
								if(res==1)
								{
									//System.out.println("the key value successfully stored on the server");
								}

							}
							break;

						case 3:

							System.out.println("Enter the key you want to delete");
							String delKey= sc.next();

							peer.setHashKey(delKey);
							int delFrom=peer.hashCode();

							if(delFrom==selfID)
							{
								//System.out.println("The key value has to be deleted the local host");

								if(map1.containsKey(delKey))
								{

									map1.remove(delKey);
									System.out.println(" the key value pair has been removed successfully");


								}
								else
								{

									System.out.println("the key is not found in the table");

								}
							}
							else
							{


								din = new DataInputStream(utilityObj.ServerConnect(delFrom).getInputStream());
								dos = new DataOutputStream(utilityObj.ServerConnect(delFrom).getOutputStream());
								dos.writeInt(choice);
								// sending the key to server
								dos.writeUTF(delKey);


								int res=din.readInt();
								if(res==1)
								{
									System.out.println(" the key value pair has been removed successfully");
								}
								else if(res==-3)
								{
									System.out.println("there was no such value on the server");
								}

							}
							break;

							//case 3 the client code exited when 3 is entered by user		
						case 4:
							exitcode=1;

							break;

						default:

							System.out.println("invalid choice entered");
							break;

						}	

					}

				}
				catch(Exception e)
				{
					System.out.println(e.getMessage());
				}
			}
		}).start();
	}

	// method to start thread for peer to act as Server
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
						ServerOperate conn_c= new ServerOperate(s1,selfID);
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


	public static void displayHashTable()
	{
		for (Entry<String, LinkedHashSet<String>> entry : map1.entrySet()) {
			String key = entry.getKey().toString();;
			LinkedHashSet<String> value = entry.getValue();
			System.out.println("key, " + key  );
			System.out.println(" value");
			for(String s : value)
				System.out.println(s);
		}

	}


	public void setHashKey(String key)
	{
		keytoBehashed=key;		
	}

	@Override 
	public int hashCode() {
		long hash = 5381;

		for (int i = 0; i < keytoBehashed.length(); i++)
		{
			hash = ((hash << 5) + hash) + keytoBehashed.charAt(i);
		}

		//hash%numberOfservers
		int numberOfServer=8;

		int hashres = (int) (hash%numberOfServer);

		return Math.abs(hashres);

	}

}
