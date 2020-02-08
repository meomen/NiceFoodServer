package com.vuducminh.nicefoodserver.callback;

import com.vuducminh.nicefoodserver.model.CategoryModel;

import java.util.List;

public interface ICategoryCallbackListener {
    void onCategoryLoadSuccess(List<CategoryModel> CategoryModels);
    void onCategoryLoadFailed(String message);
}
