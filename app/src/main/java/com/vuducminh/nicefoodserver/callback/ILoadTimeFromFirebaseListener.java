package com.vuducminh.nicefoodserver.callback;

import com.vuducminh.nicefoodserver.model.OrderModel;

public interface ILoadTimeFromFirebaseListener {
    void onLoadTimeSuccess(OrderModel orderModel, long estimateTimeInMs);
    void onLoadOnlyTimeSuccess( long estimateTimeInMs);
    void onLoadtimeFailed(String message);
}
