package com.vuducminh.nicefoodserver.callback;

import com.vuducminh.nicefoodserver.model.BestDealsModel;

import java.util.List;
 //lắng nghe sự kiện item món ăn đề xuất(Best Deal) được chọn
public interface IBestDealsCallbackListener {
    void onListBestDealsLoadSuccess(List<BestDealsModel> bestDealsModels);
    void onListBestDealsLoadFailed(String message);
}
