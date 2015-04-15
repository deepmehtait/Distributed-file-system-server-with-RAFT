package poke.ftp;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPCommand;
import org.apache.commons.net.ftp.FTPReply;
import org.jibble.simpleftp.SimpleFTP;

public class ftpConnection {
	String server = "192.168.0.9";
	int port = 21;
	String user = "ftp";
    String pass = "ftp";
    FTPClient ftpClient;
    SimpleFTP ftp;
    public ftpConnection() {
        ftpClient = new FTPClient();
	}
    
    public void connect(){
	    try {
	        ftpClient.connect(server, port);
	        showServerReply();
	        int replyCode = ftpClient.getReplyCode();
	        if (!FTPReply.isPositiveCompletion(replyCode)) {
	            System.out.println("Operation failed. Server reply code: " + replyCode);
	            return;
	        }
	        boolean success = ftpClient.login(user, pass);
	        showServerReply();
	        if (!success) {
	            System.out.println("Could not login to the server");
	            return;
	        } else {
	            System.out.println("LOGGED IN SERVER");
	        }
	         ftp = new SimpleFTP();
		    ftp.connect(server, port, user, pass);
	    } 
	    catch (IOException ex) {
	        System.out.println("Oops! Something wrong happened");
	        ex.printStackTrace();
    	}
    }
    
    private void showServerReply() {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                System.out.println("SERVER: " + aReply);
            }
        }
    }
    
    public void uploadFile(InputStream inputStream){
    	try{	
    		
    		 // Set binary mode.
		    ftp.bin();
		    
		    // Change to a new working directory on the FTP server.
		    ftp.cwd("images");
		    System.out.println("changed dir");
		    ftp.stor(inputStream, "test.jpg");
		    ftp.disconnect();
    	}
    	catch(IOException e){
    		e.printStackTrace();
    	}
    		 
    		
    		
			/*ftpClient.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
       	    //ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
			
    		ftpClient.enterLocalPassiveMode();
			//ftpClient.setBufferSize(1024);
	        String firstRemoteFile = "images/mobile.jpg";
	        System.out.println("Start uploading first file");
	        boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
	        System.out.println("Upload reply code " + ftpClient.getReplyString());
	        ftpClient.sendCommand(FTPCommand.CWD, "images");
	        System.out.println("Change working directory " + ftpClient.getReplyString());
	        ftpClient.sendSiteCommand("chmod " + "666" + " hello ");
	        System.out.println("check permissions" + ftpClient.getReplyString());
	        inputStream.close();
	        if (done) {
	            System.out.println("The file is uploaded successfully.");
	        }
	        else
	        {
	        	System.out.println("The file is not uploaded successfully");
	        }
    	}
    	catch (IOException e){
    		System.out.println("Error: " + e.getMessage());
            e.printStackTrace();	
    	}
    	finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }
    
    public InputStream retrieveImage()
    {
    	InputStream inputStream = null;
		try{
			  System.out.println("In Retriev Image");
	    	  ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
/*	    	  ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
	    	  ftpClient.setBufferSize(1024);
*/			  ftpClient.enterLocalPassiveMode();
			  System.out.println("Client set");
			    ftpClient.cwd("images");
			  String remoteFile2 = "test.jpg";
			  inputStream = ftpClient.retrieveFileStream(remoteFile2);
			  System.out.println("Input Stream " + inputStream);
		      boolean success = ftpClient.completePendingCommand();
	          if (success) {
	              System.out.println("File #2 has been downloaded successfully.");
	          }
	          else
	        	  System.out.println("Failure could not download");
	    }
    	catch (IOException e) {
            e.printStackTrace();
        }
    	finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        }
		return inputStream;
    }
}
