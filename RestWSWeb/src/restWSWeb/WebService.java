package restWSWeb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestScoped
@Path("")
@Produces({ "application/xml", "application/json" })
@Consumes({ "application/xml", "application/json" })
public class WebService {
	
	public Statement getStatement(){
		try {
			InitialContext cxt = new InitialContext();
			DataSource ds = (DataSource) cxt.lookup("java:/PostgresXADS");
			Connection connection = ds.getConnection();
			Statement statement = connection.createStatement();
			return statement;
				
		} catch (Exception e) {
			System.out.println("No db loaded");
			return null;
		}
	}
	
	/*POST A USER*/
	@POST
	@Path("/user")
	public Response createUser(User user){
		try {
			Statement st = getStatement();
			ResultSet rs = st.executeQuery("SELECT name FROM users "
					+ "WHERE name='" + user.getUserName() + "';");
			if (rs.isBeforeFirst())
				return Response.status(409).entity("Name already in use").build();
			
			st.executeUpdate("INSERT INTO users(name,password) VALUES("
							+ "'" + user.getUserName() + "'," 
							+ "'" + user.getPassword() + "');");
			return Response. status(201).build();
			
		} catch (SQLException e) {
			return Response.status(500).entity("Database ERROR").build();
		}
	}
	
	/*GET USERS REGISTERED*/ 
	@GET
	@Path("/users")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUsers(){	 
		try {
			Statement st = getStatement();
			ResultSet rs = st.executeQuery("SELECT name FROM users");
			List<String> users = new ArrayList<>();
			String name;
			while(rs.next()){		
				name = rs.getString("name");
				users.add(name);
			}
			st.close();
			return Response.status(200).entity(users).build();
			
		} catch (SQLException e) {
			return Response.status(500).entity("Database ERROR").build();
		}
	}
	
	/*GET USER BY NAME*/ 
	@GET
	@Path("/user/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(@PathParam("name") String name){	 
		try {
			Statement st = getStatement();
			ResultSet rs = st.executeQuery("SELECT name,password FROM users "
					+ "WHERE name='" + name + "';");
			User user = new User();
			
			if(!rs.isBeforeFirst())
				return Response.status(404).entity("User not found").build();
			else{
				rs.next();
				user.setUserName(rs.getString("name"));
				user.setPassword(rs.getString("password"));
			}
			
			st.close();
			return Response.status(200).entity(user).build();
			
		} catch (SQLException e) {
			return Response.status(500).entity("Database ERROR" + e.toString()).build();
		}
	}
	
	/*POST A VIDEO*/
	@POST
	@Path("/video")
	public Response createVideo(Video video){
		try {
			Statement st = getStatement();
			String id = UUID.randomUUID().toString();
			
			st.executeUpdate("INSERT INTO videos(id, title, description, uploader, server) VALUES("
							+ "'" + id + "'," 
							+ "'" + video.getTitle() +  "',"
							+ "'" + video.getDescription() + "',"
							+ "'" + video.getUploader() + "',"
							+ "'" + video.getServerAddress() + "');");
			return Response. status(201).entity(id).build();
			
		} catch (SQLException e) {
			return Response.status(500).entity("Database ERROR").build();
		}
	}
	
	/*GET ALL VIDEOS*/ 
	@GET
	@Path("/videos")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllVideos(){	 
		try {
			Statement st = getStatement();
			ResultSet rs = st.executeQuery("SELECT id, title, description, uploader, server FROM videos");
			List<Video> videos = new ArrayList<>();
			Video video;
			while(rs.next()){		
				video = new Video();
				video.setId(rs.getString("id"));
				video.setTitle(rs.getString("title"));
				video.setDescription(rs.getString("description"));
				video.setUploader(rs.getString("uploader"));
				video.setServerAddress(rs.getString("server"));
				videos.add(video);
			}
			st.close();
			return Response.status(200).entity(videos).build();
			
		} catch (SQLException e) {
			return Response.status(500).entity("Database ERROR").build();
		}
	}
	
	/*GET VIDEOS OF A USER*/ 
	@GET
	@Path("/videos/user/{user}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMyVideos(@PathParam("user") String user){	 
		try {
			Statement st = getStatement();
			ResultSet rs = st.executeQuery("SELECT id, title, description, uploader, server FROM videos "
										 + "WHERE uploader = '" + user + "';");
			List<Video> videos = new ArrayList<>();
			Video video;
			while(rs.next()){		
				video = new Video();
				video.setId(rs.getString("id"));
				video.setTitle(rs.getString("title"));
				video.setDescription(rs.getString("description"));
				video.setUploader(rs.getString("uploader"));
				video.setServerAddress(rs.getString("server"));
				videos.add(video);
			}
			st.close();
			return Response.status(200).entity(videos).build();
			
		} catch (SQLException e) {
			return Response.status(500).entity("Database ERROR").build();
		}
	}
	
	/*GET VIDEOS BY TAG*/ 
	@GET
	@Path("/videos/tag/{tag}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQueryVideos(@PathParam("tag") String tag){	 
		try {
			Statement st = getStatement();
			ResultSet rs = st.executeQuery("SELECT id, title, description, uploader, server FROM videos " + 
											"WHERE description LIKE CONCAT('%','" + tag +"','%') " +
											"OR title LIKE CONCAT('%', '" + tag + "','%');");
			List<Video> videos = new ArrayList<>();
			Video video;
			while(rs.next()){		
				video = new Video();
				video.setId(rs.getString("id"));
				video.setTitle(rs.getString("title"));
				video.setDescription(rs.getString("description"));
				video.setUploader(rs.getString("uploader"));
				video.setServerAddress(rs.getString("server"));
				videos.add(video);
			}
			st.close();
			return Response.status(200).entity(videos).build();
			
		} catch (SQLException e) {
			return Response.status(500).entity("Database ERROR").build();
		}
	}
	
	/*GET VIDEO BY ID*/ 
	@GET
	@Path("/video/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVideo(@PathParam("id") String id){	 
		try {
			Statement st = getStatement();
			ResultSet rs = st.executeQuery("SELECT title, description, uploader, server FROM videos "
					+ "WHERE id='" + id + "';");
			Video video = new Video();
			
			if(!rs.isBeforeFirst())
				return Response.status(404).entity("Video not found").build();
			else{
				rs.next();
				video.setId(id);
				video.setTitle(rs.getString("title"));
				video.setDescription(rs.getString("description"));
				video.setUploader(rs.getString("uploader"));
				video.setServerAddress(rs.getString("server"));
			}
			
			st.close();
			return Response.status(200).entity(video).build();
			
		} catch (SQLException e) {
			return Response.status(500).entity("Database ERROR" + e.toString()).build();
		}
	}
	
	/*DELETE VIDEO BY ID*/
	@DELETE
	@Path("/video/{id}/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteVideo(@PathParam("id") String id){
		try{
			Statement st = getStatement();
			st.executeUpdate("DELETE FROM videos WHERE id = '" + id + "';");
			st.close();
			return Response.status(204).build();
			
		}catch(SQLException ex){
			return Response.status(500).entity("Database ERROR").build();
		}
	}
	
	/*UPDATE VIDEO TITLE BY ID*/
	@PUT
	@Path("/video/{id}/edit/title")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateTitle(@PathParam("id") String id, String title){
		try{
			Statement st = getStatement();
			st.executeUpdate("UPDATE videos SET title = '" + title + "'"
					      + " WHERE id = '" + id + "';");
			st.close();
			return Response.status(204).build();
			
		}catch(SQLException ex){
			return Response.status(500).entity("Database ERROR").build();
		}
	}
	
	/*UPDATE VIDEO DESCRIPTION BY ID*/
	@PUT
	@Path("/video/{id}/edit/description")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateDescription(@PathParam("id") String id, String description){
		try{
			Statement st = getStatement();
			st.executeUpdate("UPDATE videos SET title = '" + description + "'"
					      + " WHERE id = '" + id + "';");
			st.close();
			return Response.status(204).build();
			
		}catch(SQLException ex){
			return Response.status(500).entity("Database ERROR").build();
		}
	}
	
}
