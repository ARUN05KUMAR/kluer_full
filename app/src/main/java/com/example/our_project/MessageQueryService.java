package com.example.our_project;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MessageQueryService extends IntentService {

    public static final String EXTRA_MESSAGE = "message_text";
    public static final String ACTION_REPLY_RECEIVED = "com.example.our_project.ACTION_REPLY_RECEIVED";
    public static final String EXTRA_REPLY = "reply_message";

    public MessageQueryService() {
        super("MessageQueryService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String message = intent.getStringExtra(EXTRA_MESSAGE);

        try {
            URL url = new URL("https://pazhayasoru-kluer-docker.hf.space/retrieve");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("query", message);
            String jsonBody = jsonObject.toString();

            OutputStream os = conn.getOutputStream();
            os.write(jsonBody.getBytes("utf-8"));
            os.flush();
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while((line = br.readLine()) != null){
                response.append(line);
            }
            br.close();

            String replyMessage = "";
            try {
                JSONObject jsonResponse = new JSONObject(response.toString());
                replyMessage = jsonResponse.getString("message").trim();
            } catch (Exception e) {
                Log.e("Service", "Error parsing JSON", e);
                replyMessage = "Error parsing reply";
            }


            Intent replyIntent = new Intent(ACTION_REPLY_RECEIVED);
            replyIntent.putExtra(EXTRA_REPLY, replyMessage);
            sendBroadcast(replyIntent);


        } catch (Exception e) {
            Log.e("Service", "Error sending message", e);
        }
    }
}
