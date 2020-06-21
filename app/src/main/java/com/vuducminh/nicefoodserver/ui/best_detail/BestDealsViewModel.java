package com.vuducminh.nicefoodserver.ui.best_detail;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vuducminh.nicefoodserver.callback.IBestDealsCallbackListener;
import com.vuducminh.nicefoodserver.common.Common;
import com.vuducminh.nicefoodserver.common.CommonAgr;
import com.vuducminh.nicefoodserver.model.BestDealsModel;

import java.util.ArrayList;
import java.util.List;

public class BestDealsViewModel extends ViewModel implements IBestDealsCallbackListener {
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private MutableLiveData<List<BestDealsModel>> bestDealsListMutable;
    private IBestDealsCallbackListener bestDealsCallbackListener;

    public BestDealsViewModel() {
        bestDealsCallbackListener = this;
    }

    public MutableLiveData<List<BestDealsModel>> getBestDealsListMutable() {
        if(bestDealsListMutable == null) {
            bestDealsListMutable = new MutableLiveData<>();
        }
        loadBestDeals();
        return bestDealsListMutable;
    }

    public void loadBestDeals() {
        List<BestDealsModel> temp = new ArrayList<>();
        DatabaseReference bestDealsRef = FirebaseDatabase.getInstance()
                .getReference(CommonAgr.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(CommonAgr.BEST_DEALS);
        bestDealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot bestdealsSnapshot: dataSnapshot.getChildren()) {
                    BestDealsModel bestDealsModel = bestdealsSnapshot.getValue(BestDealsModel.class);
                    bestDealsModel.setKey(bestdealsSnapshot.getKey());
                    temp.add(bestDealsModel);
                }
                bestDealsCallbackListener.onListBestDealsLoadSuccess(temp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                bestDealsCallbackListener.onListBestDealsLoadFailed(databaseError.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onListBestDealsLoadSuccess(List<BestDealsModel> bestDealsModels) {
        bestDealsListMutable.setValue(bestDealsModels);
    }

    @Override
    public void onListBestDealsLoadFailed(String message) {
        messageError.setValue(message);
    }
}