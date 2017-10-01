package com.tapsi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class KNXHandler {

    public void setItem() throws IOException {
        String url = "http://192.168.1.112:8080/rest/items/og_schlafzimmer/state";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("GET");
        //con.setRequestProperty("Content-Type", "text/plain");
        //con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        //String urlParameters = "ON";

        // Send post request
        //con.setDoOutput(true);
        //DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        //wr.writeBytes(urlParameters);
        //wr.flush();
        //wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        //System.out.println("Post parameters : " + urlParameters);
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
}
