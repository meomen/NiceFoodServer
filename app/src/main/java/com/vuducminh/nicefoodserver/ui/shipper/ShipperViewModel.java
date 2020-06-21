package com.vuducminh.nicefoodserver.ui.shipper;

import android.widget.Button;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vuducminh.nicefoodserver.callback.IShipperLoadcallbackListener;
import com.vuducminh.nicefoodserver.common.Common;
import com.vuducminh.nicefoodserver.common.CommonAgr;
import com.vuducminh.nicefoodserver.model.OrderModel;
import com.vuducminh.nicefoodserver.model.ShipperModel;

import java.util.ArrayList;
import java.util.List;

public class ShipperViewModel extends ViewModel implements IShipperLoadcallbackListener {
    private MutableLiveData<List<ShipperModel>> mutableLiveDataShipper;
    private MutableLiveData<String> messageError;
    private IShipperLoadcallbackListener shipperLoadcallbackListener;

    public ShipperViewModel() {
        this.messageError = new MutableLiveData<>();
        this.shipperLoadcallbackListener = this;
    }

    public MutableLiveData<List<ShipperModel>> getMutableLiveDataShipper() {
        if(mutableLiveDataShipper == null) {
            mutableLiveDataShipper  = new MutableLiveData<>();
            loadShipper();
        }
        return mutableLiveDataShipper;
    }

    private void loadShipper() {
        List<ShipperModel> tempList = new ArrayList<>();
        DatabaseReference shipperRef = FirebaseDatabase.getInstance()
                .getReference(CommonAgr.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(CommonAgr.SHIPPER);
        shipperRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot shipperSnapShot:dataSnapshot.getChildren()) {
                    ShipperModel shipperModel = shipperSnapShot.getValue(ShipperModel.class);
                    shipperModel.setKey(shipperSnapShot.getKey());
                    tempList.add(shipperModel);
                }

                shipperLoadcallbackListener.onShipperLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                shipperLoadcallbackListener.onShipperLoadFailed(databaseError.getMessage());
            }
        });

    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onShipperLoadSuccess(List<ShipperModel> shipperModelList) {
        if(shipperModelList != null) {
            mutableLiveDataShipper.setValue(shipperModelList);
        }
    }

    @Override
    public void onShipperLoadSuccess(int position, OrderModel orderModel, List<ShipperModel> shipperModels, AlertDialog dialog, Button btn_ok, Button btn_cancle, RadioButton rdi_shipping, RadioButton rdi_shipped, RadioButton rdi_cancelled, RadioButton rdi_delete, RadioButton rdi_restore_placed) {

    }

    @Override
    public void onShipperLoadFailed(String message) {
        messageError.setValue(message);
    }
}
