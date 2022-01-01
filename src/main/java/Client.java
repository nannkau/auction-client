import dto.Action;
import dto.Request;
import dto.Response;
import dto.UserForm;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Scanner scanner;
    List<String> userOnlines;

    private void sendRequest(Request req) throws IOException {
        this.out.writeObject(req);
        this.out.flush();
    }

    private void close() throws IOException {
        if (this.in != null) {
            this.in.close();
        }
        if (this.out != null) {
            this.out.close();
        }

        if (this.clientSocket != null) {
            this.clientSocket.close();
        }
    }


    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.in = new ObjectInputStream(clientSocket.getInputStream());
            scanner = new Scanner(System.in);
            userOnlines = new ArrayList<>();
            new ResponseProcess().start();
            Request<String> connect= new Request<>();
            connect.setAction(Action.CONNECT);
            sendRequest(connect);
            while (true) {
                System.out.println("Chose your options");
                System.out.println("1: LOGIN");
                System.out.println("2: SEND MESSAGE");
                System.out.println("3: SEND ALL");
                System.out.println("-1: ESC");

                String ch = scanner.next();
                switch (ch) {
                    case "1": {
                        UserForm userForm= new UserForm();
                        userForm.setUsername("admin");
                        userForm.setPassword("123456");
                        Request<UserForm> request= new Request<>();
                        request.setData(userForm);
                        request.setAction(Action.LOGIN);
                        this.sendRequest(request);
                        break;
                    }
                    case "-1": {
                        Request<String> request= new Request<>();
                        request.setAction(Action.DISCONNECT);
                        sendRequest(request);
                        close();
                        return;
                    }
                    default:
                        break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

                if (out != null) {
                    out.close();
                }

                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class ResponseProcess extends Thread {

        @Override
        public void run() {
            try {
                while (true) {
                    Response object = (Response) in.readObject();
                    if (ObjectUtils.isEmpty(object)) {
                        continue;
                    }
                    System.out.println((String)object.getData());


                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}

