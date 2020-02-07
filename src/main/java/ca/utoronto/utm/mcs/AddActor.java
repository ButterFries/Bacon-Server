package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AddActor implements HttpHandler
{
    private static Memory memory;

    public AddActor(Memory mem) {
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
        
        boolean badRequest = false, alreadyExists = false;
        String name = memory.getValue();
        String actorId = memory.getValue();

        if (deserialized.has("name"))
            name = deserialized.getString("name");
        else
        	badRequest = true;

        if (deserialized.has("actorId"))
            actorId = deserialized.getString("actorId");
        else
        	badRequest = true;
        
        if (!badRequest) {
        	Driver driver = GraphDatabase.driver("bolt://127.0.0.1:7687", AuthTokens.basic("neo4j", "1234"));
        	
        	
        	String transCheck = "MATCH (a:actor {id:\""+actorId+"\"})" 
					  + "RETURN a.Name";
        	
        	try (Session session = driver.session()) {
        		StatementResult result = session.run(transCheck);
        		if (result.hasNext()) {
        			alreadyExists = true;
        		}
        	}
        	
        	if(!alreadyExists) {
        		String trans = "CREATE (a:actor {Name: \""+name+"\", id: \""+actorId+"\"})";
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
