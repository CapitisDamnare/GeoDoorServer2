package tapsi.com.knx;

import tapsi.com.logging.LogHandler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class KNXHandler {

    private String address = "http://192.168.1.114:8080";

    public Thread autoModeThread = null;
    public AutoModeTimer autoModeTimer = null;

    public Timer timer = null;
    public TimerTask timerTask = null;

    public Thread doorThread = null;
    public DoorTimer doorTimer = null;

    // doorStatus: 0-Closed; 1-is Opening; 2-Stopped in between; 3-is Closing, 4-Open
    public int doorStatus = 0;
    public boolean doorIsOpening = false;

    public boolean highFlag = true;

    private KNXListener listener;

    public interface KNXListener {
        public void onDoorStatChanged(int value);
        public void onAutomaticDoorClose();
    }

    public void setCustomListener(KNXListener listener) {
        this.listener = listener;
    }

    public KNXHandler() {
        LogHandler.printLog(new Date() + ": KNX Handler started...");
        startTimer();
    }

    public void getDoorStatus() {
        listener.onDoorStatChanged(doorStatus);
    }

    public void setOpenDoorStatus(boolean value) {
        if (value)
            doorStatus = 4;
        else
            doorStatus = 0;
    }

    public void setItem(String item, String parameter) throws IOException {
        String url = address + "/rest/items/" + item;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "text/plain");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        String urlParameters = parameter;

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        //LogHandler.printLog("\nSending 'POST' request to URL : " + url);
        //LogHandler.printLog("Post parameters : " + urlParameters);
        //LogHandler.printLog("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        LogHandler.printLog(new Date() + ": knx response code: " +String.valueOf(responseCode));
    }

    public String getItem(String item) {
        String url = address + "/rest/items/" + item + "/state";
        String value = "";

        URL obj = null;
        try {
            obj = new URL(url);
        } catch (MalformedURLException e) {
            LogHandler.handleError(e);
        }

        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) obj.openConnection();
        } catch (IOException e) {
            LogHandler.handleError(e);
        }

        try {
            con.setRequestMethod("GET");
        } catch (ProtocolException e) {
            LogHandler.handleError(e);
        }
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = 0;
        try {
            responseCode = con.getResponseCode();
            //LogHandler.printLog("\nSending 'GET' request to URL : " + url);
            //LogHandler.printLog("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            value = response.toString();
        } catch (IOException e) {
            LogHandler.handleError(e);
        }
        return value;
    }

    public void stopTimer () {
        timer.cancel();
    }

    public void startTimer() {
        timerTask = new RepeatTimer();
        timer = new Timer(true);
        timer.schedule(timerTask,0,200);
    }

    public void startAutoModeTimer () {
        autoModeTimer = new AutoModeTimer();
        autoModeThread = new Thread(autoModeTimer);
        autoModeThread.start();
    }

    public class RepeatTimer extends TimerTask {

        @Override
        public void run() {
            if(Objects.equals(getItem("eg_tor_stat"), "ON")) {

                if (!highFlag)
                    return;

                highFlag = false;

                if (doorStatus == 0) {
                    // door is closed and will be opened
                    doorStatus = 1;
                    doorTimer = new DoorTimer();
                    doorThread = new Thread(doorTimer);
                    doorThread.start();
                    doorIsOpening = true;
                }
                else if (doorStatus == 1) {
                    // door was opening and is stopped
                    doorStatus = 2;
                    doorTimer.setCounter(0);
                    listener.onDoorStatChanged(2);
                }
                else if (doorStatus == 2) {
                    // door was stopped
                    // door will be closed if the door was opening before
                    if (doorIsOpening) {
                        doorStatus = 3;
                        doorTimer = new DoorTimer();
                        doorThread = new Thread(doorTimer);
                        doorThread.start();
                        doorIsOpening = false;
                    }
                    else {
                        // door will be opened if the door was closing before
                        doorStatus = 1;
                        doorTimer = new DoorTimer();
                        doorThread = new Thread(doorTimer);
                        doorThread.start();
                        doorIsOpening = true;
                    }
                }
                else if (doorStatus == 3) {
                    // door is closing and was stopped
                    doorStatus = 2;
                    doorTimer.setCounter(0);
                    listener.onDoorStatChanged(2);
                }
                else if (doorStatus == 4) {
                    // door is open and will be closed
                    doorStatus = 3;
                    doorTimer = new DoorTimer();
                    doorThread = new Thread(doorTimer);
                    doorThread.start();
                    doorIsOpening = false;
                }
            }
            else
                highFlag = true;
        }
    }

    // Counter to send the open or close door signal
    public class DoorTimer implements Runnable {

        boolean autoMode = false;
        int counter = 76;

        public void setCounter(int counter) {
            this.counter = counter;
        }

        @Override
        public void run() {
            while (counter > 0) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    LogHandler.handleError(e);
                }
                counter--;
            }

            if (doorStatus == 2)
                return;

            if (doorStatus == 1) {
                doorStatus = 4;
                listener.onDoorStatChanged(4);
            }

            if (doorStatus == 3) {
                doorStatus = 0;
                listener.onDoorStatChanged(0);
            }
        }
    }

    public class AutoModeTimer implements Runnable {

        int counter = 180;

        public void setCounter(int counter) {
            this.counter = counter;
        }

        @Override
        public void run() {
            while (counter > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LogHandler.handleError(e);
                }
                counter--;
            }
            if (counter != -99)
                listener.onAutomaticDoorClose();
        }
    }
}
