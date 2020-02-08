package com.vuducminh.nicefoodserver.ui.order;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vuducminh.nicefoodserver.callback.IOrderCallbackListerner;
import com.vuducminh.nicefoodserver.common.CommonAgr;
import com.vuducminh.nicefoodserver.model.OrderModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderViewModel extends ViewModel implements IOrderCallbackListerner {

    private MutableLiveData<List<OrderModel>> mutableLiveDataOrderModel;
    private MutableLiveData<String> messageError;

    private IOrderCallbackListerner listerner;

    public OrderViewModel() {
        mutableLiveDataOrderModel = new MutableLiveData<>();
        messageError = new MutableLiveData<>();
        listerner = this;
    }

    public MutableLiveData<List<OrderModel>> getMutableLiveDataOrderModel() {
        loadOrderByStatus(0);
        return mutableLiveDataOrderModel;
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    public void loadOrderByStatus(int status) {
        List<OrderModel> tempList = new ArrayList<>();
        Query orderRef = FirebaseDatabase.getInstance().getReference(CommonAgr.ORDER_REF)
                .orderByChild("orderStatus")
                .equalTo(status);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot itemSnapshot:dataSnapshot.getChildren()) {
                    OrderModel orderModel = itemSnapshot.getValue(OrderModel.class);
                    orderModel.setKey(itemSnapshot.getKey());
                    tempList.add(orderModel);
                }
                listerner.onOrderLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listerner.onOrderLoadFailed(databaseError.getMessage());
            }
        });
    }


    @Override
    public void onOrderLoadSuccess(List<OrderModel> orderModelList) {
        if(orderModelList.size() > 0) {
            Collections.sort(orderModelList,(orderModel,t1)->{
                if(orderModel.getCreateDate() < t1.getCreateDate()){
                    return -1;
                }
                return orderModel.getCreateDate() == t1.getCreateDate() ? 0:1;
            });
        }

        mutableLiveDataOrderModel.setValue(orderModelList);
    }



    @Override
    public void onOrderLoadFailed(String message) {
        messageError.setValue(message);
    }
}