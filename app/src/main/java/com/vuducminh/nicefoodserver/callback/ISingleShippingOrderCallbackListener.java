package com.vuducminh.nicefoodserver.callback;

import com.vuducminh.nicefoodserver.model.ShippingOrderModel;

public interface ISingleShippingOrderCallbackListener {
    void onSingleShippingOrderSuccess(ShippingOrderModel shippingOrderModel);
}
