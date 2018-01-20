package tapsi.com.logging;

public class GeoDoorExceptions extends Exception {
    public GeoDoorExceptions(String msg) {
        //String gotClass = this.getClass().getCanonicalName();
        super(msg);
    }
}
