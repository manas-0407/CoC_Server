package com.coc.CoC.service;

import com.coc.CoC.models.Output;
import org.springframework.boot.SpringApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.io.*;
import java.util.regex.Pattern;

@org.springframework.stereotype.Service
public class Service {

    private final String valid_java_class_regex = "^[A-Za-z]\\w*$";

    private boolean regex_match(String regex , String name){
        return Pattern.matches(regex,name);
    }

    private String getRandomString(){

        StringBuilder ans = new StringBuilder();
        for(int i=0;i<5;i++){
            ans.append("_");
            ans.append((int) (Math.random() * 100000));
        }

        return String.valueOf(ans);
    }

    private String getClassName(String program){

        StringTokenizer stringTokenizer = new StringTokenizer(program);
        boolean got_class = false;
        while (stringTokenizer.hasMoreTokens()){
            String token = stringTokenizer.nextToken();
            if(got_class){
                if(regex_match(valid_java_class_regex , token)){
                    return token;
                }
                return null;
            }
            if(token.equals("class")) got_class = true;
        }
        return null;
    }


    // for deleting file
    public void deleteFile(String fileName) throws IOException {
        String delete_command = "del "+fileName;
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c",
                "cd \"C:\\Users\\manas\\Desktop\\TestFolder\""+
                        " && "+delete_command);
        builder.redirectErrorStream(true);
        builder.start();
    }

    private String runCommand(String command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c",
                "cd \"C:\\Users\\manas\\Desktop\\TestFolder\""+
                        " && "+command);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line=r.readLine();
        if(line != null) return line;
        return "";
    }

    public Output execute_Java(String program) throws IOException {

        Output response = new Output();

        String className = getClassName(program);

        if(className == null){
            response.upadteOutput("Invalid class name !");
        }

        /*

        // Generating unique class for collision free
        className = className+getRandomString();

         */

        String java_className = className+".java";
        String file_creation_command = "echo. > " + java_className;
        // for windows

        // Creating File
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c",
                "cd \"C:\\Users\\manas\\Desktop\\TestFolder\""+
                        " && "+file_creation_command);
        builder.redirectErrorStream(true);
        builder.start();


        /*
        CMD access to root user
         */
        String username = runCommand("echo %USERNAME%");
        String write_command  = "icacls . /grant " + username+":W";
        runCommand(write_command);

        // write code to file
        String path = "C:\\Users\\manas\\Desktop\\TestFolder\\"+java_className;
        Path fileName = Path.of(path);
        Files.writeString(fileName, program);

        //Compiling the result
        String compile_command = "javac "+java_className;
        builder = new ProcessBuilder(
                "cmd.exe", "/c",
                "cd \"C:\\Users\\manas\\Desktop\\TestFolder\""+
                        " && "+compile_command);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) { break; }
            response.upadteOutput(line);
        }

        if(response.getOutput().length() != 0){
            // Some error while compiling
            deleteFile(java_className);
            deleteFile(className+".class");
            return response;
        }

        // Execute the result
        String execute_command = "java "+className;
        builder = new ProcessBuilder(
                "cmd.exe", "/c",
                "cd \"C:\\Users\\manas\\Desktop\\TestFolder\""+
                        " && "+execute_command);
        builder.redirectErrorStream(true);
        p = builder.start();
        r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while (true) {
            line = r.readLine();
            if (line == null) { break; }
            response.upadteOutput(line);
        }

        deleteFile(java_className);
        deleteFile(className+".class");

        return response;
    }

}



/*

    This is service layer, controller take code and send to this layer to execute on Machine and return the output
        If we want to persist the data, it can be done later via a Repository layer

    Java workflow :
        Get class name with a unique string ( With assumption that file contains unique class with psvm )
        Create a class in local Directory
        Compile .java
        Execute .class
        Delete File
 */