/*
 * This server has he functionality for the booting up peer and choosing confi file
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class Utility {
	static Socket[] sockets = new Socket[10]; ;
	static int[] list;
	static int count =0;
	
	public static String propertyFileLoc;
		
	/*
	 * This function makes socket connection of the peer with the rest of the peers in the network
	 */
	public int connectToServers() throws Exception

	{
		int numberOfServer=Integer.parseInt(getvalueFromProperty("NumberOfSystem"));
		String[] ServerIP = new String[numberOfServer];
		String[] ServerPort= new String[numberOfServer];
		for(int j=0;j<numberOfServer;j++)
		{
     	ServerIP[j]= getOptionalvalueFromProperty("server"+j);
    	ServerPort[j]=  getOptionalvalueFromProperty("server"+j+"port");
		}
		
		String selfAddress = getvalueFromProperty("locahost");
		String localPort = getvalueFromProperty("ServerPortNum");
		//static String selfIDstr=getvalueFromProperty("selfID");
		int selfId = Integer.parseInt(getvalueFromProperty("selfID"));

			
		for(int i=0;i<numberOfServer;i++)
		{

			
			if(i!=selfId)
			{
				
				String ip = ServerIP[i];
				int port = Integer.parseInt(ServerPort[i]);

				try{
					
					Socket s1= null;
					System.out.println("trying to connect to "+ip+":"+port);
					s1= new Socket(ip, port);
					System.out.println("connected to "+ip+":"+port);
					sockets[i]=s1;
					
					
				}
				catch(Exception ex)
				{

					System.out.println(ex.getMessage());
					System.out.println("Exception occurred while connecting to one of the servers");
					return -1;
				}

			}
			else
			{
				sockets[i]=null;
			}
		}
		return 0;

	}


	static  String getvalueFromProperty(String parameter)
	{
		try
		{
				//int selfId = Integer.parseInt(getvalueFromProperty("selfID"));

			String proploc  = System.getProperty("PropertyFile0"); 
			Properties prop = new Properties();
			FileInputStream input= new FileInputStream(proploc);

			prop.load(input);
			String value =  prop.getProperty(parameter);
			return value;
		}
		catch(Exception ex)
		{
			System.out.println("Error in retrieving value from Property File in utility!!"+parameter);
		}
		return null;
	}
	
	static  String getOptionalvalueFromProperty(String parameter)
	{
		try
		{
			//int selfId = Integer.parseInt(getvalueFromProperty("selfID"));

			String proploc  = System.getProperty("PropertyFile0"); 
			Properties prop = new Properties();
			FileInputStream input= new FileInputStream(proploc);

			prop.load(input);
			String value =  prop.getProperty(parameter, null);
			return value;
		}
		catch(Exception ex)
		{
			System.out.println("Error in retrieving value from Property File!!");
		}
		return null;
	}
	/*
	 * This function retrieves a socket from the Socket array
	 */
	public Socket ServerConnect(int i)
	{
		return sockets[i];

	}


	//get property file
	public static void choosePropertyFile()
	{

		try{
			JFrame parentFrame = new JFrame();

			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Choose Property file for the Peer");   

			int userSelection = fileChooser.showSaveDialog(parentFrame);

			if (userSelection == JFileChooser.APPROVE_OPTION) {
				File fileToSave = fileChooser.getSelectedFile();
				System.out.println("property file chosen is " + fileToSave.getAbsolutePath());

				propertyFileLoc=fileToSave.getAbsolutePath();
				//return(fileToSave.getAbsolutePath()); 
				setPropertyFileName(propertyFileLoc);

			}
		}
		catch(Exception ex)
		{
			System.out.println("Propery file not chosen correctly!!!");
			System.exit(0);
		}

		//return null;
	}

	public static void setPropertyFileName(String prop)
	{
		propertyFileLoc=prop;	
	}
	public static String propertyFileLocation()
	{
		return propertyFileLoc;
	}
}
