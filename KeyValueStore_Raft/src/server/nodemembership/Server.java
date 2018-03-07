package server.nodemembership;


public class Server {

    protected String ipAddress;
    protected int port;
    public Server(String ipAddress, int port) {
        
        this.ipAddress = ipAddress;
        this.port = port;
    }
    
    public String getIpAddress() {
        
        return ipAddress;
    }
    
    public int getPort() {
        
        return port;
    }
}
