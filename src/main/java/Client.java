import dto.*;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Client {
    public static Socket clientSocket;
    public static ObjectInputStream in;
    public static ObjectOutputStream out;
    public static Scanner scanner;

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
            new ResponseProcess().start();
            while (true) {
                System.out.println("Chose your options");
                System.out.println("LOGIN cú pháp login;username;password");
                System.out.println("ra giá sản phẩm  cú pháp price;giá");
                System.out.println("thoát cú pháp exit:ok");

                String ch = scanner.next();
                StringTokenizer stringTokenizer= new StringTokenizer(ch,";");
                String test1=stringTokenizer.nextToken();
                switch (test1) {
                    case "login": {
                        String username=stringTokenizer.nextToken();
                        String password=stringTokenizer.nextToken();
                        UserForm userForm= new UserForm();
                        userForm.setUsername(username);
                        userForm.setPassword(password);
                        Request<UserForm> request= new Request<>();
                        request.setData(userForm);
                        request.setAction(Action.LOGIN);
                        this.sendRequest(request);
                        break;
                    }
                    case "price": {
                        String price =stringTokenizer.nextToken();
                        OfferForm offerForm = new OfferForm();
                        offerForm.setPrice(Integer.valueOf(price));
                        Request<OfferForm> request= new Request<>();
                        request.setData(offerForm);
                        request.setAction(Action.OFFER);
                        this.sendRequest(request);
                        break;
                    }
                    case "exit": {
                        Request<ExitForm> request= new Request<>();
                        ExitForm form= new ExitForm();
                        request.setData(form);
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

