package rmi;



import java.rmi.*;

public interface ClientInterface extends Remote {

    public void sendMessage(String msg) throws RemoteException;
    public void sendVideoFile(byte[] data, String id) throws RemoteException;
    
} 
