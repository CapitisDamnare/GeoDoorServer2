/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tapsi;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author a.tappler
 */
public class InterfaceToVisu {
    //private static Visualization visu = null;
    private static GeoDoorServer2 server= null;
    
    public InterfaceToVisu(GeoDoorServer2 server) {
        this.server = server;
        //visu = new Visualization(this);
        //visu.setVisible(true);
    }
    
    public void startPressed() {
        try {
            server.startServer();
        } catch (InterruptedException ex) {
            LogHandler.handleError(ex);
        } catch (IOException ex) {
            LogHandler.handleError(ex);
        }
    }
    
    public void stopPressed() {
        try {
            server.stopServer();
        } catch (GeoDoorExceptions ex) {
            LogHandler.handleError(ex);
        }
    }
    
    public void showVisualization(boolean val) {
        //visu.showWindow(val);
    }
    
    public void closeVisualisation() {
        //visu.closeWindow();
    }
}
