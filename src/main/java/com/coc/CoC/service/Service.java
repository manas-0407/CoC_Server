package com.coc.CoC.service;

import com.coc.CoC.models.Output;

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

    private ArrayList<String> get_inputs(String input){
        StringTokenizer st = new StringTokenizer(input , "\n");
        ArrayList<String> inputs = new ArrayList<>();
        while (st.hasMoreTokens()){
            inputs.add(st.nextToken());
        }
        return inputs;
    }


    // for deleting file
    public void deleteFile(String fileName) throws IOException {
        String delete_command = "rm "+fileName;
        ProcessBuilder builder = new ProcessBuilder(
                "/bin/bash", "-c",
                "cd /home/ubuntu/javaCode && " + delete_command);
        builder.redirectErrorStream(true);
        builder.start();
    }

    public Output execute_Java(String program,String input) throws IOException {

        Output response = new Output();

        String className = getClassName(program);

        if(className == null){
            response.upadteOutput("Invalid class name !");
            return response;
        }

        System.err.print("classname is : ");
        System.out.println(className);

        /*

        // Generating unique class for collision free
        className = className+getRandomString();

         */

        String java_className = className+".java";
        String file_creation_command = "touch "+java_className;
        // for linux

        // Creating File
        ProcessBuilder builder = new ProcessBuilder(
                "/bin/bash", "-c",
                "cd /home/ubuntu/javaCode" +" && " + file_creation_command);
        builder.redirectErrorStream(true);
        builder.start();

        System.out.println("FILE CREATED ! ");

        // write code to file
        String path = "/home/ubuntu/javaCode/"+java_className;
        Path fileName = Path.of(path);
        Files.writeString(fileName, program);

        System.out.println("Written in file");

        //Compiling the result
        String compile_command = "javac "+java_className;
        builder = new ProcessBuilder(
                "/bin/bash", "-c",
                "cd /home/ubuntu/javaCode && " + compile_command);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) { break; }
            response.upadteOutput(line);
            response.upadteOutput("WHILE COMPILING");
        }

        if(response.getOutput().length() != 0){
            // Some error while compiling
//            deleteFile(java_className);
//            deleteFile(className+".class");
            return response;
        }

        System.out.println("COMPILED SUCCESS");

        // Execute the result
        String execute_command = "java "+className;
        builder = new ProcessBuilder(
                "/bin/bash", "-c",
                "cd /home/ubuntu/javaCode && " + execute_command);
        builder.redirectErrorStream(true);
        p = builder.start();
        r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while (true) {
            line = r.readLine();
            if (line == null) { break; }
            response.upadteOutput(line);
        }

        int input_pointer = 0;
        while (response.getOutput().length() == 0){
            // Logic Here for input
        }

//        deleteFile(java_className);
//        deleteFile(className+".class");

        System.out.println("RUN SUCCESS");


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