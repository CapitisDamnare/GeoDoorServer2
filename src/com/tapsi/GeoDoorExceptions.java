/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tapsi;

/**
 *
 * @author a.tappler
 */
public class GeoDoorExceptions extends Exception {
    public GeoDoorExceptions(String msg) {
        //String gotClass = this.getClass().getCanonicalName();
        super(msg);
    }
}
