package com.example.nearbuyhq.core.supabase;

/**
 * Supabase project constants.
 * SUPABASE_URL  – the REST base URL for your project.
 * ANON_KEY      – the publishable / anon key (safe to ship in the APK).
 */
public final class SupabaseConfig {

    public static final String SUPABASE_URL = "https://dsujnyhfjmwkxozwwzaw.supabase.co";
    public static final String ANON_KEY     = "sb_publishable_iBCdlSbemtr6zonNs_vP3g_4dr8k77v";

    /** Name of the Storage bucket that holds product images. */
    public static final String STORAGE_BUCKET = "product-images";

    /** REST endpoint for the products table. */
    public static final String PRODUCTS_ENDPOINT = SUPABASE_URL + "/rest/v1/products";

    /** Storage upload base URL. */
    public static final String STORAGE_UPLOAD_URL =
            SUPABASE_URL + "/storage/v1/object/" + STORAGE_BUCKET + "/";

    /** Storage public URL base (for reading back the image). */
    public static final String STORAGE_PUBLIC_URL =
            SUPABASE_URL + "/storage/v1/object/public/" + STORAGE_BUCKET + "/";

    private SupabaseConfig() { /* no instances */ }
}

