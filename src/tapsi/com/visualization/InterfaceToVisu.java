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
        GeoDoorServer2.startServer();
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
