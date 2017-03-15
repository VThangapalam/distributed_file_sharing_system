/*
 * This class implements the server functionality of the peer
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;


class DistributedServerOperate implements Runnable {

	private Socket server;
	private static int serverID;

	DistributedServerOperate (Socket server,int serverID) {
		this.server=server;
		this.serverID=serverID;
	}


	public void run () {
		try
		{
			BufferedOutputStream bos;
			bos = new BufferedOutputStream(server.getOutputStream());

			BufferedInputStream bis;
			bis = new BufferedInputStream(server.getInputStream());


			DataOutputStream dos =  new DataOutputStream(bos);
			DataInputStream din = new DataInputStream(bis);



			Utility utilityObj = new Utility();
			int exitcode=0;
			while(exitcode==0)
			{


				int userChoice =  din.readInt();


				switch (userChoice)
				{

				case 1:
					
					String key = din.readUTF();
					//System.out.println(key +"key");
					//System.out.println("server receivied "+key);
					if(DistributedServer.map1.containsKey(key))
					{

						//System.out.println("into contains key!!!");
						LinkedHashSet<String> sendres= DistributedServer.map1.get(key);
						/*if(sendres.size()==0)
						{							
								System.out.println("file deleted");
								dos.writeInt(-1);
								dos.flush();
								break;

							}
						 */
						//System.out.println("sent 1 in response from server");
						dos.writeInt(1);




						Iterator itr = sendres.iterator();

						StringBuilder sb = new StringBuilder();

						while(itr.hasNext())
						{
							//add a "," before every entry, except for the first one
							if(sb.toString().length() > 0)
								sb.append(">");
							//append the string
							sb.append(itr.next());
						}

						//work with string
						String myConatinatedString = sb.toString();
						dos.writeUTF(myConatinatedString);
						dos.flush();		

					}
					else
					{

						dos.writeInt(-1);
						dos.flush();
						//System.out.println("key not found ");

					}

					//System.out.println("end of get");
					break;

				case 2:
					//System.out.println("second server put!");
					String putkey = din.readUTF();

					String putvalue = din.readUTF();

					//LinkedHashSet<String> putString = (LinkedHashSet<String>) objin.readObject();
					//Client.map1.put(putkey,putString);
					try{
						if(DistributedServer.map1.containsKey(putkey))
						{
							LinkedHashSet<String> temp = DistributedServer.map1.get(putkey);
							temp.add(putvalue);
							DistributedServer.map1.put(putkey, temp);
						}
						else
						{

							LinkedHashSet<String> storeValue = new LinkedHashSet<>();
							storeValue.add(putvalue);

							DistributedServer.map1.put(putkey,storeValue);

						}
						dos.writeInt(1);


					}
					catch(Exception ex)
					{
						dos.writeInt(0);

					}
					dos.flush();
					//System.out.println("end of put in ext server");
					break;


				case 3:
				//	System.out.println("ino delete");
					String delkey = din.readUTF();
					String delValue = din.readUTF();
					if(DistributedServer.map1.containsKey(delkey))
					{
						//System.out.println("key to be deleted "+delkey);
						//DistributedServer.map1.remove(delkey);
						LinkedHashSet<String> temp = DistributedServer.map1.get(delkey);
						temp.remove(delValue);
						if(temp.isEmpty())
						{
							//System.out.println("the value is empty");
							DistributedServer.map1.remove(delkey);
							dos.writeInt(1);

						}
						else{
							DistributedServer.map1.put(delkey,temp);
							dos.writeInt(1);
						}

					}
					else
					{

						dos.writeInt(-1);
					}
					dos.flush();
				//	System.out.println("end of del");
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
