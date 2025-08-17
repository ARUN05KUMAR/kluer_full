package com.example.our_project;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONObject;

public class MessageSendService extends IntentService {

    public static final String EXTRA_MESSAGE = "extra_message";
    public static final String ACTION_MESSAGE_SENT = "com.example.our_project.MESSAGE_SENT";

    public MessageSendService() {
        super("MessageSendService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String message = intent.getStringExtra(EXTRA_MESSAGE);
            sendMessageToApi(message);
        }
    }

    private void sendMessageToApi(String message) {
        try {
            URL url = new URL("https://pazhayasoru-kluer-docker.hf.space/input");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
            String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            String finalMessage = "On " + currentDate + ", at " + currentTime + ", message:\n" + message;

            // Safely create JSON object
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("input", finalMessage);
            String jsonInputString = jsonObject.toString();

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("Service", "Message sent successfully");
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(ACTION_MESSAGE_SENT);
                broadcastIntent.putExtra(EXTRA_MESSAGE, "Successfully");
                sendBroadcast(broadcastIntent);
            } else {
                Log.e("Service", "Failed to send: " + responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
