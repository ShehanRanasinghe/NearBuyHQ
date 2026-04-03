package com.example.nearbuyhq.data.repository;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Handles uploading product images to Supabase Storage.
 * Uses the REST API directly — no Supabase Android SDK needed.
 *
 * Storage path: product-images/products/{userId}_{timestamp}.jpg
 * Public URL:   https://dsujnyhfjmwkxozwwzaw.supabase.co/storage/v1/object/public/product-images/...
 */
public class SupabaseStorageHelper {

    // ── Your Supabase project credentials ─────────────────────────────────────
    private static final String SUPABASE_URL     = "https://dsujnyhfjmwkxozwwzaw.supabase.co";
    private static final String SUPABASE_ANON_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                    "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRzdWpueWhmam13a3hvend3emF3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQ4MTAzNTYsImV4cCI6MjA5MDM4NjM1Nn0." +
                    "pPY9HPA-HbNL9uSK5v1pr2nzkQF4QsVRtE1Gs2ShgAE";
    // ─────────────────────────────────────────────────────────────────────────

    private static final String BUCKET   = "product-images";
    private static final String TAG      = "SupabaseStorage";

    private final OkHttpClient    client   = new OkHttpClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Callback interface — result always delivered on background thread
    // You MUST post to main thread yourself (Handler is used in Add_Product)
    public interface UploadCallback {
        void onSuccess(String publicUrl);
        void onError(Exception e);
    }

    /**
     * Uploads JPEG bytes to Supabase Storage.
     *
     * @param imageBytes  Compressed JPEG bytes
     * @param fileName    Path inside bucket e.g. "products/user123_1710000000.jpg"
     * @param callback    Returns public URL on success
     */
    public void uploadImage(byte[] imageBytes, String fileName, UploadCallback callback) {
        executor.execute(() -> {
            // Build the upload URL
            // Format: {SUPABASE_URL}/storage/v1/object/{BUCKET}/{fileName}
            String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET + "/" + fileName;

            RequestBody body = RequestBody.create(
                    imageBytes,
                    MediaType.parse("image/jpeg")
            );

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("x-upsert", "true")  // overwrite if file already exists
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    // Build the public URL for display
                    // Format: {SUPABASE_URL}/storage/v1/object/public/{BUCKET}/{fileName}
                    String publicUrl = SUPABASE_URL
                            + "/storage/v1/object/public/"
                            + BUCKET + "/"
                            + fileName;
                    Log.d(TAG, "Upload success → " + publicUrl);
                    callback.onSuccess(publicUrl);
                } else {
                    String errorBody = response.body() != null
                            ? response.body().string() : "No error body";
                    Log.e(TAG, "Upload failed " + response.code() + ": " + errorBody);
                    callback.onError(new Exception("Upload failed " + response.code() + ": " + errorBody));
                }
            } catch (IOException e) {
                Log.e(TAG, "Network error during upload", e);
                callback.onError(e);
            }
        });
    }
}