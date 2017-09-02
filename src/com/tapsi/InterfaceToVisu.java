package com.tapsi;

import java.io.IOException;

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
