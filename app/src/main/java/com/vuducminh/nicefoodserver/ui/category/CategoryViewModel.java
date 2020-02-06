package com.vuducminh.nicefoodserver.ui.category;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vuducminh.nicefoodserver.Callback.ICategoryCallbackListener;
import com.vuducminh.nicefoodserver.Common.CommonAgr;
import com.vuducminh.nicefoodserver.Model.CategoryModel;

import java.util.ArrayList;
import java.util.List;

public class CategoryViewModel extends ViewModel implements ICategoryCallbackListener {

    private MutableLiveData<List<CategoryModel>> categoryList;
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private ICategoryCallbackListener categoryCallbackListener;

    public CategoryViewModel() {
        categoryCallbackListener = this;
    }


    public MutableLiveData<List<CategoryModel>> getCategoryList() {
        if(categoryList == null) {
            categoryList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadCategories();
        }
        return categoryList;
    }

    private void loadCategories() {
        List<CategoryModel> tempList = new ArrayList<>();
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference(CommonAgr.CATEGORY_REF);
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot itemSnaShot:dataSnapshot.getChildren()) {
                    CategoryModel categoryModel = itemSnaShot.getValue(CategoryModel.class);
                    categoryModel.setMenu_id(itemSnaShot.getKey());
                    tempList.add(categoryModel);
                }
                categoryCallbackListener.onCategoryLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                categoryCallbackListener.onCategoryLoadFailed(databaseError.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onCategoryLoadSuccess(List<CategoryModel> categoryModelList) {
        categoryList.setValue(categoryModelList);
    }

    @Override
    public void onCategoryLoadFailed(String message) {
        messageError.setValue(message);
    }
}