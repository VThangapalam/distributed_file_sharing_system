import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class PeerClientPerfTest {
	static {

		final String propFile = choosePropertyFile();
//final String propFile ="/home/ec2-user/src/Peer1.properties";
		System.setProperty("PropertyFile", propFile);
	}
	static Socket s1= null;
	static String fileDownloadLoc;

	public static void main(String[] args) throws IOException {
		
		startServer();
		startClient();

	}

	//method to start a thread for Peer to act as a client
	public static void startClient() {
		(new Thread() {
			@Override
			public void run() {
				try
				{
					
					PeerClientPerfTest cli = new PeerClientPerfTest();


					String serverIp= getvalueFromProperty("serverIP");

					int serverPort = Integer.parseInt(getvalueFromProperty("serverPort").trim());
					String peerReplication=getvalueFromProperty("fileReplication");
					Socket peerreplicasocket=null;
					String replicaIp = getvalueFromProperty("Peer_replica_IP");
					int replicaPort=Integer.parseInt(getvalueFromProperty("Peer_replica_Port"));
					String replicaStoragel=getvalueFromProperty("Peer_replica_storage_location");
					String replicaserverIp= getvalueFromProperty("ReplicaServer_1_IP");
					int replicaserverPort = Integer.parseInt(getvalueFromProperty("ReplicaServer_1_Port").trim());
					String  testFileLoc = getvalueFromProperty("FileCreationLoc");
					String fileDownloadLoc = getvalueFromProperty("fileDownloadLocation");
					String fileSize = getvalueFromProperty("fileSizeInKiloBytes");
						
					
					//Socket s1= null;
					try
					{
						s1 = new Socket(serverIp,serverPort);
					}
					catch(Exception ex)
					{

						s1 = new Socket(replicaserverIp,replicaserverPort);

						System.out.println("exp !!!!"+ex.getMessage());
					}
					if(peerReplication.equals("on"))

					{
						peerreplicasocket=new Socket(replicaIp, replicaPort);
						System.out.println("connected to replica peer");
					}
					

					String clientIp=s1.getLocalAddress().getHostAddress();
					int clientPort = Integer.parseInt(getvalueFromProperty("clientPortNum").trim());
					int exitcode=0;
					while(exitcode!=1)
					{
						System.out.println("enter 1 to get a file location,"
								+ " 2 to register a value ,"
								+ " 3 to delete a key,"
								+ " 4 to exit \n");
						Scanner sc = new Scanner(System.in);
						int choice = sc.nextInt();
						if(choice==4)
						{exitcode=1;
						break;
						}
						
						cli.createFixedSizeFiles(fileSize);
						int start= Integer.parseInt(getvalueFromProperty("PerformanceStartFileNumber"));
						int stop =Integer.parseInt(getvalueFromProperty("PerformanceStopFileNumber"));;
				
						long startTime = System.currentTimeMillis();
						for(int i=start;i<stop;i++)
						{
							
							try{
								
								BufferedInputStream bis = new BufferedInputStream(s1.getInputStream());
								BufferedOutputStream  bos = new BufferedOutputStream(s1.getOutputStream());
								
								
								/*DataInputStream din = new DataInputStream(s1.getInputStream());
								DataOutputStream dos = new DataOutputStream(s1.getOutputStream());
	*/
								DataInputStream din = new DataInputStream(bis);
								DataOutputStream dos = new DataOutputStream(bos);
								 
								
								dos.writeInt(choice);
								//dos.flush();

								switch(choice)
								{
								case 1:
									
									String filetoDownload = "test"+i+".txt";
									
									dos.writeUTF(filetoDownload);
									dos.flush();
									int res_temp = din.readInt();
									
									if(res_temp == -1)
									{
										break;
									}
									
									String fileLoc = din.readUTF();
								String	 split[];
									split = fileLoc.split(">");
									
									cli.downloadFile(split,filetoDownload);
									

									break;

									// register file
								case 2:




									String registerFileName="test"+i+".txt";

									dos.writeUTF(registerFileName);
									
									String sendValue = clientIp+"|"+clientPort+"|"+testFileLoc;
									dos.writeUTF(sendValue);
									dos.flush();
									int res= din.readInt();
									

									break;


									//deregister a file name		
								case 3:
									
									dos.writeUTF("test"+i+".txt");

									
									String  fileToDereg = testFileLoc;
										String sendPath = clientIp+"|"+clientPort+"|"+fileToDereg;
									dos.writeUTF(sendPath);
									dos.flush();
											int delres= din.readInt();
									if(delres==1)
										
								

									break;


								case 4:
									exitcode=1;
									break;
								default:

									System.out.println("invalid choice entered");
									break;

									//end of switch
								}

							}
							catch(IOException ex)

							{			s1 = new Socket(replicaserverIp,replicaserverPort);
							}

						}//end of for
						long endTime = System.currentTimeMillis();
						long duration = (endTime - startTime);
						System.out.println("response time for the "+(stop-start)+" operations!!!" + duration);
					}
				}

				catch(Exception e)
				{


					//s1 = new Socket(replicaserverIp,replicaserverPort);

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
					int clientPort = Integer.parseInt(getvalueFromProperty("clientPortNum"));
					ServerSocket s = new ServerSocket(clientPort);
					Socket s1 ;
					while(true){
						s1 = s.accept();
						PeerServer conn_c= new PeerServer(s1);
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

	/**sends he peers ip to server
	 * @param s1
	 * @param dout
	 * @return
	 * @throws Exception
	 */
	String sendIp(Socket s1, DataOutputStream dout) throws Exception
	{	
		InetAddress Ip;
		Ip =s1.getLocalAddress();
		String clientIp = Ip.getHostAddress();

		dout.writeUTF(clientIp);
		return clientIp;
	}

	/**gets the list of files in the shared folder location
	 * @param path
	 * @return
	 */
	File[] getListOfFiles(String path)
	{

		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				System.out.println("File " + listOfFiles[i].getName());
			} else if (listOfFiles[i].isDirectory()) {
				System.out.println("Directory " + listOfFiles[i].getName());
			}
		}
		return listOfFiles;
	}



	/**sends peers port number to server
	 * @param dout
	 * @param portNum
	 * @throws Exception
	 */
	void sendPortNumberOfPeer(DataOutputStream dout, int portNum) throws Exception
	{


		dout.writeInt(portNum);


	}


	/**Sends he list of file names in the shared folder location of peer
	 * @param s
	 * @param filenames
	 * @throws Exception
	 */
	void sendFileNames(Socket s,File[] filenames) throws Exception

	{
		ObjectOutputStream obj = new ObjectOutputStream(s.getOutputStream());
		obj.writeObject(filenames);
		obj.flush();

	}


	/**
	 * @param soc
	 * @param regClient
	 * @throws Exception
	 */
	void sendKeyToServer(Socket soc, HashMap<ArrayList ,String> regClient) throws Exception

	{

		ObjectOutputStream objout = new ObjectOutputStream(soc.getOutputStream());
		objout.writeObject(regClient);

	}

	void uploadFile(Socket replicasocket,String fileName,String fileAbsolutePath,String replicalocation)
	{
		//DataInputStream din = new Da;
		try {
			DataOutputStream dos = new DataOutputStream(replicasocket.getOutputStream());
			InputStream is= null;
			FileOutputStream fos=null;
			BufferedOutputStream bos=null;
			BufferedOutputStream outToClient = new BufferedOutputStream(replicasocket.getOutputStream());
			dos.writeInt(2);
			dos.writeUTF(fileName);
			dos.writeUTF(replicalocation);
			if (outToClient != null) {
				File myFile = new File (fileAbsolutePath);

				byte[] mybytearray = new byte[(int) myFile.length()];

				FileInputStream fis = null;

				try {
					fis = new FileInputStream(myFile);
				} catch (FileNotFoundException ex) {
					// Do exception handling
				}
				BufferedInputStream bis = new BufferedInputStream(fis);


				bis.read(mybytearray, 0, mybytearray.length);
				outToClient.write(mybytearray, 0, mybytearray.length);
				outToClient.flush();
				outToClient.close();
				fis.close();
				//dos.close();

				// File sent, exit the main method
				return;
			}
		}catch (IOException ex) {
			// Do exception handling
		}

	}


	/**function downloads the file from the peer , id of which was sent by the index server
	 * @param fileLoc
	 * @param file_name
	 */
	void downloadFile(String[] locList, String file_name)
	{
		DataInputStream din =null;
		DataOutputStream dos = null;
		InputStream is= null;
		FileOutputStream fos=null;
		BufferedOutputStream bos=null;
		try{
			//location of download
			String fileDownloadLoc = getvalueFromProperty("fileDownloadLocation");
			
			String  fileloc = fileDownloadLoc+file_name;
			String[] strArr = locList;

			/*System.out.println("Enter index number the link from where you want to download the file");
			for(int i =0;i<strArr.length;i++)
			{
				System.out.println("Index:"+i+". "+strArr[i]);
			}*/
			//	Scanner sc_obj = new Scanner(System.in);
			int index_fileLink = 0;

			if(strArr[index_fileLink]==null)
			{                                                                                                                                                                                                                                     
				//invalid condition

			}
			String loc = strArr[index_fileLink];
			String delims = "[|]";
			String[] splitStrings = loc.split(delims);
			String ip = splitStrings[0];
			int port = Integer.parseInt(splitStrings[1]);
			String filepath = splitStrings[2]+"/";

			//System.out.println("IP chosen "+ip);
			//System.out.println("Port "+port);
			Socket socket = new Socket(ip, port);
			//System.out.println(socket.isConnected());
			try {

				//System.out.println("starting download of file "+file_name);


				din = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());
				//to start download
				dos.writeInt(1);
				dos.writeUTF(file_name);
				dos.writeUTF(filepath);

				//download
				byte[] aByte = new byte[1];
				int bytesRead;

				is = socket.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				if (is != null) {
					
					fos = new FileOutputStream(fileloc);
					bos = new BufferedOutputStream(fos);
					bytesRead = is.read(aByte, 0, aByte.length);

					do {
						baos.write(aByte);
						bytesRead = is.read(aByte);
					} while (bytesRead != -1);

					bos.write(baos.toByteArray());

					bos.flush();
					File filedownload = new File(fileloc);
					long len = filedownload.length();
					//System.out.println("File "+file_name+ " "+len+"bytes download complete file in "+fileloc);
				} 

			}catch (Exception e) {
				//System.out.println(" error in downloadingfile try another link");
				System.out.println(e.getMessage());
				e.printStackTrace();

			}
			finally
			{
				din.close();
				dos.close();
				is.close();
				fos.close();
				bos.close();
			}

			//	socket.close();  
		}
		catch(Exception ex)
		{
			System.out.println("Exception when downloading file try another link is present , the source not existing in the location you selected!!");
			System.out.println(ex);
		}
	}

	static  String getvalueFromProperty(String parameter)
	{
		try
		{
			String proploc  = System.getProperty("PropertyFile"); 
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

	public static String choosePropertyFile()
	{
		try{


			JFrame parentFrame = new JFrame();

			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Choose Property file for the client");   

			int userSelection = fileChooser.showSaveDialog(parentFrame);

			if (userSelection == JFileChooser.APPROVE_OPTION) {
				File fileToSave = fileChooser.getSelectedFile();
				System.out.println("property file chosen is " + fileToSave.getAbsolutePath());
				return(fileToSave.getAbsolutePath()); 


			}
		}
		catch(Exception ex)
		{

			System.out.println("Exception occured while choosing property file !! please upload the correct");

		}
		return null;
	}



	public static File chooseFileToBeRegistered()
	{
		try{


			JFrame parentFrame = new JFrame();

			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Choose the file to be registered/deregisterd");   

			int userSelection = fileChooser.showSaveDialog(parentFrame);

			if (userSelection == JFileChooser.APPROVE_OPTION) {
				File fileToRegister = fileChooser.getSelectedFile();
				System.out.println("The file chosen to be registered is " + fileToRegister.getAbsolutePath());
				return(fileToRegister); 


			}
		}
		catch(Exception ex)
		{

			System.out.println("Error occured while trying to choose file retry");

		}
		return null;
	}
	
	void createFixedSizeFiles(String filesizeInKB)
	
	{

		BufferedWriter output = null;
		
		String dirLoc= getvalueFromProperty("FileCreationLoc");
	
		String filename=dirLoc+"test";
		
		String start = (getvalueFromProperty("PerformanceStartFileNumber").trim());
		String shellScriptLoc = getvalueFromProperty("shellScriptLoc");
		
		
		String  end = (getvalueFromProperty("PerformanceStopFileNumber").trim());
		try
		{
		String[] cmd = { shellScriptLoc, start, end , filesizeInKB ,dirLoc };
		Process p = Runtime.getRuntime().exec(cmd);
		}
		catch (Exception ex)
		{
			System.out.println("ex "+ex.getMessage());
		}

	}
	
	void createTextFiles()
	{
		
		BufferedWriter output = null;
		
		String dirLoc= getvalueFromProperty("FileCreationLoc");
		
		String filename=dirLoc+"test";
		
		int start = Integer.parseInt(getvalueFromProperty("PerformanceStartFileNumber").trim());
		
		int end = Integer.parseInt(getvalueFromProperty("PerformanceStopFileNumber").trim());
		
		for(int i=start;i<end;i++)
			try {
				
				String t = String.valueOf(i);
				String text ="Ubuntu This is the first line of  file test"+i;
				File file = new File(dirLoc+"test"+i+".txt");
				output = new BufferedWriter(new FileWriter(file));
				output.write(text);
				;
			} catch ( IOException e ) {
				e.printStackTrace();
			} finally {
				if ( output != null ) 
					try
				{
						output.close();
				}
				catch(Exception ex)
				{System.out.println("exception occ");
				System.out.println(ex.getMessage());
				}
			}

	}


}
