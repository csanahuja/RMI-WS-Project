package rmi;



import rmi.ClientInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;



/**
* This class implements the remote interfaceSomeInterface.
*/

public class ServerImpl extends UnicastRemoteObject implements ServerInterface{
        
	private String pathfile = "C:\\Users\\Public\\Videos\\";
	private String IPAddress;
	private String port;
	private String webService = "localhost:8080";
	
    public ServerImpl() throws RemoteException{
            super();
    }

    public void setPath(String Address){
    	this.IPAddress = Address;
    }
    
    public void setPort(String port){
    	this.port = port;
    }
    
    @Override
    public void registerClient(ClientInterface client, User user) throws RemoteException{
        int status = createClient(user);
        if (status == 0){
            client.sendMessage("Registered succesfull. User: " + user.getUserName());
            System.out.println("Registered user: " + user.getUserName());
        }
        if (status == 1)
           client.sendMessage("User: " + user.getUserName() + " already registered");
        if (status == 2)
           client.sendMessage("Register failed. Try again.");
    }
    
    /*Function returns 0 if new user was created successfully
     *         returns 1 if user name already in use
     * 		   returns 2 if creation failed 
     */        
    public int createClient(User user){
        try {
            URL url = new URL ("http://" +webService + "/RestWSWeb/rest/user");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            Gson g = new Gson();
            String input = g.toJson(user);

            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            int status = conn.getResponseCode();
            conn.disconnect();
            
            if(status != HttpURLConnection.HTTP_CREATED){ 
                if(status == 409)
                    return 1;
                return 2;
            }
            return 0;
            
        
        } catch (IOException ex) {
            System.out.println("No id recieved");
            return 2;
        }  
    }
    
    @Override
	public List<String> getUsers(){
		try {
			
			URL url = new URL ("http://" + webService + "/RestWSWeb/rest/users");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
		
			if(conn.getResponseCode() != 200)
				return new ArrayList<>();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output = br.readLine();
			conn.disconnect();
			
			Gson g = new Gson();
			String[] usersArray = g.fromJson(output, String[].class);
			List<String> users = new ArrayList<>(Arrays.asList(usersArray));

			return users;
			
		} catch (Exception e) { return new ArrayList<>(); }
	}
	
	public User getUser(String name){
		try {
			
			URL url = new URL ("http://" + webService + "/RestWSWeb/rest/user/" + name);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
		
			if(conn.getResponseCode() != 200)
				return null;
			
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output = br.readLine();
			conn.disconnect();
			
			Gson g = new Gson();
			User user = g.fromJson(output, User.class);
			
			return user;
					
		} catch (Exception e) { return null; }
	}

    @Override
    public String uploadVideo(ClientInterface client, Video video, User user, byte[] data) throws RemoteException {
        String id = null;
        
        User db_user = getUser(user.getUserName());
        if (db_user == null){
        	client.sendMessage("Client: " + user.getUserName() + " not registered");
        	return null;
        }
        
        //User registered with correct password
        if (db_user.getPassword().equals(user.getPassword())){
        
	        video.setUploader(user.getUserName());
	        video.setServerAddress(this.IPAddress + ":" + this.port);
	        System.out.println(video.getServerAddress());
	        id = createVideo(video); 
	        
	        if(id == null) return null;
	        
			Path path = Paths.get(pathfile + id + ".mp4");
			try {
				Files.write(path, data);
			} catch (IOException e) {
				System.out.println(e.toString());
			}
	
	        client.sendMessage("Video with title: '" + video.getTitle() + "' uploaded correctly");
	        System.out.println(("Video with title: " + video.getTitle() + " uploaded"));
	        return id;
        //Incorrect user password
        } else {
        	client.sendMessage("Incorrect password for Client: " + user.getUserName());
        	return null;
        }
    }
    
    /*Function returns the id if Video uploaded successfully
     * 		   returns null otherwise
     */
    public String createVideo(Video video){
        String id = null;
        try {
            URL url = new URL ("http://" + webService + "/RestWSWeb/rest/video");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            Gson g = new Gson();
            String input = g.toJson(video);
            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();
            
            int status = conn.getResponseCode();
            
            if(status != HttpURLConnection.HTTP_CREATED){ 
                return id;
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			id = br.readLine();
            conn.disconnect();
            
            return id;
            
        } catch (IOException e) {
            System.out.println(e.toString());
            return id;
        }  
    }
    
    public Video getVideo(String id){
    	try {
			
			URL url = new URL ("http://" + webService + "/RestWSWeb/rest/video/" + id);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
		
			if(conn.getResponseCode() != 200)
				return null;
			
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output = br.readLine();
			conn.disconnect();
			
			Gson g = new Gson();
			Video video = g.fromJson(output, Video.class);
			
			return video;
					
		} catch (Exception e) { return null; }
    }
    
    public boolean checkServer(String server, ClientInterface client, User user, String id) throws RemoteException{
    	String Address = IPAddress + ":" + port;
    	if (Address.equals(server)) return true;
    	return false;
    }
    
    public boolean checkVideo(String id, ClientInterface client, Video video) throws RemoteException{
    	//Video not in DB
    	if(video == null){
    		client.sendMessage("Video not found");
    		return false;
    	}
    	
    	//Video in DB
    	return true;   	
    }
    
    public boolean checkUser(ClientInterface client, User db_user, User user, Video video) throws RemoteException{
    	//User not registered
    	if (db_user == null){
        	client.sendMessage("Client: " + user.getUserName() + " not registered");
        	return false;
        
        //User registered with correct password
    	} else if (db_user.getPassword().equals(user.getPassword())){
    	        
	        	//User is the owner
	        	if(video.getUploader().equals(user.getUserName())){
	        		return true;
				} else
					client.sendMessage("You are not the owner of the Video");
	        		return false;
		        
	      //Incorrect user password
	      } else {
	    	  client.sendMessage("Incorrect password for Client: " + user.getUserName());
	    	  return false;
	      }		
    }
    
    public ServerInterface callRMIServer(ClientInterface client, Video video) throws RemoteException{
    	client.sendMessage("Video not in this server, delegating operation to: " + video.getServerAddress());
		System.out.println("Delegating delete operation to: " + video.getServerAddress());
		String registryURL = "rmi://" + video.getServerAddress() + "/mytube";
		try {
			return (ServerInterface) Naming.lookup(registryURL);
		} catch (Exception e) {
			client.sendMessage("Server not available, video not deleted");
			return null;
		}
    }
    
    /*Function logic can be traced with the comments*/
    @Override
    public void deleteVideo(ClientInterface client, User user, String id) throws RemoteException{

    	Video video = getVideo(id);
    	//Video exists in DB
    	if(checkVideo(id, client, video)){
    		
    		//Video physically located in this RMI Server
    		if(checkServer(video.getServerAddress(), client, user, id)){
    			
    			//User not registered
    			
    			User db_user = getUser(user.getUserName());
    			//Correct User
	        	if(checkUser(client, db_user, user, video)){
	        		
	        	}
	        		//Delete from DB
	        		int status = deleteVideo(id);
	        		if(status == 1)
	        			client.sendMessage("Error deleting video from DB, either does not exist"
	        					+ "or DB error");
		        
	        		//Delete physically if exits in path
	        		File f = new File(pathfile + id + ".mp4");
	        		if(f.isFile())
	        			f.delete();

	        		client.sendMessage("Video with id: " + id + " deleted");
    		        System.out.println(("Video with id: " + id + " deleted")); 
    		
    	    //Video located in other RMI Server
    		} else {
    			ServerInterface serverRMI = callRMIServer(client, video);
    			serverRMI.deleteVideo(client, user, id);
    		} //end checkServer
    	} //end checkVideo	      
    }
    
    public int deleteVideo(String id){
        try {
            URL url = new URL ("http://" +webService + "/RestWSWeb/video/" + id + "/delete");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Content-Type", "application/json");

            int status = conn.getResponseCode();
            conn.disconnect();
            
            if(status != 200){ 
                    return 1;
            }
            return 0;
            
        
        } catch (IOException ex) {
        	return 1;
        }  
    }
    
    
    @Override
    public void modifyTitle(ClientInterface client, User user, String id, String title) throws RemoteException{
    	
    	Video video = getVideo(id);
    	//Video exists in DB
    	if(checkVideo(id, client, video)){
    		
    		//User not registered
    			
			User db_user = getUser(user.getUserName());
			//Correct User
        	if(checkUser(client, db_user, user, video)){
        		
        	}
        		//Update from DB
        		updateField("title", title, id);

        		client.sendMessage("Video with id: " + id + " updated (title)");
		        System.out.println(("Video with id: " + id + " updated (title)")); 

    	} //end checkVideo	  
    }
    
    @Override
    public void modifyDescription(ClientInterface client, User user, String id, String description) throws RemoteException{
    	
    	Video video = getVideo(id);
    	//Video exists in DB
    	if(checkVideo(id, client, video)){
    		
    		
			//User not registered
			
			User db_user = getUser(user.getUserName());
			//Correct User
        	if(checkUser(client, db_user, user, video)){
        		
        	}
        		//Update from DB
    			updateField("description", description, id);

        		client.sendMessage("Video with id: " + id + " updated (description)");
		        System.out.println(("Video with id: " + id + " updated (description)")); 

    	} //end checkVideo	  
    }
    
    public int updateField(String field, String value, String id){
    	try {
            URL url = new URL ("http://" +webService + "/RestWSWeb/video/" + id + "/edit/" + field);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");

            OutputStream os = conn.getOutputStream();
            os.write(value.getBytes());
            os.flush();

            int status = conn.getResponseCode();
            conn.disconnect();
            
            if(status != 200){ 
                    return 1;
            }
            return 0;
            
        
        } catch (IOException ex) {
        	return 1;
        }  
    }

    
    @Override
    public void getVideoFileByKey(ClientInterface client, String id) throws RemoteException{
		
    	//Check if file allocated physically in this RMI Server
		File f = new File(pathfile + id + ".mp4");
		if(f.isFile()){
			byte[] data = null;
			
			try {
				data = Files.readAllBytes(f.toPath());
				client.sendVideoFile(data, id);
			} catch (IOException e) {
				System.out.println("Failed to load file");
				client.sendMessage("Failed to load file. File not send");
			}
		
		//Not in this server -> Query for video info and contact the server where file is	
		} else {
			Video video = getVideo(id);
			ServerInterface serverRMI = callRMIServer(client, video);
			serverRMI.getVideoFileByKey(client, id);
		}
    }
    
    
    @Override
    public List<Video> getMyVideos(ClientInterface client, User user) throws RemoteException{
		try {
			
			URL url = new URL ("http://" + webService + "/RestWSWeb/rest/videos/user/" + user.getUserName());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
		
			if(conn.getResponseCode() != 200)
				return new ArrayList<>();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output = br.readLine();
			conn.disconnect();
			
			Gson g = new Gson();
			Video[] videosArray = g.fromJson(output, Video[].class);
			List<Video> videos = new ArrayList<>(Arrays.asList(videosArray));

			System.out.println("Get My Videos query served");
			return videos;
			
		} catch (Exception e) { return new ArrayList<>(); }
    	
    }

    @Override
    public List<Video> getQueryVideos(ClientInterface client, String tag) throws RemoteException{
		try {
			
			URL url = new URL ("http://" + webService + "/RestWSWeb/rest/videos/tag/" + tag);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
		
			if(conn.getResponseCode() != 200)
				return new ArrayList<>();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output = br.readLine();
			conn.disconnect();
			
			Gson g = new Gson();
			Video[] videosArray = g.fromJson(output, Video[].class);
			List<Video> videos = new ArrayList<>(Arrays.asList(videosArray));

			System.out.println("Get Tag Videos query served");
			return videos;
			
		} catch (Exception e) { return new ArrayList<>(); }
    }
    
    @Override
    public List<Video> getAllVideos(ClientInterface client) throws RemoteException{
		try {
			
			URL url = new URL ("http://" + webService + "/RestWSWeb/rest/videos/");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
		
			if(conn.getResponseCode() != 200)
				return new ArrayList<>();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output = br.readLine();
			conn.disconnect();
			
			Gson g = new Gson();
			Video[] videosArray = g.fromJson(output, Video[].class);
			List<Video> videos = new ArrayList<>(Arrays.asList(videosArray));

			System.out.println("Get All Videos query served");
			return videos;
			
		} catch (Exception e) { return new ArrayList<>(); }
    }
    
    

}
