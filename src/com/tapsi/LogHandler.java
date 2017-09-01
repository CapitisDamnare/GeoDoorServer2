/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tapsi;

import java.io.IOException;

/**
 *
 * @author a.tappler
 */
public class LogHandler {

    public static void handleError(GeoDoorExceptions ex) {
        ex.printStackTrace();
    }

    static void handleError(InterruptedException ex) {
        ex.printStackTrace();
    }
    
        static void handleError(IOException ex) {
        ex.printStackTrace();
    }

    public static void printPrompt() {
        System.out.print("GeoDoorServer:> ");
    }
}
