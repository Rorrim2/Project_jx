package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import server.CsvFileFormat;

import java.io.*;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main_Scene_Controller {

    @FXML
    private AnchorPane anchor_main_scene;
    @FXML
    private MenuItem menu_item_edit_dark;
    @FXML
    private TreeView<File> treeView;
    @FXML
    private TreeView<File> treeViewShare;
    @FXML
    private Label textOfCurAction;
    @FXML
    private Button showDirectoryTree;
    public Path path;

    public String sharePath = "C:\\Users\\aliss\\IdeaProjects\\project_jx\\src\\sample\\local\\shared";

    private boolean first = true;
    public static ArrayList<String> listOfFiles = new ArrayList<String>();
    public static ArrayList<String> listOfFilesShared = new ArrayList<String>();
    public ArrayList<String> listOfUsers;

    public static Socket socket;
    public static ObjectOutputStream out;
    public static ObjectInputStream in;
    public static Controller con = new Controller();

    private static String pathOfMissing = "";

    public Main_Scene_Controller() {
    }


    public void initialize(){
        try{
            socket = new Socket("localhost", 6000);
        }
        catch (IOException e) {
            System.err.println(e.getStackTrace());
            System.exit(1);
        }
        try{
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        Stage window = (Stage) anchor_main_scene.getScene().getWindow();
        window.setOnCloseRequest(event1 -> {
            try {
                in.close();
                out.close();
                socket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void setTree() {
        // wyczysc liste
        listOfFiles.clear();
        File rootFile = new File(con.pathname);
        TreeItem<File> root = new TreeItem<File>(rootFile);
        root.setExpanded(false);
        try{
            createTree(root, listOfFiles);
        }
        catch(IOException io) {
            System.out.println(io.getStackTrace());
        }
        treeView.setRoot(root);
        treeView.setShowRoot(false);

        listOfFilesShared.clear();
        File rootFileShare = new File(sharePath);
        TreeItem<File> rootShare = new TreeItem<File>(rootFileShare);
        rootShare.setExpanded(false);

        try{
            createTree(rootShare, listOfFilesShared);
        }
        catch(IOException io) {
            System.out.println(io.getStackTrace());
        }

        treeViewShare.setRoot(root);
        treeViewShare.setShowRoot(false);
    }

    public void changeToSnakeScene(ActionEvent event) throws IOException {
        Parent tableViewParent = FXMLLoader.load(getClass().getResource("log_scene.fxml"));
        Scene tableViewScene = new Scene(tableViewParent);

        //Stage information
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

        window.setScene(tableViewScene);
        window.show();
    }

    public void switchMode(ActionEvent event) {

        String caspian = getClass().getResource("caspian.css").toExternalForm();
        String modena = getClass().getResource("modena_dark.css").toExternalForm();

        Scene scene = anchor_main_scene.getScene();

        if(scene.getStylesheets().contains(caspian)) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(modena);
            menu_item_edit_dark.setText("Switch to light mode");
        }
        else if(scene.getStylesheets().contains(modena)) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(caspian);
            menu_item_edit_dark.setText("Switch to dark mode");
        }

    }

    private class ListeningThread extends Thread {
        public boolean isRunning = true;
        public boolean isFirst = true;
        public void startMonitor() throws Exception {
            // Change this to match the environment you want to watch.
            final File directory = new File("C:\\Users\\aliss\\IdeaProjects\\project_jx\\out\\production\\project_jx\\sample\\local\\main");
            FileAlterationObserver fao = new FileAlterationObserver(directory);
            fao.addListener(new FileAlterationListenerImpl());
            final FileAlterationMonitor monitor = new FileAlterationMonitor();
            monitor.addObserver(fao);
            System.out.println("Starting monitor. CTRL+C to stop.");
            monitor.start();

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        System.out.println("Stopping monitor.");
                        monitor.stop();
                    } catch (Exception ignored) {
                    }
                }
            }));
        }

        public void run() {
            if(isFirst) {
                try {
                    isFirst = false;
                    startMonitor();
                }
                catch (Exception e) {
                    System.out.println(e.getStackTrace());
                }
            }
            while(isRunning) {
                //pobierz liste uzytkownikow
                try {
                    listOfUsers = (ArrayList<String>) in.readObject();
                }
                catch (ClassNotFoundException e) {
                    System.out.println(e.getStackTrace());
                }
                catch (IOException e) {
                    System.out.println(e.getStackTrace());
                }
                for(String every : listOfUsers) {
                    System.out.println(every);
                }
                //pobierz ilosc plikow
                Integer size = 0;
                try {
                    size = (Integer) in.readObject();
                }
                catch (ClassNotFoundException e) {
                    System.out.println(e.getStackTrace());
                }
                catch (IOException e) {
                    System.out.println(e.getStackTrace());
                }
                //pobierz pliki i zapisz w shared
                for(int i = 0; i < size; ++i) {
                    PackageToSend pack;
                    try {
                        pack = (PackageToSend) in.readObject();
                        File newfile = new File(sharePath + pack.getFilename());
                        if(newfile.createNewFile()) {
                            System.out.println("File created");
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

            }
        }
    }

    ListeningThread listeningThread;

    public void showTree(ActionEvent event) {
        if(con.pathname == null) {
            System.out.println("con.pathname is null");
            return;
        }

        //TreeItem<Path> root = new TreeItem<Path>(Paths.get(con.pathname));
        File rootFile = new File(con.pathname);
        TreeItem<File> root = new TreeItem<File>(rootFile);
        root.setExpanded(false);

        try{
            createTree(root, listOfFiles);
        }
        catch(IOException io) {
            System.out.println(io.getStackTrace());
        }

        treeView.setRoot(root);
        treeView.setShowRoot(false);

        showDirectoryTree.setDisable(true);

        File rootFileShare = new File(sharePath);
        TreeItem<File> rootShare = new TreeItem<File>(rootFileShare);
        rootShare.setExpanded(false);

        try{
            createTree(rootShare, listOfFilesShared);
        }
        catch(IOException io) {
            System.out.println(io.getStackTrace());
        }

        treeViewShare.setRoot(root);
        treeViewShare.setShowRoot(false);

        if(first) {
            init();
            first = false;
        }
    }

    private void init() {
        //wysylanie nazwy
        try{
            out.writeObject("Init, " + con.username);
            out.flush();
        }
        catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
        //pobierz liste uzytkownikow
        try {
            listOfUsers = (ArrayList<String>) in.readObject();
        }
        catch (ClassNotFoundException e) {
            System.out.println(e.getStackTrace());
        }
        catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
        for(String every : listOfUsers) {
            System.out.println(every);
        }
        //wyslij liste plikow
        try{
            out.writeObject(listOfFiles);
            out.flush();
        }
        catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
        //odbierz liste plikow brakujacych
        ArrayList<String> missingFiles = new ArrayList<>();
        try {
            missingFiles = (ArrayList<String>) in.readObject();
        }
        catch (ClassNotFoundException e) {
            System.out.println(e.getStackTrace());
        }
        catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
        System.out.println("Missing");
        //wyslij pliki
        textOfCurAction.setText("Sending");
        for(String every : missingFiles) {
            pathOfMissing = "";
            System.out.println(every);
            try {
                searchTree(every, con.pathname);
                if(pathOfMissing.equals("")) {
                    System.out.println("No such file");
                    continue;
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            File file = new File(pathOfMissing);
            PackageToSend pack = new PackageToSend(con.username, every, file);
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
        //--------------------------------------------------------------------------------------------------------------
        //udostepnione na serwerze
        //wyslij liste plikow udostepnionych
        try{
            out.writeObject(listOfFilesShared);
            out.flush();
        }
        catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
        //pobierz ilosc plikow
        Integer size = 0;
        try {
            size = (Integer) in.readObject();
        }
        catch (ClassNotFoundException e) {
            System.out.println(e.getStackTrace());
        }
        catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
        //pobierz pliki i zapisz w shared
        for(int i = 0; i < size; ++i) {
            PackageToSend pack;
            try {
                pack = (PackageToSend) in.readObject();
                File newfile = new File(sharePath + pack.getFilename());
                if(newfile.createNewFile()) {
                    System.out.println("File created");
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
        setTree();
        listeningThread.start();
    }

    public void createTree(TreeItem<File> rootItem, ArrayList<String> list) throws IOException {
        String newOneString = rootItem.getValue().getAbsolutePath().toString();
        Path newPath = Paths.get(newOneString);
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(newPath);
        for(Path pathh : directoryStream) {
            File newOneFile = new File(pathh.toString());
            TreeItem<File> newItem = new TreeItem<File>(newOneFile);
            newItem.setExpanded(false);

            rootItem.getChildren().add(newItem);

            if(Files.isDirectory(pathh)) {
                createTree(newItem, list);
            }
            else {
                list.add(newOneFile.getName());
            }
        }
    }


    public void searchTree(String name, String baseFilename) throws IOException {
        File base = new File(baseFilename);
        File[] listOfFile = base.listFiles();
        for(File every : listOfFile) {
            if(every.isDirectory()) {
                searchTree(name, every.getAbsolutePath());
            }
            else {
                if(every.getName().equals(name)) {
                    System.out.println("Znalazlam");
                    pathOfMissing = every.getAbsolutePath();
                    return;
                }
            }
        }
    }

    public void setList () {

    }


}
