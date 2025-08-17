package com.example.our_project;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;


public class UploadService extends Service {

    public static final String EXTRA_IMAGE_URIS = "image_uris";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        List<Uri> imageUris = intent.getParcelableArrayListExtra(EXTRA_IMAGE_URIS);
        if (imageUris != null) {
            uploadImages(imageUris);
        }
        return START_NOT_STICKY;
    }

    private void uploadImages(List<Uri> imageUris) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                for (Uri uri : imageUris) {
                    MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

                    File file = createTempFileFromUri(uri);
                    RequestBody body = RequestBody.create(file, MediaType.parse("image/jpeg"));

                    // Use field name "image" as expected by the API
                    builder.addFormDataPart("image", file.getName(), body);

                    Request request = new Request.Builder()
                            .url("https://pazhayasoru-kluer-docker.hf.space/input_image")
                            .post(builder.build())
                            .build();

                    Response response = client.newCall(request).execute();
                    Log.d("UploadService", "Image upload response: " + response.code());

                    Intent intent = new Intent("UPLOAD_COMPLETE");
                    intent.putExtra("success", response.isSuccessful());
                    sendBroadcast(intent);
                }

                stopSelf();

            } catch (Exception e) {
                e.printStackTrace();
                stopSelf();
            }
        }).start();
    }


    private File createTempFileFromUri(Uri uri) throws IOException {
        File temp = File.createTempFile("upload_", ".jpg", getCacheDir());
        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(temp)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
        }
        return temp;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

