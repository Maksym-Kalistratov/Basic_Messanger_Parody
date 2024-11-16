import java.net.*;
import java.io.*;

public class Client {
    public static void main(String[] args) throws IOException {
        // communication socket
        Socket echoSocket = null;
        // communication streams
        PrintWriter out = null;
        BufferedReader in = null;
        // server name
        String hostname = "localhost";
        if(args.length > 0) hostname=args[0];

        try {
            System.out.println("Trying to connect with " + hostname);
            echoSocket = new Socket(hostname, 9999);
            System.out.println("Creating communication streams");
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + hostname + ".");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Connection error with " + hostname + ".");
            System.exit(1);
        }

        // keyboard read buffer
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        // user's input
        String userInput;
        // in a loop - read from the keyboard
        while ((userInput = stdIn.readLine()) != null) {
            // send to the server
            out.println(userInput);
            // read the response and print it out
            System.out.println("echo: " + in.readLine());
        }

        // end the job
        out.close();
        in.close();
        stdIn.close();
        echoSocket.close();
    }
}
