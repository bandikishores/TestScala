# TestScala

To run the main code (sample main program), run `sbt run`

## To run the Play Server

1) Switch Project 
   1) `project playServer`
2) Run
   1) `compile`
   2) `run`
   3) For custom port
      `run 9002`
3) To compile `sbt playServer`
4) To Build docker `sbt docker:publishLocal`
5) The Play Server is running at http://localhost:9001/
6) For WebSocket is running at http://localhost:9001/chat
7) To Run WebSocket client with a Test
   1) `WebSocketControllerSpec`
