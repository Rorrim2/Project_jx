package server;

import java.io.*;
import java.util.Map;
import java.util.Scanner;

public class CsvReaderWriter {
    String one = "C:\\Users\\aliss\\IdeaProjects\\project_jx\\src\\server\\1\\one.csv";
    String two = "C:\\Users\\aliss\\IdeaProjects\\project_jx\\src\\server\\2\\two.csv";
    String three = "C:\\Users\\aliss\\IdeaProjects\\project_jx\\src\\server\\3\\three.csv";
    String four = "C:\\Users\\aliss\\IdeaProjects\\project_jx\\src\\server\\4\\four.csv";
    String five = "C:\\Users\\aliss\\IdeaProjects\\project_jx\\src\\server\\5\\five.csv";
    //Nazwa pliku - 0, wlascicie - l, folderID - 2, udostepniony (Y/N) - 3, udostepniony od? - 4

    //read from file to the map
    public Integer readCsv(String filePath, Map<Integer, CsvFileFormat> conMap, Integer i, int[] tab) {
        File file = new File(filePath);
        try {
            Scanner inputStream = new Scanner(file);
            while(inputStream.hasNext()) {
                String data = inputStream.nextLine();
                String[] values = data.split(",");
                CsvFileFormat temp = new CsvFileFormat(values[0], values[1], values[2],
                        values[3], values[4]);
                conMap.put(i, temp);
                int a = Integer.parseInt(values[2]) - 1;
                tab[a]++;
                ++i;
            }
            inputStream.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return i;
    }

    public void deleteFile(Map<Integer, CsvFileFormat> conMap, File file, CsvFileFormat format) {
        //delete from csv
        File tempFile = new File("myTempFile.txt");
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            writer = new BufferedWriter(new FileWriter(tempFile));
        }
        catch (Exception f) {
            System.out.println(f.getStackTrace());
        }

        String lineToRemove = format.getName() + "," + format.getOwner() + "," + format.getFolderNumber() + "," + format.getShared() + "," + format.getSharedFrom() + "\n";
        String currentLine = null;

        while(true) {
            try {
                if((currentLine = reader.readLine()) == null) {
                    break;
                }
            }
            catch (Exception e) {
                System.out.println(e.getStackTrace());
            }
            if(currentLine.equals(lineToRemove)) continue;
            try {
                writer.write(currentLine);
            }
            catch (Exception e) {
                System.out.println(e.getStackTrace());
            }
        }
        try {
            writer.close();
            reader.close();
        }
        catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
        String path = file.getAbsolutePath();
        tempFile.renameTo(file);
        //delete from mapOfFolders
        for(Integer i : conMap.keySet()) {
            CsvFileFormat temp = conMap.get(i);
            if(temp.getOwner().equals(format.getOwner()) && temp.getName().equals(format.getName())) {
                conMap.remove(i);
            }
        }
        //delete from folder
        file.delete();
    }

    //save from hash to the folders
    public void writeCsv(Map<Integer, CsvFileFormat> conMap) {
        FileWriter fileWriterOne = null;
        FileWriter fileWriterTwo = null;
        FileWriter fileWriterThree = null;
        FileWriter fileWriterFour = null;
        FileWriter fileWriterFive = null;
        try {
            fileWriterOne = new FileWriter(one, true);
            fileWriterTwo = new FileWriter(two, true);
            fileWriterThree = new FileWriter(three, true);
            fileWriterFour = new FileWriter(four, true);
            fileWriterFive = new FileWriter(five, true);

            //System.out.println("In writeCsv");
            //fileWriterOne.write("Czy da się wpisać cokolwoiek?\n");

            for(Integer i : conMap.keySet()) {
               // System.out.println("Write csv foreach");
                CsvFileFormat temp = conMap.get(i);
                String number =  temp.getFolderNumber();
                if(number.equals("1")) {
                    appendAll(fileWriterOne, temp);
                }
                else if(number.equals("2")) {
                    appendAll(fileWriterTwo, temp);
                }
                else if(number.equals("3")) {
                    appendAll(fileWriterThree, temp);
                }
                else if(number.equals("4")) {
                    appendAll(fileWriterFour, temp);
                }
                else if(number.equals("5")) {
                    appendAll(fileWriterFive, temp);
                }
                else {
                    System.out.println("None of");
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try{
                fileWriterOne.flush();
                fileWriterOne.close();

                fileWriterTwo.flush();
                fileWriterTwo.close();

                fileWriterThree.flush();
                fileWriterThree.close();

                fileWriterFour.flush();
                fileWriterFour.close();

                fileWriterFive.flush();
                fileWriterFive.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public void appendAll(FileWriter fileWriter, CsvFileFormat format) throws IOException{
        fileWriter.write(format.getName());
        fileWriter.write(",");
        fileWriter.write(format.getOwner());
        fileWriter.write(",");
        fileWriter.write(format.getFolderNumber());
        fileWriter.write(",");
        fileWriter.write(format.getShared());
        fileWriter.write(",");
        fileWriter.write(format.getSharedFrom());
        fileWriter.write("\n");
    }

}
