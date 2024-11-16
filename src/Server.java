import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static class ClientThread implements Runnable {
        // communication socket for this thread
        Socket clientSocket = null;
        // client's address
        String clientName = null;

        // constructor setting the attributes
        ClientThread(Socket socket) {
            clientSocket=socket;
            clientName=clientSocket.getInetAddress().toString() + ":" + clientSocket.getPort();
        }

        // main thread method
        @Override
        public void run() {
            // communication streams
            BufferedReader in = null;
            PrintWriter out = null;

            try {
                // get communication streams
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // buffer for client's data
                String line;
                // communication loop
                while ((line = in.readLine()) != null) {
                    System.out.println("read: "+line);
                    out.println(line);
                }

                System.out.println("Client " + clientName + " disconnected");
                // close everything and end the thread
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public static void main(String args[]) throws IOException {
        // main server socket
        ServerSocket echoServer = null;
        // communication socket
        Socket clientSocket = null;
        // open a server socket at port 9999
        try {
            System.out.println("Attempt to create a server socket");
            echoServer = new ServerSocket(9999);
            System.out.println("Socket created");
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }

        // waiting for a client
        while(true) {
            System.out.println("Waiting for accept");
            clientSocket = echoServer.accept();
            System.out.println("A client has connected:");
            InetAddress address = clientSocket.getInetAddress();
            int port = clientSocket.getPort();
            System.out.println("From address " + address.toString() + ":" + port);
            // create a new worker thread
            (new Thread(new ClientThread(clientSocket))).start();
        }
    }
}

