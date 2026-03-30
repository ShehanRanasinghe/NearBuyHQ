-- ============================================================
-- NearBuyHQ  ·  Supabase Setup Script
-- Run this once in the Supabase SQL Editor
-- (Dashboard → SQL Editor → New query → paste & run)
-- ============================================================


-- ── 1. Products table ─────────────────────────────────────
CREATE TABLE IF NOT EXISTS public.products (
    id            BIGSERIAL PRIMARY KEY,
    firebase_id   TEXT UNIQUE,          -- Firebase document ID (used for PATCH updates)
    shop_id       TEXT NOT NULL,        -- Firebase UID of the shop owner
    name          TEXT NOT NULL,
    description   TEXT,
    category      TEXT,
    unit          TEXT,
    price         NUMERIC(10, 2) DEFAULT 0,
    quantity      INTEGER        DEFAULT 0,
    status        TEXT,
    image_url     TEXT,                 -- Supabase Storage public URL
    created_at    BIGINT,               -- epoch ms (mirrors Firebase)
    updated_at    BIGINT,               -- epoch ms (mirrors Firebase)
    inserted_at   TIMESTAMPTZ DEFAULT NOW()
);

-- Index for quick lookups by shop
CREATE INDEX IF NOT EXISTS idx_products_shop_id ON public.products (shop_id);


-- ── 2. Row-Level Security ────────────────────────────────
-- Keep RLS disabled while you're developing so the anon key
-- can INSERT/UPDATE freely.  Enable and add policies before
-- going to production.

ALTER TABLE public.products DISABLE ROW LEVEL SECURITY;

-- Example policy for when you want to restrict:
-- ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
-- CREATE POLICY "anon can insert" ON public.products FOR INSERT WITH CHECK (true);
-- CREATE POLICY "anon can update" ON public.products FOR UPDATE USING (true);
-- CREATE POLICY "anon can select" ON public.products FOR SELECT USING (true);


-- ── 3. Storage bucket ────────────────────────────────────
-- Run in the Supabase SQL Editor OR just create the bucket in
-- Storage → New Bucket → name: "product-images" → Public: ON

INSERT INTO storage.buckets (id, name, public)
VALUES ('product-images', 'product-images', true)
ON CONFLICT (id) DO NOTHING;

-- Allow anonymous uploads (remove / tighten in production)
CREATE POLICY "anon upload product images"
    ON storage.objects FOR INSERT
    WITH CHECK (bucket_id = 'product-images');

CREATE POLICY "public read product images"
    ON storage.objects FOR SELECT
    USING (bucket_id = 'product-images');

