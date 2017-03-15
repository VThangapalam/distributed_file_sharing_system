/*
 * This class implements the server functionality of the peer
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;


class ServerOperate implements Runnable {



	private Socket server;
	private static int serverID;

	ServerOperate (Socket server,int serverID) {
		this.server=server;
		this.serverID=serverID;
	}

	public void run () {
		try
		{

			DataOutputStream dos = new DataOutputStream(server.getOutputStream());
			DataInputStream din = new DataInputStream(server.getInputStream());

			Utility utilityObj = new Utility();

			int exitcode=0;
			while(exitcode==0)
			{


				int userChoice =  din.readInt();
			

				switch (userChoice)
				{

				case 1:
					
					String key = din.readUTF();
					if(Peer.map1.containsKey(key))
					{
						dos.writeInt(1);
						ObjectOutputStream objout = new ObjectOutputStream(server.getOutputStream());
							LinkedHashSet<String> searchresult = Peer.map1.get(key);
						Iterator itr = searchresult.iterator();
								while (itr.hasNext()){
							System.out.println(itr.next());
						}

						objout.writeObject(Peer.map1.get(key));


					}
					else
					{
						dos.writeInt(0);
								}

					break;

				case 2:

					String putkey = din.readUTF();
					String putvalue = din.readUTF();
					//LinkedHashSet<String> putString = (LinkedHashSet<String>) objin.readObject();
					//Client.map1.put(putkey,putString);
					try{
						if(Peer.map1.containsKey(putkey))
						{
						    LinkedHashSet<String> temp = Peer.map1.get(putkey);
							temp.add(putvalue);
							Peer.map1.put(putkey, temp);
						}
						else
						{
							
							LinkedHashSet<String> storeValue = new LinkedHashSet<>();
							storeValue.add(putvalue);

							Peer.map1.put(putkey,storeValue);

						}
						dos.writeInt(1);
						
					
					}
					catch(Exception ex)
					{
						dos.writeInt(0);
							}
					break;


				case 3:
					String delkey = din.readUTF();
					if(Peer.map1.containsKey(delkey))
					{
						Peer.map1.remove(delkey);
						dos.writeInt(1);
					}
					else
					{
						
						dos.writeInt(-3);
					}
					break;
				case 4:
					exitcode=1;
					break;

				default :
					
					break;
				}
				
			}
			
		} catch (Exception ioe) {
			System.out.println("IOException on socket listen:  !!!! " + ioe);
			ioe.printStackTrace();
		}		

	}
	static String getvalueFromProperty(String parameter)
	{
		try
		{
			String proploc  = System.getProperty("PropertyFile"+serverID); 
			Properties prop = new Properties();
			FileInputStream input= new FileInputStream(proploc);

			prop.load(input);
			String value =  prop.getProperty(parameter);
			return value;
		}
		catch(Exception ex)
		{
			System.out.println("Error in retrieving value from Property File!!");
		}
		return null;
	}
}
