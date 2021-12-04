package ca.camosun.ICS226;
import java.net.*;
import java.nio.channels.*;
import java.io.*;
import java.util.HashMap;

public class App 
{
    protected int port;
    protected String response = "";
    protected final String ERROR_RESPONSE = "NO";
    protected final int KEY_SIZE = 8;
    protected final int MAX_MSG_SIZE = 160;
    HashMap<String, String> stored = new HashMap<String, String>();

    public App(int port)
    {
        this.port = port;
    }

    // take the input received from the client and
    // figure out if it starts with PUT, GET, or  
    // neither, then call the appropriate function
    //
    // inputLine - the input returned from the client as a String
    //
    // return   - "" if the input starts with GET or PUT
    //          - var ERROR_RESPONSE if the input starts with neither

    public void processLine(String inputLine) {
        if (inputLine.startsWith("PUT")) 
        {
            processPut(inputLine.substring(3));
            
        }
        else if (inputLine.startsWith("GET")) 
        {
            processGet(inputLine.substring(3));
        }
        else 
        {
            // System.out.println("DEBUG: line doesn't start with PUT or GET");
            response = ERROR_RESPONSE;
        }
    }

    // given a string, extract the key from it. If the key
    // is blank, set the response to "NO" and return from the function.
    // otherwise, extract the message from the rest of the string.
    // if the message is longer than 160 characters, set the response
    // to "NO" and return from the function.
    // if key and message length are both okay, add the key and message to
    // the hashmap stored.
    //
    // inputLine - the input returned from the client as a String

    public void processPut(String inputLine) 
    {
        String key = getKey(inputLine);
        // if key is empty
        if (key == "") 
        {
            // System.out.println("DEBUG: key is blank");
            response = ERROR_RESPONSE;  
            return; 
        }

        String message = getMessage(inputLine);
        // System.out.println("DEBUG: Saving " + message + " with key " + key);
        if (message.length() <= MAX_MSG_SIZE)
        {
            stored.put(key, message);
            response = "OK";
        }
        else 
        {
            response = ERROR_RESPONSE;
        }
    }

    // given a string, extract the key from it. If the key is
    // blank, return from the function. Otherwise, set the 
    // response to the corresponding message from the hashmap stored.
    // If no corresponding message exists, override the null returned with
    // a blank. 
    //
    // inputLine - the input returned from the client as a String

    public void processGet(String inputLine) 
    {
        String key = getKey(inputLine);
        // System.out.println("DEBUG: " + key + " received from GET command");

        if (key == "")
        {
            return;
        }
        // System.out.println("DEBUG: message gotten from key");
        response = stored.get(key);
        if (response == null) {response = "";}
    }

    // given a string, extract they key from it.
    // If the string is shorter than 8 characters, return
    // a blank.
    //
    // inputLine - the input returned from the client as a String
    //
    // return - the key extracted
    
    public String getKey(String inputLine) 
    {
        if (inputLine.length() < KEY_SIZE)
        {
            return "";
        } 
        else 
        {
            return inputLine.substring(0, 8);
        }
    }

    // given a string, return the substring
    //
    // inputLine - the input returned from the client as a String
    //
    // return- the message extracted

    public String getMessage(String inputLine)
    {
        return inputLine.substring(8);
    }

    public void printHash() 
    {
        System.out.println(stored);
    }

    public void delegate(Socket clientSocket) {
        try (
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) 
        {
            while (true) 
            {
                String inputLine = in.readLine();
                if (inputLine == null) {
                    break;
                }
                synchronized (this) {
                    response = "";
                    processLine(inputLine);
                    System.out.println("DEBUG: sending: " + inputLine);
                    out.println(response);
                    // System.out.println(inputLine);
                    // printHash();
                }
                // out.println(Thread.currentThread() + inputLine);
            }
        }
        catch (Exception e)
        {
            System.err.println(e);
            System.exit(-1);
        }
    }

    public void serve()
    {
        try(
            ServerSocket serverSocket = new ServerSocket(port);
        ) 
        {
            while(true) 
            {   
                try
                {
                    Socket clientSocket = serverSocket.accept();
                    // Socket clientSocketCopy = clientSocket;
                    Runnable runnable = () -> this.delegate(clientSocket);
                    Thread t = new Thread(runnable);
                    t.start();
                } 
                catch (Exception e)
                {
                    System.err.println(e);
                    // if (clientSocketCopy != null) 
                    // {
                    //     clientSocketCopy.close();
                    // }
                    System.exit(-2);
                }
                
            }
        } 
        catch (IOException e) 
        {
            System.err.println(e);
            System.exit(-2);
        } 
        catch (SecurityException e)
        {
            System.err.println(e);
            System.exit(-3);
        } 
        catch (IllegalArgumentException e) 
        {
            System.err.println(e);
            System.exit(-4);
        }
        catch (IllegalBlockingModeException e)
        {
            System.err.println(e);
            System.exit(-6);
        }
    }

    public static void main( String[] args )
    {
        App server = new App(12345);
        server.serve();
    }
}
