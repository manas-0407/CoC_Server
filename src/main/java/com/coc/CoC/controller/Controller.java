package com.coc.CoC.controller;

import com.coc.CoC.models.Code;
import com.coc.CoC.models.Output;
import com.coc.CoC.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;

@RestController
public class Controller {

    @Autowired
    Service service;

    @PostMapping(value = "/run")
    public ResponseEntity<Output> runCode(@RequestBody Code code) throws IOException {

        Output output = null;
        if(code.getLang_code() == 1){
            output = service.execute_Java(code.getCode(),code.getInput());
        }
        return ResponseEntity.ok(output);
    }
}


/*

    runcode() gets the Code obj from frontend test it and return to JSON
              Handle file name collision like in java

    Lang Code : 1 -> JAVA
 */