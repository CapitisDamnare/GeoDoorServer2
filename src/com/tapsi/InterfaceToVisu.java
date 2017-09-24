package com.tapsi;

import java.io.IOException;

public class InterfaceToVisu {
    //private static Visualization visu = null;
    
    InterfaceToVisu() {
        //visu = new Visualization(this);
        //visu.setVisible(true);
    }
    
    public void startPressed() {
        try {
            GeoDoorServer2.startServer();
        } catch (InterruptedException ex) {
            LogHandler.handleError(ex);
        } catch (IOException ex) {
            LogHandler.handleError(ex);
        }
    }
    
    public void stopPressed() {
        try {
            GeoDoorServer2.stopServer();
        } catch (GeoDoorExceptions ex) {
            LogHandler.handleError(ex);
        }
    }
    
    void showVisualization(boolean val) {
        //visu.showWindow(val);
    }
    
    void closeVisualisation() {
        //visu.closeWindow();
    }
}
