package com.vuducminh.nicefoodserver.Callback;

import com.vuducminh.nicefoodserver.Model.CategoryModel;
import com.vuducminh.nicefoodserver.Model.OrderModel;

import java.util.List;

public interface IOrderCallbackListerner {
    void onOrderLoadSuccess(List<OrderModel> orderModels);
    void onOrderLoadFailed(String message);
}
