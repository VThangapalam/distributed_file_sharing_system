import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

public class PeerClient {
	static {

		final String propFile = choosePropertyFile();
//final String propFile ="/home/ec2-user/src/Peer1.properties";
		System.setProperty("PropertyFile", propFile);
	}
	static Socket s1= null;

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
					System.out.println("1");
					PeerClient cli = new PeerClient();
					String serverIp= getvalueFromProperty("serverIP");
					int serverPort = Integer.parseInt(getvalueFromProperty("serverPort").trim());
					String peerReplication=getvalueFromProperty("fileReplication");
					Socket peerreplicasocket=null;
					String replicaIp = getvalueFromProperty("Peer_replica_IP");
					int replicaPort=Integer.parseInt(getvalueFromProperty("Peer_replica_Port"));
					String replicaStoragel=getvalueFromProperty("Peer_replica_storage_location");
					//Socket s1= null;
					try
					{
						s1 = new Socket(serverIp,serverPort);
					}
					catch(Exception ex)
					{
						System.out.println("primary server down connectng to replica server");
						String replicaserverIp= getvalueFromProperty("ReplicaServer_1_IP");
						int replicaserverPort = Integer.parseInt(getvalueFromProperty("ReplicaServer_1_Port").trim());

						s1 = new Socket(replicaserverIp,replicaserverPort);

						System.out.println("exp "+ex.getMessage());
					}
					if(peerReplication.equals("on"))

					{
						peerreplicasocket=new Socket(replicaIp, replicaPort);
						System.out.println("connected to replica peer");
					}

					System.out.println("2");
					//ObjectInputStream objin = new ObjectInputStream(s1.getInputStream());
					System.out.println("3");
					//send Ip and Port of client
					String clientIp=s1.getLocalAddress().getHostAddress();
					int clientPort = Integer.parseInt(getvalueFromProperty("clientPortNum").trim());
					System.out.println("5");
					//used for exit condition of client peer
					int exitcode=0;
					while(exitcode==0)
					{
						try{

							BufferedInputStream bis = new BufferedInputStream(s1.getInputStream());
							BufferedOutputStream  bos = new BufferedOutputStream(s1.getOutputStream());


							/*DataInputStream din = new DataInputStream(s1.getInputStream());
							DataOutputStream dos = new DataOutputStream(s1.getOutputStream());
							 */
							DataInputStream din = new DataInputStream(bis);
							DataOutputStream dos = new DataOutputStream(bos);

							//System.out.println(din.readUTF());
							//System.out.println(din.readUTF());
							System.out.println("enter 1 to get a file location /n"
									+ " 2 to register a value /n"
									+ "3 to delete a key /n"
									+ "4 to exit /n");
							Scanner sc = new Scanner(System.in);
							int choice = sc.nextInt();
							//sending user choice to server
							System.out.println("choice entered by user "+choice);

							dos.writeInt(choice);
							dos.flush();
							long startTime = System.currentTimeMillis();

							switch(choice)
							{
							case 1:

								System.out.println("Please enter the file name you want to download");
								String filetoDownload = sc.next();
								//send file name
								dos.writeUTF(filetoDownload);
								System.out.println("sent file name"+filetoDownload);
								dos.flush();
								int res_temp = din.readInt();
								System.out.println("result received !!"+ res_temp);
								if(res_temp == -1)
								{
									System.out.println(" Sorry the file "+filetoDownload+" is not found!!!");
									break;
								}
								System.out.println("after***");
								//LinkedHashSet<String> fileLoc = (LinkedHashSet<String>) objin.readObject();

								String fileLoc = din.readUTF();
								System.out.println("file loc !!"+fileLoc);
								String split[];
								split = fileLoc.split(">");
								/*Iterator itr = split.iterator();

							while (itr.hasNext()){
								System.out.println(itr.next());
							}*/
								System.out.println("got loc going to download");
								cli.downloadFile(split,filetoDownload);

								break;

								// register file
							case 2:

								System.out.println("choose the file which you want to register");

								File file_name_to_register =chooseFileToBeRegistered();;
								String  fileToRegister = file_name_to_register.getAbsolutePath();
								String registerFileName=file_name_to_register.getName();

								dos.writeUTF(registerFileName);
								//dos.flush();
								System.out.println("sent file name");
								int index=fileToRegister.lastIndexOf('/');
								String Path =fileToRegister.substring(0,index);
								String sendValue = clientIp+"|"+clientPort+"|"+Path;
								dos.writeUTF(sendValue);
								dos.flush();
								System.out.println("sent file loc");
								int res= din.readInt();

								if(res==1)
									System.out.println("file registered successfully");

								if(peerReplication.equals("on"))
								{
									cli.uploadFile(peerreplicasocket,registerFileName,fileToRegister,replicaStoragel);
									System.out.println("sending duplcate data to server");
									dos.writeInt(2);
									dos.writeUTF(registerFileName);
									//dos.flush();
									System.out.println("sent file name");
									index=fileToRegister.lastIndexOf('/');
									Path =fileToRegister.substring(0,index);
									sendValue = replicaIp+"|"+replicaPort+"|"+replicaStoragel;
									dos.writeUTF(sendValue);
									//dos.flush();
									System.out.println("sent file loc of replica "+sendValue);
									res= din.readInt();

								}

								break;


								//deregister a file name		
							case 3:
								System.out.println("choose the file which you want to deregister");

								File file =chooseFileToBeRegistered();;
								dos.writeUTF(file.getName());

								//dos.flush();
								String  fileToDereg = file.getAbsolutePath();

								int index1=fileToDereg.lastIndexOf('/');
								String filePath =fileToDereg.substring(0,index1);
								String sendPath = clientIp+"|"+clientPort+"|"+filePath;
								dos.writeUTF(sendPath);
								
								dos.flush();
								int delres= din.readInt();
								if(delres==1)
									System.out.println("deleted successfully");
								//delete replica metadata
								if(peerReplication.equals("on"))
								{
									dos.writeInt(3);
									dos.writeUTF(file.getName());
									dos.flush();
									sendPath = replicaIp+"|"+replicaPort+"|"+replicaStoragel;
									dos.writeUTF(sendPath);
									delres= din.readInt();
									if(delres==1)
										System.out.println("deleted replica meta data successfully");
								}

								//dos.flush();
								break;


							case 4:
								exitcode=1;
								break;
							default:

								System.out.println("invalid choic entered");
								break;

							}	//end of switch
							long endTime = System.currentTimeMillis();
							long duration = (endTime - startTime);
							System.out.println("response time for the request!!!" + duration);

						}
						catch(IOException ex)

						{
							System.out.println("exception!!!"+ex.getMessage());
							System.out.println("primary server down connectng to replica server");
							String replicaserverIp= getvalueFromProperty("ReplicaServer_1_IP");
							int replicaserverPort = Integer.parseInt(getvalueFromProperty("ReplicaServer_1_Port").trim());
							s1 = new Socket(replicaserverIp,replicaserverPort);
						}
						//	din.close();
						//		dos.close();
						//s1.close();
					}//end of while
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

		System.out.println("Ip address of the client to be registered "+ Ip);
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
			System.out.println("into upload function");
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
				dos.close();

				System.out.println("file sent");
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
			System.out.println("Into download function");
			String fileDownloadLoc = getvalueFromProperty("fileDownloadLocation");
			String  fileloc = fileDownloadLoc+file_name;
			String[] strArr = locList;

			System.out.println("Enter index number the link from where you want to download the file");
			for(int i =0;i<strArr.length;i++)
			{
				System.out.println("Index:"+i+". "+strArr[i]);
			}
			Scanner sc_obj = new Scanner(System.in);
			int index_fileLink = sc_obj.nextInt();

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

			System.out.println("IP chosen "+ip);
			System.out.println("Port "+port);
			Socket socket = new Socket(ip, port);
			System.out.println(socket.isConnected());
			try {

				System.out.println("starting download of file "+file_name);


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
					System.out.println("File "+file_name+ " "+len+"bytes download completeat "+fileloc);
				} 

			}catch (Exception e) {
				System.out.println(" error in downloadingfile try another link");
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

			socket.close();  
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
}
