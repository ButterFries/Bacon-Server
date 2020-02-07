package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AddRelationship implements HttpHandler
{
    private static Memory memory;

    public AddRelationship(Memory mem) {
        memory = mem;
    }

    public void handle(HttpExchange r) throws IOException {
        try {
            if (r.getRequestMethod().equals("PUT")) {
                handlePut(r);
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

    public void handlePut(HttpExchange r) throws IOException, JSONException {
    	
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

        	String transCheck = "MATCH (a:actor {id:\""+actorId+"\"})-[r:ACTED_IN]-(m:movie {id:\""+movieId+"\"})" 
        					  + "RETURN a";
        	
        	boolean exists = false;
        	
        	try (Session session = driver.session())
            {
        		StatementResult result = session.run(transCheck);
        		exists = result.hasNext();
            }
        	
        	if(!exists) {
        		String trans = "MATCH(m:movie),(a:actor) "
        				+ "WHERE m.id=\""+movieId+"\" AND a.id=\""+actorId+"\""
        				+ "CREATE (a)-[name:ACTED_IN]->(m)";
        	
        		try (Session session = driver.session()) {
        			session.run(trans);
        		}
        	}
        	String response = "";
        	r.sendResponseHeaders(200, response.length());
        	OutputStream os = r.getResponseBody();
        	os.write(response.getBytes());
        	os.close();
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
