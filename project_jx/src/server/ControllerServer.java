package server;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import sample.PackageToSend;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ControllerServer {
    @FXML
    private Label textOfCurAction;
    @FXML
    private ListView<String> listView1 = new ListView<>();
    @FXML
    private ListView<String> listView2= new ListView<>();
    @FXML
    private ListView<String> listView3= new ListView<>();
    @FXML
    private ListView<String> listView4= new ListView<>();
    @FXML
    private ListView<String> listView5= new ListView<>();

    public static ArrayList<String> userList = new ArrayList<String>();
    String username;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    ServerSocket serverSocket;
    ServerSocket serverSocketSeven;
    Socket socket;
    Socket socketSeven;
    public static ConcurrentHashMap<Integer, CsvFileFormat> mapOfFolders  = new ConcurrentHashMap<>();

    public void initialize(){
        try  {
            serverSocket = new ServerSocket(6000);
            serverSocketSeven = new ServerSocket(7000);
            System.out.println("The date server is running...");
        }
        catch(IOException e) {
            System.err.println(e.getStackTrace());
        }
        new Thread(() -> startThread()).start();
    }

    public void startThread() {
        while (true) {
            try {
                socket = serverSocket.accept();
                try{
                    out = new ObjectOutputStream(socket.getOutputStream());
                    in = new ObjectInputStream(socket.getInputStream());
                }
                catch (IOException e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
                //dodaj uzytkownika
                try{
                    String tempReceived = (String)in.readObject();
                    List<String> items = Arrays.asList(tempReceived.split("\\s*,\\s*"));
                    if(items.get(0).equals("Init")) {
                        username = items.get(1);
                        userList.add(username);
                    }
                }
                catch (ClassNotFoundException e) {
                    System.out.println(e.getStackTrace());
                }
                //wyslanie listy uzytkownikow
                try {
                    out.writeObject(userList);
                    out.flush();
                }
                catch(IOException e){
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
                System.out.println("Client accepted");
                //odpal listenera
                new ServiceThread(socket, username, in, out).start();
            }
            catch(IOException e){
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
    }

    public int savingController(int[] tab) {
        int i, min;
        for(i = 0, min = i; i < tab.length; ++i) {
            if(tab[i] < tab[min]) {
                min = i;
            }
        }
        return min;
    }

    public void setListView(Map<Integer, CsvFileFormat> conMap) {
        for(Integer i : conMap.keySet()) {
            CsvFileFormat temp = conMap.get(i);
            String number =  temp.getFolderNumber();
            if(number.equals("1")) {
                listView1.getItems().add(temp.getName());
            }
            else if(number.equals("2")) {
                listView2.getItems().add(temp.getName());
            }
            else if(number.equals("3")) {
                listView3.getItems().add(temp.getName());
            }
            else if(number.equals("4")) {
                listView4.getItems().add(temp.getName());
            }
            else if(number.equals("5")) {
                listView5.getItems().add(temp.getName());
            }
        }
        listView1.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }
    public void checkHashMap(Map<Integer, CsvFileFormat> conMap) {
        System.out.println("Checking if map works");
        for(Integer i : conMap.keySet()) {
            CsvFileFormat temp = conMap.get(i);
            System.out.println(temp.getName());
        }
    }
}
