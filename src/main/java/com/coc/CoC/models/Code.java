package com.coc.CoC.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Code {
    String code;
    int lang_code;
    LocalDateTime dateTime;
}

/*
lang_code : Code for the language choosen
code : String of code to be executed
dateTime : Current date and time
 */