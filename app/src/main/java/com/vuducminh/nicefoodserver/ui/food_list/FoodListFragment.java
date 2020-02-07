package com.vuducminh.nicefoodserver.ui.food_list;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.vuducminh.nicefoodserver.Adapter.MyFoodListAdapter;
import com.vuducminh.nicefoodserver.Common.Common;
import com.vuducminh.nicefoodserver.Common.CommonAgr;
import com.vuducminh.nicefoodserver.Common.MySwiperHelper;
import com.vuducminh.nicefoodserver.Model.FoodModel;
import com.vuducminh.nicefoodserver.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FoodListFragment extends Fragment {

    private FoodListViewModel foodListViewModel;

    private List<FoodModel> foodModelList;

    Unbinder unbinder;
    @BindView(R.id.recycler_food_list)
    RecyclerView recycler_food_list;

    LayoutAnimationController layoutAnimationController;
    MyFoodListAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodListViewModel =
                ViewModelProviders.of(this).get(FoodListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_list, container, false);
        unbinder = ButterKnife.bind(this,root);
        initViews();
        foodListViewModel.getMutableLiveDataFoodList().observe(this, foodModels -> {
           if(foodModels != null) {
               foodModelList = foodModels;
               adapter = new MyFoodListAdapter(getContext(),foodModelList);
               recycler_food_list.setAdapter(adapter);
               recycler_food_list.setLayoutAnimation(layoutAnimationController);
           }
        });
        return root;
    }

    private void initViews() {

        ((AppCompatActivity)getActivity())
                .getSupportActionBar()
                .setTitle(Common.categorySelected.getName()
                );

        recycler_food_list.setHasFixedSize(true);
        recycler_food_list.setLayoutManager(new LinearLayoutManager(getContext()));

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);

        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(),recycler_food_list,300) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Delete", 30, 0, Color.parseColor("#9B0000"),
                        position -> {
                           if(foodModelList != null) {
                               Common.selectedFood = foodModelList.get(position);
                               AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                               builder.setTitle("DELETE")
                                       .setMessage("Do you want to delete this food ?")
                                       .setNegativeButton("CANCLE", (dialogInterface, which) -> {
                                           dialogInterface.dismiss();
                                       })
                                       .setPositiveButton("DELETE", (dialog, which) -> {
                                           Common.categorySelected.getFoods().remove(position);
                                           updateFood(Common.categorySelected.getFoods());
                                       });

                               AlertDialog dialog = builder.create();
                               dialog.show();
                           }
                        })
                );

                buf.add(new MyButton(getContext(), "Update", 30, 0, Color.parseColor("#560027"),
                        position -> {

                        })
                );
            }
        };

    }

    private void updateFood(List<FoodModel> foods) {
        Map<String,Object> updateData = new HashMap<>();
        updateData.put("foods",foods);

        FirebaseDatabase.getInstance()
                .getReference(CommonAgr.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .updateChildren(updateData)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {
                       foodListViewModel.getMutableLiveDataFoodList();
                       Toast.makeText(getContext(),"Delete success",Toast.LENGTH_SHORT).show();
                   }
                });
    }
}