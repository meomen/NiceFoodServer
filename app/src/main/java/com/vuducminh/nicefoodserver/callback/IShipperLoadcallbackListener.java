package com.vuducminh.nicefoodserver.callback;

import com.vuducminh.nicefoodserver.model.ShipperModel;

import java.util.List;

public interface IShipperLoadcallbackListener {
    void onShipperLoadSuccess(List<ShipperModel> shipperModelList);
    void onShipperLoadFailed(String message);
}
