package rmi;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.*;
import java.util.List;
import java.util.Scanner;



public class Client{

    public static User user = new User();
    public static String path = "C:\\Users\\Public\\Videos\\";
    public static ServerInterface server = null;
    public static ClientImpl client = null;
    
    public static void main(String args[]) {
    	lookupServer();
		try {
			
	        client = new ClientImpl();
	        procedure();
	        
		} catch (RemoteException e) {
			System.out.println("Remote Exception");
		}
    }
    
    public static void lookupServer() {

        //Default values
        String portNum = "55555";
        String IPAddress = "localhost";  

        //Set an IP Address
        String msg = "Enter the IP Address of the server. Alternative"
                + " press enter to use the default localhost."; 
        IPAddress = enterParameter(msg, IPAddress);

        //Set the port
        msg = "Enter the port of the server. Alternative"
                + " press enter to use the default port 55555.\nNote that "
                + "the port must be in range of 1024-65535.";

        portNum = enterParameter(msg, portNum);
        
        try {
	        String registryURL = "rmi://" + IPAddress + ":" + portNum + "/mytube";
	        ServerInterface serverRMI = (ServerInterface) Naming.lookup(registryURL);
	        server = serverRMI;
	        
        } catch (NotBoundException ex) {
            System.out.println("URL of the server not found");
        } catch (MalformedURLException ex) {
            System.out.println("Either bad IP address or port introduced");
        } catch (RemoteException ex) {
            System.out.println("Remote exception");
        }
    }
    
    public static String enterParameter(String msg, String default_value){
        Scanner scn = new Scanner (System.in);
        String aux;
        
        System.out.println(msg);
        
        aux = scn.nextLine();
        if(!aux.equals(""))
            return aux;
        return default_value;
    }
    
    public static void procedure() throws RemoteException{
        
        String msg = "-----------------------------------------------\n" + 
                "Select a method entering the corresponding number:\n" +
                " 0:  switch user \n" +
                " 1:  register \n" +
                " 2:  switch RMI server \n" +
                " 3:  upload video \n" +
                " 4:  delete video \n" +
                " 5:  modify video title \n" +
                " 6:  modify video description \n" +
                " 7:  get video file by key \n" +
                " 8:  get my videos info \n" +
                " 9:  get query videos info \n" +
                "10:  get all videos info \n" +
                "11:  close client \n" +
                "-----------------------------------------------\n";
        
        String number = "";
        int method = 0;
        
        loggin();
        
        while(true){
            number = enterParameter(msg, number);
            method = getNumber(number);
            switchMethod(method);
        }
    }
    
    public static void switchMethod(int method) throws RemoteException{
        switch (method){
            //set user
            case 0: loggin();
                    break;
            //register
            case 1:  register(); 
                     break;
            //switch server
            case 2:  lookupServer();
            		 break;
            //upload video         
            case 3:  upload();
                     break;
            //delete video         
            case 4:  delete();
                     break;
            //modify video title       
            case 5:  modifyTitle();
                     break;
            //modify video description     
            case 6:  modifyDescription();
                     break;
            //get video file by id     
            case 7:  getVideoFileById();
                     break;
            //get videos info of uploaded files      
            case 8:  getMyVideos();
                     break;
            //search video by tag
            case 9:  getQueryVideos();
                     break;
            //get my videos    
            case 10:  getAllVideos();
                     break;    
            //close client
            case 11: System.exit(0);
                     
            //invalid
            default: System.out.println("Invalid method selected");
                     break;
        }
    }
     
    public static void loggin(){
        String msg = "Enter the user (you will need to register on server if it is the first)"
        		+ " time you log with that user";
        String userName = null;
        while (userName == null)
            userName = enterParameter(msg, userName);
        
        String password = null;
        msg = "Enter a password";
        while (password == null)
            password = enterParameter(msg, password);
        
        user = new User(userName,password);
        System.out.println("logged as:" + user.getUserName());
    }
    
    public static int getNumber(String str){
        int number;   
        try {
            number = Integer.parseInt(str);
        } catch (NumberFormatException ex){
            number = -1;
        }
        return number;
    }
    
    public static void register() throws RemoteException{
        server.registerClient(client, user);
    }
    
    public static void upload() throws RemoteException{
        String msg = "Enter a title to the video";
        String title = "Unspecified title";
        String file = "";
        title = enterParameter(msg, title);
        
        msg = "Enter a description to the video";
        String description = "Default description";
        description = enterParameter(msg, description);
        
        msg = "Enter the name of the file including the extension";
        file = enterParameter(msg, "");
        
        try {
        	byte[] data = Files.readAllBytes(new File(path + file).toPath());
			
			Video video = new Video(title, description);
	        String id = server.uploadVideo(client, video, user, data);
			
		} catch (IOException e) {
			System.out.println("File not found" + e.toString());
		}


    
    }
    
    public static void delete() throws RemoteException{
        String id = selectFilmId("Select id film (Update with method 8",client.getMyVideos());
        
        if (id.equals(""))
            System.out.println("Delete canceled"); 
        else 
            server.deleteVideo(client, user, id);
    }
    
    public static void modifyTitle() throws RemoteException{
        String msg = "Enter the new title to the video";
        String title = "Unspecified title";
        title = enterParameter(msg, title);
        
        String id = selectFilmId("Select id film (Update with method 8",client.getMyVideos());

        if (id.equals(""))
            System.out.println("Modification canceled"); 
        else 
            server.modifyTitle(client, user, id, title);

    }
    
    public static void modifyDescription() throws RemoteException{
        String msg = "Enter the new description to the video";
        String description = "Default description";
        description = enterParameter(msg, description);
        
        String id = selectFilmId("Select id film (Update with method 8",client.getMyVideos());
        
        if (id.equals(""))
            System.out.println("Modification canceled"); 
        else 
            server.modifyDescription(client, user, id, description);

    }
    
    public static void getVideoFileById() throws RemoteException{
        String id = selectTypeOfFilmId();
        if (id.equals("")){
            System.out.println("Selection canceled"); 
        }
        else {
            server.getVideoFileByKey(client, id);
        }
    }
    
    public static void getMyVideos() throws RemoteException{
    	client.setMyVideos(server.getMyVideos(client, user));
        System.out.println("Updated my videos list");
    }    
    
    public static void getQueryVideos() throws RemoteException{
        String msg = "Enter the keyword to search videos";
        String key_word = "";
        key_word = enterParameter(msg, key_word);
        
        client.setQueryVideos(server.getQueryVideos(client, key_word));
        System.out.println("Updated query videos list with tag: " + key_word);   
    }                     
    
    public static void getAllVideos() throws RemoteException{
    	client.setAllVideos(server.getAllVideos(client));
        System.out.println("Updated all videos list");   
    }
    
    public static String selectFilmId(String msg, List<Video> videos){

        String number = "";
        msg = showList(msg, videos);
        number = enterParameter(msg,number);
        
        int index_id = getNumber(number);
        if (index_id == -1) index_id = 0;
        String id;
        
        if (index_id != 0 && index_id > 0 && index_id <= videos.size())
            id = videos.get(index_id-1).getId();
        else
            id = "";
        
        return id;
    }
    
    public static String selectTypeOfFilmId(){

    	String msg = "Type the number to select ids to display:\n" +
    			     "1 - To display your videos id (Update with method 8)\n" +
    			     "2 - To display videos ids of last query by tag (Update with method 9)\n" +
    			     "3 - To display all videos ids (Update with method 10)\n" +
    			     "To cancel introduce anything else";
    	
        String str_number = "";
        str_number = enterParameter(msg,str_number);
        int number = getNumber(str_number);
        
        String id = "";
        msg = "Select Video of the list";
        if(number == 1)
        	id = selectFilmId(msg, client.getMyVideos());
        if(number == 2)
        	id = selectFilmId(msg, client.getQueryVideos());
        if(number == 3)
        	id = selectFilmId(msg, client.getAllVideos());
        return id;

    }
    
    public static String showList(String msg, List<Video> videos){
    	msg = msg + "\n";
    	msg = msg + "ID - Type the number of the video\n";
    	if(videos.isEmpty()) msg = msg + "ALERT: No videos in this list, press Enter";
    	
    	int id = 1;
    	for(Video v: videos){
    		msg = msg + id + " - " + v.toString() +"\n";
    		id++;
    	}
    	return msg;
    }
    
    public static void debug(){
    	System.out.println(client.getMyVideos().toString());
    }
}
