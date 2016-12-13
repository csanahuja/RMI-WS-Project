package rmi;



import java.rmi.*;
import java.util.List;

public interface ServerInterface extends Remote {
    
	public void registerClient(ClientInterface client, User user) throws RemoteException;
    public String uploadVideo(ClientInterface client, Video video, User user, byte[] data) throws RemoteException;
    public List<String> getUsers() throws RemoteException;
    public void deleteVideo(ClientInterface client, User user, String id) throws RemoteException;
    public void modifyTitle(ClientInterface client, User user, String id, String title) throws RemoteException;
    public void modifyDescription(ClientInterface client, User user, String id, String description) throws RemoteException;
    public void getVideoFileByKey(ClientInterface client, String id) throws RemoteException;
    public List<Video> getMyVideos(ClientInterface client, User user) throws RemoteException;
    public List<Video> getQueryVideos(ClientInterface client, String tag) throws RemoteException;
    public List<Video> getAllVideos(ClientInterface client) throws RemoteException;
}

