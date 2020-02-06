package com.vuducminh.nicefoodserver.Callback;

import com.vuducminh.nicefoodserver.Model.CategoryModel;

import java.util.List;

public interface ICategoryCallbackListener {
    void onCategoryLoadSuccess(List<CategoryModel> CategoryModels);
    void onCategoryLoadFailed(String message);
}
