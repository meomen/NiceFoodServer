package com.vuducminh.nicefoodserver.callback;

import com.vuducminh.nicefoodserver.model.BestDealsModel;
import com.vuducminh.nicefoodserver.model.MostPopularModel;

import java.util.List;

public interface IMostPopularCallbackListener {
    void onListMostPopularLoadSuccess(List<MostPopularModel> mostPopularModels);
    void onListMostPopularLoadFailed(String message);
}
