package com.vuducminh.nicefoodserver.callback;

import com.vuducminh.nicefoodserver.model.OrderModel;

import java.util.List;

public interface IOrderCallbackListerner {
    void onOrderLoadSuccess(List<OrderModel> orderModels);
    void onOrderLoadFailed(String message);
}
