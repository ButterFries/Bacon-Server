**BaconPath Server**

A java server which can determine the distance any actor is from Kevin Bacon, using HTTP protocol and neo4j 3.5.
By default on "http://localhost:8080" and the server credentials are username: "neo4j" and password: "1234"  
  
The neo4j database consists of two nodes: actor and movie  
actor has properties:  
"id", "Name"  
movie has properties:  
"id", "Name"  
Where the id property is unique  
  
actor nodes can have a relationship "ACTED_IN" with movies  
(actor)-[ACTED_IN]->(movie)  


  
  
Commands:  
**PUT http://localhost:8080/api/v1/addMovie**  
     Command body:  
     ```
     {  
          "name": string,  
          "actorId": "string  
     }  
     ```
     Return body:  
          ```N/A``` 
  
**PUT http://localhost:8080/api/v1/addActor**  
     Command body:  
     ```
     {  
          "name": string,  
          "movieId": "string  
     }  
     ```
     Return body:  
          ```N/A```  
  
**PUT http://localhost:8080/api/v1/addRelationship**  
     Command body:  
     ```
     {  
          "actorId": string,  
          "movieId": "string  
     }  
     ```
     Return body:  
          ```N/A```  

  
  
  

**GET http://localhost:8080/api/v1/getMovie**  
     Command body:  
     ```
     {  
          "movieId": string  
     }  
     ```
     Return body:  
     ```
     {  
          "movieId": string,  
          "name": string,  
          "actors": [  
               string,  
               string,  
               ...  
          ]  
     }  
     ```
  
**GET http://localhost:8080/api/v1/getActor**  
     Command body:  
     ```
     {  
          "actorId": string  
     }  
     ```
     Return body:  
     ```
     {  
          "actorId": string,  
          "name": string,  
          "movies": [  
               string,  
               string,  
               ...  
          ]  
     }  
     ```
  
**GET http://localhost:8080/api/v1/hasRelationship**  
     Command body:  
     ```
     {  
          "actorId": string,  
          "movieId": string  
     }  
     ```
     Return body:  
          ```N/A```  
  
**GET http://localhost:8080/api/v1/computeBaconNumber**  
     Command body:  
     ```
     {  
          "actorId": string  
     }  
     Return body:  
     {  
          "baconNumber": string  
     }  
     ```

**GET http://localhost:8080/api/v1/computeBaconPath**  
     Command body:  
     ```
     {  
          "actorId": string  
     }  
     ```
     Return body:  
     ```
     {  
          "baconNumber": string,  
          "baconPath": [  
               {  
                    "actorId": string,  
                    "movieId": string  
               },  
               {  
                    "actorId": string,  
                    "movieId": string  
               },  
               ...  
          ]  
     }  
     ```
  
  
Created by: Adrian Czarnecki
