package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.*;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ComputeBaconPath implements HttpHandler
{
    private static Memory memory;
    private static final String kevinId = "nm0000102";

    public ComputeBaconPath(Memory mem) {
        memory = mem;
    }

    public void handle(HttpExchange r) throws IOException {
        try {
            if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
            }
            else {
            	String response = "";
            	r.sendResponseHeaders(404, response.length());
            	OutputStream os = r.getResponseBody();
            	os.write(response.getBytes());
            	os.close();
            }
        } catch (JSONException e) {
        	String response = "";
        	r.sendResponseHeaders(400, response.length());
        	OutputStream os = r.getResponseBody();
        	os.write(response.getBytes());
        	os.close();
        
        } catch (Exception e) {
        	String response = "";
        	r.sendResponseHeaders(500, response.length());
        	OutputStream os = r.getResponseBody();
        	os.write(response.getBytes());
        	os.close();
        }
    }

    public void handleGet(HttpExchange r) throws IOException, JSONException {
    	
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        boolean badRequest = false;
        String actorId = memory.getValue();
        
        if (deserialized.has("actorId"))
            actorId = deserialized.getString("actorId");
        else
        	badRequest = true;
        
        
        if (!badRequest) {
        	Driver driver = GraphDatabase.driver("bolt://127.0.0.1:7687", AuthTokens.basic("neo4j", "1234"));
        	
        	boolean exist = false, path = false;
    		ArrayList<String> actorList = new ArrayList<>();
    		ArrayList<String> movieList = new ArrayList<>();
        	
        	String transExists = "MATCH(a:actor) "
    				+ "WHERE a.id=\""+actorId+"\""
    				+ "RETURN a.id";
        	
        	try (Session session = driver.session()) {
    			StatementResult result = session.run(transExists);
				
    			if (result.hasNext()) {
    				exist = true;
        		}
    		}
        	
        	if (exist) {
        		String transDistance = "MATCH p=shortestPath("
        				+ "(b:actor {id:\""+kevinId+"\"})-[*]-(a:actor {id:\""+actorId+"\"})"
        				+ ") UNWIND nodes(p) as n RETURN n.id";
        		
        		if(!kevinId.equals(actorId)) {
        			try (Session session = driver.session()) {
        				StatementResult result = session.run(transDistance);
        				
        				if (result.hasNext()) {
        					path = true;
        					actorList.add(result.next().toString());
        				}
        				while (result.hasNext()) {
        					movieList.add(result.next().values().get(0).toString());
        					actorList.add(result.next().values().get(0).toString());
        					
        				}
        				actorList.set(0, "\""+kevinId+"\"");
        			}
        		}
        		if(path || actorId.equals(kevinId)) {
        			String output = "{\n\t\"baconNumber\": \""+movieList.size()+"\",\n\t\"baconPath\":[";//MAYBE NO COMMA
        			
        			if(actorId.equals(kevinId)) {//CHANGE THIS TO RANDOM MOVIE BACON IS IN
        				
        				String transMovie = "MATCH(a:actor),(m:movie) "
        	    				+ "WHERE a.id=\""+kevinId+"\" AND (a)-[:ACTED_IN]-(m)"
        	    				+ "RETURN m.id";
        				String movieActed = "";
        				
        				try (Session session = driver.session()) {
            				StatementResult result = session.run(transMovie);
            				
            				if (result.hasNext()) {
            					movieActed = result.next().get(0).toString();
            				}
            			}
        				
        				output += "\n\t\t{\n"
    							+ "\t\t\t\"actorId\": \""+kevinId+"\",\n" //ACTOR2, MOVIE1
    							+ "\t\t\t\"movieId\": "+movieActed+"\n"
    							+ "\t\t}\n\t]\n}";
        			}
        			else {
        				for (int i = movieList.size()-1; i >= 0; i--) {
        					output += "\n\t\t{\n"
        							+ "\t\t\t\"actorId\": "+actorList.get(i+1)+",\n" //ACTOR->MOVIE<-ACTOR
        							+ "\t\t\t\"movieId\": "+movieList.get(i)+"\n"  //ACTOR1, MOVIE1
        							+ "\t\t},";
        					output += "\n\t\t{\n"
        							+ "\t\t\t\"actorId\": "+actorList.get(i)+",\n" //ACTOR2, MOVIE1
        							+ "\t\t\t\"movieId\": "+movieList.get(i)+"\n"
        							+ "\t\t}";
        					if(i > 1)
        						output += ",";
        				}
        				output += "\n\t]\n}";
        			}
        			
        			String response = output;
        			r.sendResponseHeaders(200, response.length());
        			OutputStream os = r.getResponseBody();
        			os.write(response.getBytes());
        			os.close();
        		}
        		else {
        			String output = "{\n\t\"baconNumber\": \"undefined\"\n\t\"baconPath\":[]\n}";//TODO CHANGE
        			String response = output;
        			r.sendResponseHeaders(200, response.length());
        			OutputStream os = r.getResponseBody();
        			os.write(response.getBytes());
        			os.close();
        		}
        	}
        	else {
        		String response = "";
        		r.sendResponseHeaders(400, response.length());
        		OutputStream os = r.getResponseBody();
        		os.write(response.getBytes());
        		os.close();
        	}
        }
        else {
        	String response = "";
        	r.sendResponseHeaders(400, response.length());
        	OutputStream os = r.getResponseBody();
        	os.write(response.getBytes());
        	os.close();		
        }
    }
}
