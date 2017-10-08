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
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

public class KNXHandler {

    public Timer timer = null;
    public TimerTask timerTask = null;

    public KNXHandler() {
        timerTask = new RepeatTimer();
        timer = new Timer(true);
        timer.schedule(timerTask,0,10 * 1000);
    }

    public void setItem(String parameter) throws IOException {
        String url = "http://192.168.1.112:8080/rest/items/eg_buero";
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
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.err.println(response.toString());
    }

    public String getItem() {

        timer.cancel();
        System.out.println("timer canceled");

        String url = "http://192.168.1.112:8080/rest/items/eg_buero/state";
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
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

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

    public class  RepeatTimer extends java.util.TimerTask {

        @Override
        public void run() {
            System.out.println("started timer @:" + new Date());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                LogHandler.handleError(e);
            }
            System.out.println("ended timer @:" + new Date());
        }
    }
}
