import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Scanner;

public class ServiceClient implements Runnable{
	private Socket server;
	public String keytoBehashed;
	private static Utility utilityObj= new Utility();
	private String replica= getvalueFromProperty("replica");
	private int numofServers= Integer.parseInt(getvalueFromProperty("NumberOfSystem"));

	ServiceClient (Socket server) {
		this.server=server;
	}

	public void run () {
		System.out.println("started service client!!!!");
		DistributedServer peer= new DistributedServer();
		try
		{
			BufferedOutputStream bos;
			BufferedInputStream bis;
			DataOutputStream dos;
			DataInputStream din;

			
			BufferedOutputStream bos_1 = new BufferedOutputStream(server.getOutputStream());
			BufferedInputStream bis_1 = new BufferedInputStream(server.getInputStream());
			
			/*DataOutputStream dos_service = new DataOutputStream(server.getOutputStream());
			DataInputStream din_service = new DataInputStream(server.getInputStream());*/
			
			DataOutputStream dos_service = new DataOutputStream(bos_1);
			DataInputStream din_service = new DataInputStream(bis_1);
			
			//	ObjectOutputStream objout_service = new ObjectOutputStream(server.getOutputStream());


			String selfIDstr=getvalueFromProperty("selfID");
			int selfID = Integer.parseInt(getvalueFromProperty("selfID"));
			int replicaServerId=(selfID+1)% numofServers;
			int ImreplicaOf=selfID-1;
			if(selfID==0)
				ImreplicaOf=numofServers-1;

			System.out.println("im replica of "+ ImreplicaOf);

			//used for exit condition of client peer
			int exitcode=0;
			while(exitcode==0)
			{
				int choice=din_service.readInt();
				

				switch(choice)
				{
				// case 1 get a value
				case 1:

				//	System.out.println("into get file in service client");
					String key= din_service.readUTF();
					setHashKey(key);
					int serverId=hashCode();
					//System.out.println("key "+key);
					//System.out.println("server ID "+serverId);
					if(serverId==selfID || (serverId==ImreplicaOf&&replica.equals("on")))
					{
						if(peer.map1.containsKey(key))
						{
							//System.out.println("into contains key!!!");
							LinkedHashSet<String> searchresult = peer.map1.get(key);

							
							dos_service.writeInt(1);
							Iterator itr = searchresult.iterator();

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
							dos_service.writeUTF(myConatinatedString);
							//	dos_service.flush();	

							/*System.out.println("value for key");
							while (itr.hasNext()){
								System.out.println(itr.next());
							}*/



						}
						else
						{
							//System.out.println("writng -1 to client!!");
							dos_service.writeInt(-1);
							//dos_service.flush();
							//System.out.println("key not found!");
							//	break;
						}
						dos_service.flush();
					}
					else
					{
					//	System.out.println("sending reeq to another server");
						bis =  new BufferedInputStream (utilityObj.ServerConnect(serverId).getInputStream());
						bos =  new BufferedOutputStream(utilityObj.ServerConnect(serverId).getOutputStream());
						din = new DataInputStream(bis);
						dos = new DataOutputStream(bos);
						dos.writeInt(choice);
						// sending the key to server
						dos.writeUTF(key);
						dos.flush();
						int res = din.readInt();
						if(res==1)
						{
							dos_service.writeInt(1);
							String file=din.readUTF();
							dos_service.writeUTF(file);

						}
						else
						{
							dos_service.writeInt(-1);
							//System.out.println("key not found anywherr");
							//System.out.println("the key is not found on the server!!");
						}
						dos_service.flush();

					}

					break;


					//put function
				case 2:
					//System.out.println("into put !!**");

					String putkey= din_service.readUTF();
					String putvalue=din_service.readUTF();
					//System.out.println(" values received to store "+putkey+ " "+putvalue );
					setHashKey(putkey);
					int storeInId=hashCode();

					//System.out.println("store in "+storeInId);
					if(storeInId==selfID)
					{

						if(peer.map1.containsKey(putkey))
						{
							LinkedHashSet<String> temp = peer.map1.get(putkey);
							temp.add(putvalue);
							//map1.put(Serverhashkey, temp);
							peer.map1.put(putkey, temp);
							dos_service.writeInt(1);
						}
						else
						{

							LinkedHashSet<String> storeValue = new LinkedHashSet<>();
							storeValue.add(putvalue);
							peer.map1.put(putkey,storeValue);
							dos_service.writeInt(1);

						}

						dos_service.flush();
						if(replica.equals("on"))
						{
							//System.out.println("replica on sending the file location to next server");

							//System.out.println("server id "+ selfID + " replica id "+ replicaServerId);

							bis =  new BufferedInputStream(utilityObj.ServerConnect(replicaServerId).getInputStream());
							bos = new BufferedOutputStream(utilityObj.ServerConnect(replicaServerId).getOutputStream());
							din = new DataInputStream(bis);
							dos = new DataOutputStream(bos);
							dos.writeInt(choice);
							// sending the key to server
							dos.writeUTF(putkey);

							dos.writeUTF(putvalue);
							dos.flush();
							int res=din.readInt();
							
						}

					}
					else
					{


						bis =  new BufferedInputStream(utilityObj.ServerConnect(storeInId).getInputStream());
						bos = new BufferedOutputStream(utilityObj.ServerConnect(storeInId).getOutputStream());
						din = new DataInputStream(bis);
						dos = new DataOutputStream(bos);
						dos.writeInt(choice);
						// sending the key to server
						dos.writeUTF(putkey);

						dos.writeUTF(putvalue);
						dos.flush();
						int res=din.readInt();
						if(res==1)
						{
							dos_service.writeInt(1);
							//System.out.println("the key value successfully stored on the server");
						}
						else
						{
							dos_service.writeInt(-1);	
						}
						dos_service.flush();
					}
					break;

				case 3:

					//System.out.println("Enter the key you want to delete");
					String delKey= din_service.readUTF();
					String delValue= din_service.readUTF();
					setHashKey(delKey);
					int delFrom=hashCode();
					//System.out.println(" delete from "+delFrom);
					if(delFrom==selfID)
					{
						System.out.println("The key value has to be deleted the local host");

						if(peer.map1.containsKey(delKey))
						{
							/*System.out.println("local map has j=key");
							System.out.println("key to be seleted !!"+delKey);
							System.out.println("delValue "+delValue);
						*/	LinkedHashSet<String> temp = DistributedServer.map1.get(delKey);
							temp.remove(delValue);
							if(temp.isEmpty())
							{
								//System.out.println("the value is empty");
								peer.map1.remove(delKey);

							}
							else
							{
								//System.out.println("into else part");
								peer.map1.put(delKey,temp);
							}

							dos_service.writeInt(1);
							dos_service.flush();
							System.out.println("end of del locally");
							break;


						}
						else
						{
							dos_service.writeInt(-1);
							dos_service.flush();
							//System.out.println("the key is not found in the table");

						}
						


					}
					else
					{

						//System.out.println("into another server");
						bis = new BufferedInputStream(utilityObj.ServerConnect(delFrom).getInputStream());
						bos = new BufferedOutputStream(utilityObj.ServerConnect(delFrom).getOutputStream());
						din = new DataInputStream(bis);
						dos = new DataOutputStream(bos);
						dos.writeInt(choice);
						// sending the key to server
						dos.writeUTF(delKey);
						dos.writeUTF(delValue);
						dos.flush();

						int res=din.readInt();
						if(res==1)
						{
							dos_service.writeInt(1);
							//System.out.println(" the key value pair has been removed successfully");
							
						}
						else
						{
							dos_service.writeInt(-1);
							//System.out.println("there was no such value on the server");
						}
						//dos_service.flush();
						dos_service.flush();
						break;
					}


					//case 3 the client code exited when 3 is entered by user		
				case 4:
					exitcode=1;

					break;

				default:

					//	System.out.println("invalid choice entered");
					break;

				}	

			}

		}
		catch(Exception e)
		{
			//System.out.println("into catch exception area soket may be closed!");
			System.out.println(e.getMessage());
		}
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
		int numberOfServer=2;

		int hashres = (int) (hash%numberOfServer);
		//System.out.println("key "+keytoBehashed +"hash code "+ hashres);
		return Math.abs(hashres);

	}

}



