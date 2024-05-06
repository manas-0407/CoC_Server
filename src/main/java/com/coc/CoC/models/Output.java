package com.coc.CoC.models;


import lombok.Data;

import java.util.*;
import java.io.*;

@Data
public class Output {
    StringBuilder output;

    public Output(){
        output = new StringBuilder();
    }

    public void updateOutput(String s) {
        output.append(s).append("\n");
    }
}
