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

public class GetMovie implements HttpHandler
{
    private static Memory memory;

    public GetMovie(Memory mem) {
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
        String movieName = "";
        String movieId = memory.getValue();
        
        if (deserialized.has("movieId"))
            movieId = deserialized.getString("movieId");
        else
        	badRequest = true;
        
        if (!badRequest) {
        	Driver driver = GraphDatabase.driver("bolt://127.0.0.1:7687", AuthTokens.basic("neo4j", "1234"));
        	String transMovie = "MATCH (m:movie {id:\""+movieId+"\"})" 
        					  + "RETURN m.Name";
        	
        	boolean exists = false;
        	
        	try (Session session = driver.session())
            {
        		StatementResult result = session.run(transMovie);
        		if (result.hasNext()) {
        			movieName = result.next().values().get(0).toString();
        			exists = true;
        		}
            }
        	
        	if(exists) {
        		String transActors = "MATCH(m:movie),(a:actor) "
        				+ "WHERE m.id=\""+movieId+"\" AND (a)-[:ACTED_IN]->(m)"
        				+ "RETURN a.id";
        		
        		ArrayList<String> actors = new ArrayList<String>();
        		try (Session session = driver.session()) {
        			StatementResult result = session.run(transActors);
    				
        			while (result.hasNext()) {
        				actors.add(result.next().values().get(0).toString());
            		}
        		}
        		
        		String output = "{\n\t\"movieId\": \""+movieId+"\",\n\t"
        					  + "\"name\": "+movieName+",\n\t"
        					  + "\"actors\": [";
        		
        		Iterator<String> it = actors.iterator();
        		boolean foundActors = false;
        		if(it.hasNext()) {
        			foundActors = true;
        			output += "\n\t\t"+it.next();
        		}
        		while(it.hasNext()) {
        			output += ",\n\t\t"+it.next();
        		}
        		if (foundActors)
        			output += "\n\t";
        		output += "]\n}";
        		        		
        		
        		String response = ""+output;
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
