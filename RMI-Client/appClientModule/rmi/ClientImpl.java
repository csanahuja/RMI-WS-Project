package rmi;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


public class ClientImpl extends UnicastRemoteObject implements ClientInterface{
    
    private List<Video> my_videos;
    private List<Video> query_videos;
    private List<Video> all_videos;
    
    public static String pathfile = "C:\\Users\\Public\\Videos\\";

    
    public ClientImpl() throws RemoteException{
        super();
        my_videos = new ArrayList<>();
        query_videos = new ArrayList<>();
        all_videos = new ArrayList<>();
    }

    @Override
    public void sendMessage(String msg) throws RemoteException{
        System.out.println(msg);
    }
    
    @Override
    public void sendVideoFile(byte[] data, String id) throws RemoteException{
    	Path path = Paths.get(pathfile + id + ".mp4");
		try {
			Files.write(path, data);
			System.out.println("File saved in the path");
		} catch (IOException e) {
			System.out.println(e.toString());
		}
    }

    public List<Video> getMyVideos() {
        return my_videos;
    }
    
    public List<Video> getQueryVideos() {
    	return query_videos;
    }
    
    public List<Video> getAllVideos() {
    	return all_videos;
    }
    
    public void setMyVideos(List<Video> videos){
    	my_videos = videos;
    }
    
    public void setQueryVideos(List<Video> videos){
    	query_videos = videos;
    }
    
    public void setAllVideos(List<Video> videos){
    	all_videos = videos;
    }

}
