package com.tapsi;

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

// Todo: Create a command to set open or close state from gate

public class KNXHandler {

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
    }

    public void setCustomListener(KNXListener listener) {
        this.listener = listener;
    }

    public KNXHandler() {
        startTimer();
    }

    public void setItem(String item, String parameter) throws IOException {
        String url = "http://192.168.1.112:8080/rest/items/" + item;
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
        //System.out.println("\nSending 'POST' request to URL : " + url);
        //System.out.println("Post parameters : " + urlParameters);
        //System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(new Date() + ": knx response: " +response.toString());
    }

    public String getItem(String item) {
        String url = "http://192.168.1.112:8080/rest/items/" + item + "/state";
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
            //System.out.println("\nSending 'GET' request to URL : " + url);
            //System.out.println("Response Code : " + responseCode);

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

//            System.out.println("started timer @:" + new Date());
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                LogHandler.handleError(e);
//            }
//            System.out.println("ended timer @:" + new Date());
        }
    }

    public class DoorTimer implements Runnable {

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
}
