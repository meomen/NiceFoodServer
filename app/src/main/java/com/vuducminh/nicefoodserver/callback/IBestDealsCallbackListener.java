package com.vuducminh.nicefoodserver.callback;

import com.vuducminh.nicefoodserver.model.BestDealsModel;

import java.util.List;

public interface IBestDealsCallbackListener {
    void onListBestDealsLoadSuccess(List<BestDealsModel> bestDealsModels);
    void onListBestDealsLoadFailed(String message);
}
