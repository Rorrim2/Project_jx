package sample;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;


/**
 * @author ashraf
 *
 */
public class FileAlterationListenerImpl implements FileAlterationListener {
    /*Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;*/
    Main_Scene_Controller mainScene = new Main_Scene_Controller();
    @Override
    public void onStart(final FileAlterationObserver observer) {
       /* try{
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
        }*/
    //System.out.println("The FileListener has started on "
    // + observer.getDirectory().getAbsolutePath());
    }

    @Override
    public void onDirectoryCreate(final File directory) {
        directory.delete();
        System.out.println("Cannot create folder in local folder");
    }

    @Override
    public void onDirectoryChange(final File directory) {
    }

    @Override
    public void onDirectoryDelete(final File directory) {
    }

    @Override
    public void onFileCreate(final File file) {
        //dodaj do listy plikow
        mainScene.listOfFiles.add(file.getName());
        //wyslij mod + nazwe pliku
        synchronized (mainScene.out) {
            try {
                mainScene.out.writeObject("Mod, " + mainScene.con.username);
                mainScene.out.flush();
            } catch (IOException e) {
                System.out.println(e.getStackTrace());
            }
            //wysli plik
            PackageToSend pack = new PackageToSend(mainScene.con.username, file.getName(), file);
            //teraz to wyslij
            try {
                mainScene.out.writeObject(pack);
                mainScene.out.flush();
                System.out.println("Wyslane");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFileChange(final File file) {
        //wyslij mod + nazwe pliku
        synchronized (mainScene.out) {
            try {
                mainScene.out.writeObject("Mod, " + mainScene.con.username);
                mainScene.out.flush();
            } catch (IOException e) {
                System.out.println(e.getStackTrace());
            }
            //wysli plik
            PackageToSend pack = new PackageToSend(mainScene.con.username, file.getName(), file);
            //teraz to wyslij
            try {
                mainScene.out.writeObject(pack);
                mainScene.out.flush();
                System.out.println("Wyslane");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFileDelete(final File file) {
        //usun plik z listy plikow
        if(mainScene.listOfFiles.contains(file.getName())) {
            mainScene.listOfFiles.remove(file.getName());
        }
        //wysli delete + nazwa plik
        synchronized (mainScene.out) {
            try {
                mainScene.out.writeObject("Delete, " + mainScene.con.username + ", " + file.getName());
                mainScene.out.flush();
            } catch (IOException e) {
                System.out.println(e.getStackTrace());
            }
        }
    }

    @Override
    public void onStop(final FileAlterationObserver observer) {
        /*try{
            this.in.close();
            this.out.close();
        }
        catch (IOException e) {
            System.out.println("Error while closing streams");
        }*/
    }
}