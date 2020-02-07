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

public class GetActor implements HttpHandler
{
    private static Memory memory;

    public GetActor(Memory mem) {
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
        String actorName = "";
        String actorId = memory.getValue();
        
        if (deserialized.has("actorId"))
            actorId = deserialized.getString("actorId");
        else
        	badRequest = true;
        
        if (!badRequest) {
        	Driver driver = GraphDatabase.driver("bolt://127.0.0.1:7687", AuthTokens.basic("neo4j", "1234"));
        	String transActor = "MATCH (a:actor {id:\""+actorId+"\"})" 
        					  + "RETURN a.Name";
        	
        	boolean exists = false;
        	
        	try (Session session = driver.session())
            {
        		StatementResult result = session.run(transActor);
        		if (result.hasNext()) {
        			actorName = result.next().values().get(0).toString();
        			exists = true;
        		}
            }
        	
        	if(exists) {
        		String transMovies = "MATCH(m:movie),(a:actor) "
        				+ "WHERE a.id=\""+actorId+"\" AND (a)-[:ACTED_IN]->(m)"
        				+ "RETURN m.id";
        		
        		ArrayList<String> movies = new ArrayList<String>();
        		try (Session session = driver.session()) {
        			StatementResult result = session.run(transMovies);
    				
        			while (result.hasNext()) {
        				movies.add(result.next().values().get(0).toString());
            		}
        		}
        		
        		String output = "{\n\t\"actorId\": \""+actorId+"\",\n\t"
        					  + "\"name\": "+actorName+",\n\t"
        					  + "\"movies\": [";
        		
        		Iterator<String> it = movies.iterator();
        		boolean foundMovies = false;
        		if(it.hasNext()) {
        			foundMovies = true;
        			output += "\n\t\t"+it.next();
        		}
        		while(it.hasNext()) {
        			output += ",\n\t\t"+it.next();
        		}
        		if (foundMovies)
        			output += "\n\t";
        		output += "]\n}";
        		
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
