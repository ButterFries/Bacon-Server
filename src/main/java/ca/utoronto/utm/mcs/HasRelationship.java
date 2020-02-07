package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.*;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HasRelationship implements HttpHandler
{
    private static Memory memory;

    public HasRelationship(Memory mem) {
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
        String movieId = memory.getValue();
        
        if (deserialized.has("actorId"))
            actorId = deserialized.getString("actorId");
        else
        	badRequest = true;
        if (deserialized.has("movieId"))
            movieId = deserialized.getString("movieId");
        else
        	badRequest = true;
        
        
        if (!badRequest) {
        	Driver driver = GraphDatabase.driver("bolt://127.0.0.1:7687", AuthTokens.basic("neo4j", "1234"));
        	
        	boolean relationship = false, exist = false;
    		
        	String transExists = "MATCH(a:actor),(m:movie) "
    				+ "WHERE a.id=\""+actorId+"\" AND m.id=\""+movieId+"\""
    				+ "RETURN a.id";
        	
        	try (Session session = driver.session()) {
    			StatementResult result = session.run(transExists);
				
    			if (result.hasNext()) {
    				exist = true;
        		}
    		}
        	
        	if (exist) {
        		String transRelationship = "MATCH(m:movie),(a:actor) "
        				+ "WHERE m.id=\""+movieId+"\" AND a.id=\""+actorId+"\" AND (a)-[:ACTED_IN]->(m)"
        				+ "RETURN a.id";
        		
        		try (Session session = driver.session()) {
        			StatementResult result = session.run(transRelationship);
        			
        			if (result.hasNext()) {
        				relationship = true;
        			}
        		}
        		
        		String output = "{\n\t\"actorId\": \""+actorId+"\",\n\t"
        				+ "\"movieId\": \""+movieId+"\",\n\t"
        				+ "\"hasRelationship\": "+relationship+"\n}";
        		
        		
        		String response = output;
        		r.sendResponseHeaders(200, response.length());
        		OutputStream os = r.getResponseBody();
        		os.write(response.getBytes());
        		os.close();
        	}
        	else {
        		String response = "";
        		r.sendResponseHeaders(404, response.length());
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
