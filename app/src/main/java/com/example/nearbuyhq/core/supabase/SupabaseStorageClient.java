package com.example.nearbuyhq.core.supabase;

import android.net.Uri;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Uploads images to Supabase Storage bucket "product-images".
 * Returns a publicly accessible URL on success.
 */
public class SupabaseStorageClient {

    public interface UploadCallback {
        void onSuccess(String publicUrl);
        void onError(Exception e);
    }

    private static final OkHttpClient HTTP = new OkHttpClient();

    /**
     * Upload the image pointed to by {@code imageUri} and deliver the public URL
     * on the main thread via {@code callback}.
     */
    public static void uploadProductImage(Context context, Uri imageUri, UploadCallback callback) {
        String fileName = "products/" + UUID.randomUUID() + ".jpg";

        byte[] imageBytes;
        try {
            imageBytes = readBytes(context, imageUri);
        } catch (IOException e) {
            callback.onError(e);
            return;
        }

        Request request = new Request.Builder()
                .url(SupabaseConfig.STORAGE_UPLOAD_URL + fileName)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.ANON_KEY)
                .addHeader("x-upsert", "true")
                .post(RequestBody.create(imageBytes, MediaType.parse("image/jpeg")))
                .build();

        HTTP.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean success = response.isSuccessful();
                String errorBody = "";
                int code = response.code();
                try {
                    if (!success && response.body() != null) {
                        errorBody = response.body().string();
                    }
                } finally {
                    response.close();
                }
                if (success) {
                    String publicUrl = SupabaseConfig.STORAGE_PUBLIC_URL + fileName;
                    callback.onSuccess(publicUrl);
                } else {
                    callback.onError(new IOException("Storage upload failed (" + code + "): " + errorBody));
                }
            }
        });
    }

    private static byte[] readBytes(Context context, Uri uri) throws IOException {
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            if (is == null) throw new IOException("Cannot open URI: " + uri);
            return is.readAllBytes();
        }
    }
}


