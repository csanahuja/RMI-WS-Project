package rmi;


import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Scanner;



public class Server{

	private static String portNum;
	private static String IPAddress;
	
    public static void main(String args[]){
        try{            
            bindServer();
        }catch(RemoteException ex) {
            System.out.println("Mytube Server not ready.");
        } catch (MalformedURLException ex) {
            System.out.println("Either bad IP address or port introduced");
        }
    }

    private static void startRegistry(int RMIPortNum) throws RemoteException{
        try {
            Registry registry = LocateRegistry.getRegistry(RMIPortNum);
            registry.list();

        }catch(RemoteException ex) {
            System.out.println("RMI registry cannot be located at port " +RMIPortNum);
            LocateRegistry.createRegistry(RMIPortNum);
            System.out.println("RMI registry created at port " + RMIPortNum);
        }
    } 
    
    public static void bindServer() throws MalformedURLException, RemoteException{

        //Default values
        portNum = "55555";
        IPAddress = "localhost";  

        //Set an IP Address
        String msg = "Enter the IP Address of the server. Alternative"
                + " press enter to use the default localhost."; 
        IPAddress = enterParameter(msg, IPAddress);

        //Set the port
        msg = "Enter the port of the server. Alternative"
                + " press enter to use the default port 55555. \nNote that "
                + "the port must be in range of 1024-65535.";

        portNum = enterParameter(msg, portNum);
        
        ServerImpl exportedObj = new ServerImpl();
        exportedObj.setPath(IPAddress);
        exportedObj.setPort(portNum);
        startRegistry(Integer.parseInt(portNum));
            
        String registryURL = "rmi://" + IPAddress + ":" + portNum + "/mytube";
        Naming.rebind(registryURL, exportedObj);
        System.out.println("Mytube Server ready.");
        
        //Shows the registered services on portNum 
        //Registry registry = LocateRegistry.getRegistry(Integer.parseInt(portNum));
        //System.out.println(Arrays.toString(registry.list()));
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
}
