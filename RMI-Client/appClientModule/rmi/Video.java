package rmi;


import java.io.Serializable;

public class Video implements Serializable{
    private String title;
    private String description;
    private String uploader;
    private String serverAddress;
    private String id;

    public Video(){
    	
    }
    
    public Video(String title, String description) {
        this.title = title;
        this.description = description;
        this.uploader = "";
        this.serverAddress = "";
        this.id = "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }
    
    public void setServerAddress(String serverAddress){
    	this.serverAddress = serverAddress;
    }
    
    public void setId(String id){
    	this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUploader() {
        return uploader;
    }
    
    public String getServerAddress(){
    	return serverAddress;
    }
    
    public String getId(){
    	return id;
    }
    
    @Override
    public String toString(){
		return "Video: [ " + this.id + " , " + this.title + " , " + this.description +
					                   " , " + this.uploader + " , " + this.serverAddress + " ]";
    	
    }
}