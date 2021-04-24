"# distributed-content-sharing-app" 
compile the project
1. BootstrapServer.java
2. DistributedNode.java
3. Client.java

run below commands from the root 

1. javac bootstrap_server/BootstrapServer.java
2. javac file_transfering/DistributedNode.java
3. javac client/Client.java

To run the app,
1. first run server in a seperate CLI
    java bootstrap_server.BootstrapServer
2. Then run 2 sepearate CLI for each node u want to create
    1. java file_transfering.DistributedNode  -port=<PORT> -server=127.0.0.1:55555
    2. java client.Client -node=127.0.0.1:<PORT>

    if you want to create more nodes just run another 2 CLIs with above commands,

Then follow the instructions in the CLI to Search and Download the files

