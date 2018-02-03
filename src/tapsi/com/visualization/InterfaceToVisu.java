package tapsi.com.visualization;

import tapsi.com.GeoDoorServer2;
import tapsi.com.logging.LogHandler;

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
        GeoDoorServer2.stopServer();

    }

    public void showVisualization(boolean val) {
        //visu.showWindow(val);
    }

    void closeVisualisation() {
        //visu.closeWindow();
    }
}
