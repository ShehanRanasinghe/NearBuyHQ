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

public class DiscountRepository {

    private final CollectionReference promotionsRef;
    private final CollectionReference dealsRef;

    public DiscountRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.promotionsRef = db.collection(FirebaseCollections.PROMOTIONS);
        this.dealsRef = db.collection(FirebaseCollections.DEALS);
    }

    public void savePromotion(Promotion promotion, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        String id = promotion.getId();
        if (id == null || id.trim().isEmpty()) {
            id = promotionsRef.document().getId();
            promotion.setId(id);
        }

        promotionsRef.document(id)
                .set(promotion.toMap())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void getPromotions(DataCallback<List<Promotion>> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        promotionsRef.orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Promotion> promotions = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Promotion promotion = Promotion.fromMap(document.getId(), document.getData());
                        if (promotion != null) {
                            promotions.add(promotion);
                        }
                    }
                    callback.onSuccess(promotions);
                })
                .addOnFailureListener(callback::onError);
    }

    public void getPromotion(String promotionId, DataCallback<Promotion> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        promotionsRef.document(promotionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onError(new IllegalStateException("Promotion not found"));
                        return;
                    }
                    callback.onSuccess(Promotion.fromMap(documentSnapshot.getId(), documentSnapshot.getData()));
                })
                .addOnFailureListener(callback::onError);
    }

    public void deletePromotion(String promotionId, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        promotionsRef.document(promotionId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void saveDeal(Deal deal, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        String id = deal.getId();
        if (id == null || id.trim().isEmpty()) {
            id = dealsRef.document().getId();
        }

        dealsRef.document(id)
                .set(deal.toMap())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void getDeals(DataCallback<List<Deal>> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        dealsRef.orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Deal> deals = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Deal deal = Deal.fromMap(document.getId(), document.getData());
                        if (deal != null) {
                            deals.add(deal);
                        }
                    }
                    callback.onSuccess(deals);
                })
                .addOnFailureListener(callback::onError);
    }

    public void getDeal(String dealId, DataCallback<Deal> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        dealsRef.document(dealId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onError(new IllegalStateException("Deal not found"));
                        return;
                    }
                    callback.onSuccess(Deal.fromMap(documentSnapshot.getId(), documentSnapshot.getData()));
                })
                .addOnFailureListener(callback::onError);
    }

    public void deleteDeal(String dealId, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        dealsRef.document(dealId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
}

