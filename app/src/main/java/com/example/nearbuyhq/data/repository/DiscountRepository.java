package com.example.nearbuyhq.data.repository;

import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.data.remote.firebase.FirebaseCollections;
import com.example.nearbuyhq.discounts.Deal;
import com.example.nearbuyhq.discounts.Promotion;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * All deal/promotion data lives under NearBuyHQ/{userId}/deals
 * and NearBuyHQ/{userId}/promotions respectively.
 */
public class DiscountRepository {

    private final FirebaseFirestore db;

    public DiscountRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // ── Subcollection refs ────────────────────────────────────────────────

    private CollectionReference dealsRef(String userId) {
        return db.collection(FirebaseCollections.USERS)
                 .document(userId)
                 .collection(FirebaseCollections.USER_DEALS);
    }

    private CollectionReference promotionsRef(String userId) {
        return db.collection(FirebaseCollections.USERS)
                 .document(userId)
                 .collection(FirebaseCollections.USER_PROMOTIONS);
    }

    // ══ PROMOTIONS ════════════════════════════════════════════════════════

    public void savePromotion(Promotion promotion, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }

        String userId = promotion.getUserId();
        if (userId == null || userId.isEmpty()) { callback.onError(new IllegalArgumentException("userId is required")); return; }

        String id = promotion.getId();
        if (id == null || id.trim().isEmpty()) {
            id = promotionsRef(userId).document().getId();
            promotion.setId(id);
        }

        promotionsRef(userId).document(id)
                .set(promotion.toMap())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /** Load all promotions for one owner. */
    public void getPromotionsByUserId(String userId, DataCallback<List<Promotion>> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }

        promotionsRef(userId).orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snaps -> {
                    List<Promotion> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snaps.getDocuments()) {
                        Promotion p = Promotion.fromMap(doc.getId(), doc.getData());
                        if (p != null) list.add(p);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }

    /** @deprecated Use getPromotionsByUserId instead */
    public void getPromotions(DataCallback<List<Promotion>> callback) {
        callback.onError(new IllegalStateException("userId required – use getPromotionsByUserId(userId, cb)"));
    }

    public void getPromotion(String promotionId, String userId, DataCallback<Promotion> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }

        promotionsRef(userId).document(promotionId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) { callback.onError(new IllegalStateException("Promotion not found")); return; }
                    callback.onSuccess(Promotion.fromMap(doc.getId(), doc.getData()));
                })
                .addOnFailureListener(callback::onError);
    }

    public void deletePromotion(String promotionId, String userId, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }

        promotionsRef(userId).document(promotionId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /** @deprecated pass userId */
    public void deletePromotion(String promotionId, OperationCallback callback) {
        callback.onError(new IllegalStateException("userId required – use deletePromotion(id, userId, cb)"));
    }

    // ══ DEALS ═════════════════════════════════════════════════════════════

    public void saveDeal(Deal deal, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }

        String userId = deal.getUserId();
        if (userId == null || userId.isEmpty()) { callback.onError(new IllegalArgumentException("userId is required")); return; }

        String id = deal.getId();
        if (id == null || id.trim().isEmpty()) {
            id = dealsRef(userId).document().getId();
            deal.setId(id);
        }

        dealsRef(userId).document(id)
                .set(deal.toMap())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /** Load all deals for one owner. */
    public void getDealsByUserId(String userId, DataCallback<List<Deal>> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }

        dealsRef(userId).orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snaps -> {
                    List<Deal> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snaps.getDocuments()) {
                        Deal d = Deal.fromMap(doc.getId(), doc.getData());
                        if (d != null) list.add(d);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }

    /** @deprecated Use getDealsByUserId instead */
    public void getDeals(DataCallback<List<Deal>> callback) {
        callback.onError(new IllegalStateException("userId required – use getDealsByUserId(userId, cb)"));
    }

    public void getDeal(String dealId, String userId, DataCallback<Deal> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }

        dealsRef(userId).document(dealId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) { callback.onError(new IllegalStateException("Deal not found")); return; }
                    callback.onSuccess(Deal.fromMap(doc.getId(), doc.getData()));
                })
                .addOnFailureListener(callback::onError);
    }

    public void deleteDeal(String dealId, String userId, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }

        dealsRef(userId).document(dealId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /** @deprecated pass userId */
    public void deleteDeal(String dealId, OperationCallback callback) {
        callback.onError(new IllegalStateException("userId required – use deleteDeal(id, userId, cb)"));
    }
}
