package com.example.nearbuyhq.data.repository;

import com.example.nearbuyhq.core.supabase.SupabaseConfig;
import com.example.nearbuyhq.products.ProductItem;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Mirrors every product create / update to the Supabase "products" table via the
 * PostgREST REST API.  Failures are non-fatal – the app already persists data in
 * Firebase; Supabase is a secondary store.
 */
public class SupabaseProductRepository {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient HTTP = new OkHttpClient();

    // ── Create ────────────────────────────────────────────────────────────

    /**
     * INSERT a new product row.  Uses {@code Prefer: return=minimal} so Supabase
     * returns 201 with no body (keeps traffic small).
     */
    public void createProduct(ProductItem item, OperationCallback callback) {
        String json = buildJson(item);
        if (json == null) { callback.onError(new IllegalArgumentException("Failed to build JSON")); return; }

        Request request = new Request.Builder()
                .url(SupabaseConfig.PRODUCTS_ENDPOINT)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(RequestBody.create(json, JSON))
                .build();

        execute(request, callback);
    }

    // ── Update ────────────────────────────────────────────────────────────

    /**
     * UPDATE an existing row matched by {@code firebase_id}.
     */
    public void updateProduct(ProductItem item, OperationCallback callback) {
        String json = buildJson(item);
        if (json == null) { callback.onError(new IllegalArgumentException("Failed to build JSON")); return; }

        // PostgREST PATCH filtered by firebase_id column
        String url = SupabaseConfig.PRODUCTS_ENDPOINT + "?firebase_id=eq." + item.getId();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .patch(RequestBody.create(json, JSON))
                .build();

        execute(request, callback);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String buildJson(ProductItem item) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("firebase_id",  item.getId());
            obj.put("shop_id",      item.getShopId());
            obj.put("name",         item.getName());
            obj.put("description",  item.getDescription());
            obj.put("category",     item.getCategory());
            obj.put("unit",         item.getUnit());
            obj.put("price",        item.getPrice());
            obj.put("quantity",     item.getQuantity());
            obj.put("status",       item.getStatus());
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                obj.put("image_url", item.getImageUrl());
            }
            obj.put("created_at",   item.getCreatedAt());
            obj.put("updated_at",   item.getUpdatedAt());
            return obj.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private void execute(Request request, OperationCallback callback) {
        HTTP.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean success = response.isSuccessful();
                String body = "";
                int code = response.code();
                try {
                    if (!success && response.body() != null) {
                        body = response.body().string();
                    }
                } finally {
                    response.close();
                }
                if (success) {
                    callback.onSuccess();
                } else {
                    callback.onError(new IOException("Supabase error (" + code + "): " + body));
                }
            }
        });
    }
}


