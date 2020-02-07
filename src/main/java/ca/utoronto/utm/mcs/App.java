package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

public class App 
{
    static int PORT = 8080;
    public static void main(String[] args) throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
        
        Memory mem = new Memory();
        
        //PUT
        server.createContext("/api/v1/addActor", new AddActor(mem));
        server.createContext("/api/v1/addMovie", new AddMovie(mem));
        server.createContext("/api/v1/addRelationship", new AddRelationship(mem));
        
        //GET
        server.createContext("/api/v1/getActor", new GetActor(mem));
        server.createContext("/api/v1/getMovie", new GetMovie(mem));
        server.createContext("/api/v1/hasRelationship", new HasRelationship(mem));
        server.createContext("/api/v1/computeBaconNumber", new ComputeBaconNumber(mem));
        server.createContext("/api/v1/computeBaconPath", new ComputeBaconPath(mem));
        
        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
