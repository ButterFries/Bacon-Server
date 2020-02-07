package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.plaf.basic.BasicDirectoryModel;

import org.json.*;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ComputeBaconNumber implements HttpHandler
{
    private static Memory memory;
    private static final String kevinId = "nm0000102";

    public ComputeBaconNumber(Memory mem) {
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
    	
    	boolean badRequest = false;
    	
    	String body = Utils.convert(r.getRequestBody());
    	JSONObject deserialized = new JSONObject(body);
    	
        String actorId = memory.getValue();
       	
       	if (deserialized.has("actorId"))
            actorId = deserialized.getString("actorId");
        else
        	badRequest = true;
        
        
        if (!badRequest) {
        	Driver driver = GraphDatabase.driver("bolt://127.0.0.1:7687", AuthTokens.basic("neo4j", "1234"));
        	
        	boolean exist = false, path = false;
    		String distance = "0";
        	
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
        				+ ") RETURN length(p)";
        		
        		if(!kevinId.equals(actorId)) {
        			try (Session session = driver.session()) {
        				StatementResult result = session.run(transDistance);
        				
        				while (result.hasNext()) {
        					distance = result.next().values().get(0).toString();
        					path = true;
        				}
        			}
        		}
        		if(path || actorId.equals(kevinId)) {
        			
        			String output = "{\n\t\"baconNumber\": \""+Integer.parseInt(distance)/2+"\"\n}";
        			
        			String response = output;
        			r.sendResponseHeaders(200, response.length());
        			OutputStream os = r.getResponseBody();
        			os.write(response.getBytes());
        			os.close();
        		}
        		else {
        			String output = "{\n\t\"baconNumber\": \"undefined\"\n}";
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
