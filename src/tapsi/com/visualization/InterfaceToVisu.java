package tapsi.com.visualization;

import tapsi.com.GeoDoorServer2;
import tapsi.com.logging.GeoDoorExceptions;
import tapsi.com.logging.LogHandler;
import tapsi.com.visualization.Visualization;

import java.io.IOException;

public class InterfaceToVisu {
    private static Visualization visu = null;
    
    InterfaceToVisu() {
        //visu = new Visualization();
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
    
    public void showVisualization(boolean val) {
        //visu.showWindow(val);
    }
    
    void closeVisualisation() {
        //visu.closeWindow();
    }
}
