package server;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.application.Platform;
import sample.PackageToSend;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServiceThread extends Thread {
    private ArrayList<String> listOfSharedFiles = new ArrayList<>();
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private PackageGiven pack;
    private ArrayList<String> listOfFiles = new ArrayList<>();
    private ArrayList<String> clientListOfSharedFiles = new ArrayList<>();
    private String pathOfMissing = null;
    ControllerServer controllerServer = new ControllerServer();
    public CsvReaderWriter readerWriter = new CsvReaderWriter();
    String username;
    HashMapKey trackingHash;
    public String absolute = "C:\\Users\\aliss\\IdeaProjects\\project_jx\\src\\server\\";
    String one = "C:\\Users\\aliss\\IdeaProjects\\project_jx\\src\\server\\1\\one.csv";
    String two = "C:\\Users\\aliss\\IdeaProjects\\project_jx\\src\\server\\2\\two.csv";
    String three = "C:\\Users\\aliss\\IdeaProjects\\project_jx\\src\\server\\3\\three.csv";
    String four = "C:\\Users\\aliss\\IdeaProjects\\project_jx\\src\\server\\4\\four.csv";
    String five = "C:\\Users\\aliss\\IdeaProjects\\project_jx\\src\\server\\5\\five.csv";
    public static int[] folderSize = new int[5];
    String[] numberPaths = new String[5];


    public ServiceThread(Socket s, String user, ObjectInputStream inGiven, ObjectOutputStream outGiven) {
        //watek jest odpalany w trakcie inita initcie -> na pocztaku usypiam, nasłuchuje w petli i odbiera share, mod i delete
        trackingHash.key = readerWriter.readCsv(one, controllerServer.mapOfFolders, trackingHash.key, folderSize);
        trackingHash.key = readerWriter.readCsv(two, controllerServer.mapOfFolders, trackingHash.key, folderSize);
        trackingHash.key = readerWriter.readCsv(three, controllerServer.mapOfFolders, trackingHash.key, folderSize);
        trackingHash.key = readerWriter.readCsv(four, controllerServer.mapOfFolders, trackingHash.key, folderSize);
        readerWriter.readCsv(five, controllerServer.mapOfFolders, trackingHash.key, folderSize);

        for(int j = 0; j < 5; ++j) {
            folderSize[j] = 0;
        }

        numberPaths[0] = one;
        numberPaths[1] = two;
        numberPaths[2] = three;
        numberPaths[3] = four;
        numberPaths[4] = five;

        this.username = user;
        this.socket = s;

        out = outGiven;
        in = inGiven;

        try {
            listOfFiles = (ArrayList<String>)in.readObject();
            for(String every : listOfFiles) {
                System.out.println(every);
            }
        }
        catch(IOException e) {
            System.out.println(e.getStackTrace());
        }
        catch (ClassNotFoundException c) {
            System.out.println(c.getStackTrace());
        }
        //reszta inita
        onInitCall();
    }

    @Override
    public void run() {
        while(true) {
            //uspij
            try {
                TimeUnit.SECONDS.sleep(30);
            }
            catch (InterruptedException e) {
                e.getStackTrace();
            }
            synchronized (in) {
                //wyslij liste uzytkownikow
                try {
                    out.writeObject(controllerServer.userList);
                    out.flush();
                }
                catch(IOException e){
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
                //sprawdz czy sa udostepnione pliki
                makeListOfSharedFiles();
                //wrzuc na liste plikow udotepnionych
                //porownanie
                Collection clientFilesShared = clientListOfSharedFiles;
                Collection serverFilesShared = listOfSharedFiles;
                serverFilesShared.removeAll(clientFilesShared);
                ArrayList<String> missingFilesShared = new ArrayList<String>(clientFilesShared);
                //wyslij ilosc plikow
                try {
                    out.writeObject(missingFilesShared.size());
                    out.flush();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                //wyslij brakujace
                for(String every : missingFilesShared) {
                    pathOfMissing = "";
                    System.out.println(every);
                    for(Integer i : controllerServer.mapOfFolders.keySet()) {
                        CsvFileFormat temp = controllerServer.mapOfFolders.get(i);
                        if(temp.getShared().equals("T") && temp.getOwner().equals(username) && temp.getName().equals(every)) {
                            pathOfMissing = absolute + "\\" + temp.getFolderNumber() + "\\" + temp.getName();
                        }
                    }
                    if(pathOfMissing.equals("")) {
                        System.out.println("No such file");
                        continue;
                    }

                    File file = new File(pathOfMissing);
                    PackageToSend pack = new PackageToSend(username, every, file);
                    //teraz to wyslij
                    try{
                        out.writeObject(pack);
                        out.flush();
                        System.out.println("Wyslane");
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                clientListOfSharedFiles = listOfSharedFiles;
            }
            try {
                String tempReceived = (String)in.readObject();
                List<String> items = Arrays.asList(tempReceived.split("\\s*,\\s*"));
                if(items.get(0).equals("Init")) {
                    System.out.println("There should not be init in run");
                    break;
                }
                else if(items.get(0).equals("Share")) {
                    onShareCall(items.get(1));
                }
                else if(items.get(0).equals("Mod")) {
                    onModifiedCall();
                }
                else if(items.get(0).equals("Delete")) {
                    onDeleteCall(items.get(1), items.get(2));
                }
                else {
                    System.out.println("Wrong string");
                    break;
                }
            }
            catch (ClassNotFoundException e) {
                System.out.println(e.getStackTrace());
            }
            catch (IOException io) {
                System.out.println(io.getStackTrace());
            }
        }

    }
    private void onInitCall() {
        //odebranie listy plikow
        ArrayList<String> listOfFiles = new ArrayList<>();
        try {
            listOfFiles = (ArrayList<String>)in.readObject();
            for(String every : listOfFiles) {
                System.out.println(every);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //porownanie
        ArrayList<String> folderSideListOfFiles = new ArrayList<>();
        for(Integer i : controllerServer.mapOfFolders.keySet()) {
            CsvFileFormat temp = controllerServer.mapOfFolders.get(i);
            if(temp.getOwner().equals(username)) {
                folderSideListOfFiles.add(temp.getName());
            }
        }
        Collection clientFiles = listOfFiles;
        Collection serverFiles = folderSideListOfFiles;
        clientFiles.removeAll(serverFiles);
        ArrayList<String> missingFiles = new ArrayList<String>(clientFiles);

        //odeslanie listy brakujacych
        try {
            out.writeObject(missingFiles);
            out.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        //pobieranie i zapisywanie plikow
        //Platform.runLater(() -> textOfCurAction.setText("Downloading")); //TODO jakos trzeba to zrobic
        for(int i = 0; i < missingFiles.size(); ++i) {
            PackageToSend pack;
            //System.out.println("Get to this point - so far so good");
            try {
                pack = (PackageToSend) in.readObject();
                //System.out.println("Paczka otrzymana");
                Integer j = controllerServer.savingController(folderSize);
                j++;
                CsvFileFormat formatGiven = new CsvFileFormat(pack.getFilename(), pack.getUsername(), j.toString(), "N", "-");
                controllerServer.mapOfFolders.put(trackingHash.key, formatGiven);
                trackingHash.key++;
                File newfile = new File(absolute + j.toString() + "\\" + pack.getFilename());

                if(newfile.createNewFile()) {
                    System.out.println("File created");
                    folderSize[j - 1]++;
                }
                else {
                    newfile.delete();
                    System.out.println("File already exists");
                }
                FileOutputStream fos = new FileOutputStream(newfile);
                fos.write(pack.getContent());
                fos.close();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            catch (ClassNotFoundException c) {
                System.out.println(c.getStackTrace());
            }
        }
        //---------------------------------------------------------------------------------------------------------
        //udostepnianie
        //pobierz liste plikow udostepnionych u klienta
        try {
            clientListOfSharedFiles = (ArrayList<String>) in.readObject();
        }
        catch (ClassNotFoundException e) {
            System.out.println(e.getStackTrace());
        }
        catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
        //zrob liste wszystkich udostepnionych plikow
        makeListOfSharedFiles();
        //porownanie
        Collection clientFilesShared = clientListOfSharedFiles;
        Collection serverFilesShared = listOfSharedFiles;
        serverFilesShared.removeAll(clientFilesShared);
        ArrayList<String> missingFilesShared = new ArrayList<String>(clientFilesShared);
        //wyslij ilosc plikow
        try {
            out.writeObject(missingFilesShared.size());
            out.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        //wyslij brakujace
        for(String every : missingFilesShared) {
            pathOfMissing = "";
            System.out.println(every);
            for(Integer i : controllerServer.mapOfFolders.keySet()) {
                CsvFileFormat temp = controllerServer.mapOfFolders.get(i);
                if(temp.getShared().equals("T") && temp.getOwner().equals(username) && temp.getName().equals(every)) {
                    pathOfMissing = absolute + "\\" + temp.getFolderNumber() + "\\" + temp.getName();
                }
            }
            if(pathOfMissing.equals("")) {
                System.out.println("No such file");
                continue;
            }

            File file = new File(pathOfMissing);
            PackageToSend pack = new PackageToSend(username, every, file);
            //teraz to wyslij
            try{
                out.writeObject(pack);
                out.flush();
                System.out.println("Wysłane");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        clientListOfSharedFiles = listOfSharedFiles;
        //mapy do plikow
        //checkHashMap(mapOfFolders);
        readerWriter.writeCsv(controllerServer.mapOfFolders);
        //wyswietl listy, funkcja
        //System.out.println("Here");
        controllerServer.setListView(controllerServer.mapOfFolders);
        return;
    }
    private void onShareCall(String user) {

    }

    private void onModifiedCall() {
        PackageToSend pack;
        //System.out.println("Get to this point - so far so good");
        try {
            pack = (PackageToSend) in.readObject();
            //System.out.println("Paczka otrzymana");
            Integer j = controllerServer.savingController(folderSize);
            j++;
            CsvFileFormat formatGiven = new CsvFileFormat(pack.getFilename(), pack.getUsername(), j.toString(), "N", "-");
            controllerServer.mapOfFolders.put(trackingHash.key, formatGiven);
            trackingHash.key++;
            File newfile = new File(absolute + j.toString() + "\\" + pack.getFilename());

            if(newfile.createNewFile()) {
                System.out.println("File created");
                folderSize[j - 1]++;
            }
            else {
                newfile.delete();
                System.out.println("File already exists");
            }
            FileOutputStream fos = new FileOutputStream(newfile);
            fos.write(pack.getContent());
            fos.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException c) {
            System.out.println(c.getStackTrace());
        }
        readerWriter.writeCsv(controllerServer.mapOfFolders);
        controllerServer.setListView(controllerServer.mapOfFolders);
        return;
    }

    private void onDeleteCall(String owner, String name) {
        CsvFileFormat format = null;
        for(Integer i : controllerServer.mapOfFolders.keySet()) {
            CsvFileFormat temp = controllerServer.mapOfFolders.get(i);
            if(temp.getOwner().equals(owner) && temp.getName().equals(name)) {
                format = temp;
            }
        }
        File f = new File("C:\\Users\\aliss\\IdeaProjects\\project_jx\\src\\server\\" + format.getFolderNumber() + "\\" + format.getName());
        readerWriter.deleteFile(controllerServer.mapOfFolders, f, format);
        return;
    }
    private void makeListOfSharedFiles() {
        for(Integer i : controllerServer.mapOfFolders.keySet()) {
            CsvFileFormat temp = controllerServer.mapOfFolders.get(i);
            if(temp.getShared().equals("T") && temp.getOwner().equals(username)) {
                listOfSharedFiles.add(temp.getName());
            }
        }
    }
}
