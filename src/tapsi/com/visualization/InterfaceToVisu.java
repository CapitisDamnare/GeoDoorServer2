package tapsi.com.visualization;

import tapsi.com.GeoDoorServer2;

public class InterfaceToVisu {
    private static Visualization visu = null;

    public InterfaceToVisu() {
        visu = new Visualization();
    }

    public void startPressed() {
        GeoDoorServer2.startServer();
    }

    public void stopPressed() {
        GeoDoorServer2.stopServer();
    }

    public void hideVisualization(boolean val) {
        visu.hideVisualization(val);
    }
}
