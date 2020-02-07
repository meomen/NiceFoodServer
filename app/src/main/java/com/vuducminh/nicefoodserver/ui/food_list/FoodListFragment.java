package com.vuducminh.nicefoodserver.ui.food_list;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vuducminh.nicefoodserver.Adapter.MyFoodListAdapter;
import com.vuducminh.nicefoodserver.Common.Common;
import com.vuducminh.nicefoodserver.Common.CommonAgr;
import com.vuducminh.nicefoodserver.Common.MySwiperHelper;
import com.vuducminh.nicefoodserver.Model.FoodModel;
import com.vuducminh.nicefoodserver.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class FoodListFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1234;

    private FoodListViewModel foodListViewModel;

    private List<FoodModel> foodModelList;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private android.app.AlertDialog dialog;

    Unbinder unbinder;
    @BindView(R.id.recycler_food_list)
    RecyclerView recycler_food_list;
    private ImageView img_food;

    LayoutAnimationController layoutAnimationController;
    MyFoodListAdapter adapter;
    private Uri imageUri = null;

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

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();


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
                                           updateFood(Common.categorySelected.getFoods(),true);
                                       });

                               AlertDialog dialog = builder.create();
                               dialog.show();
                           }
                        })
                );

                buf.add(new MyButton(getContext(), "Update", 30, 0, Color.parseColor("#560027"),
                        position -> {
                            showUpdateDialog(position);
                        })
                );
            }
        };

    }

    private void showUpdateDialog(int position) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Update");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_food,null);
        EditText edt_food_name = (EditText)itemView.findViewById(R.id.edt_food_name);
        EditText edt_food_price = (EditText)itemView.findViewById(R.id.edt_food_price);
        EditText edt_food_description = (EditText)itemView.findViewById(R.id.edt_food_description);
        img_food = (ImageView)itemView.findViewById(R.id.img_food);

        // Set Date
        edt_food_name.setText(new StringBuilder("")
                .append(Common.categorySelected.getFoods().get(position).getName()));
        edt_food_price.setText(new StringBuilder("")
                .append(Common.categorySelected.getFoods().get(position).getPrice()));
        edt_food_description.setText(new StringBuilder("")
                .append(Common.categorySelected.getFoods().get(position).getDescription()));

        Glide.with(getContext()).load(Common.categorySelected.getFoods().get(position).getImage()).into(img_food);

        img_food.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
        });

        builder.setNegativeButton("CANCLE", (dialogInterface, which) -> {
            dialogInterface.dismiss();
        }).setPositiveButton("UPDATE", (dialogInterface, which) -> {
            FoodModel foodModel = Common.categorySelected.getFoods().get(position);
            foodModel.setName(edt_food_name.getText().toString());
            foodModel.setDescription(edt_food_description.getText().toString());
            foodModel.setPrice(TextUtils.isEmpty(edt_food_price.getText()) ? 0 :
                    Long.parseLong(edt_food_price.getText().toString()));

            if(imageUri != null) {
                // firebase Storage upload image
                dialog.setMessage("Uploading...");
                dialog.show();

                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/"+unique_name);

                imageFolder.putFile(imageUri)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }).addOnCompleteListener(task -> {
                    dialog.dismiss();
                    imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                        foodModel.setImage(uri.toString());
                        Common.categorySelected.getFoods().set(position,foodModel);
                        updateFood(Common.categorySelected.getFoods(),false);
                    });
                }).addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                });
            }
            else {
                Common.categorySelected.getFoods().set(position,foodModel);
                updateFood(Common.categorySelected.getFoods(),false);
            }
        });

        builder.setView(itemView);
        AlertDialog updateDialog = builder.create();
        updateDialog.show();

    }

    private void updateFood(List<FoodModel> foods, boolean isDelete) {
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
                       if(isDelete) {
                           Toast.makeText(getContext(),"Delete success",Toast.LENGTH_SHORT).show();
                       }
                       else Toast.makeText(getContext(),"Update success",Toast.LENGTH_SHORT).show();

                   }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data != null && data.getData() != null) {
                imageUri = data.getData();
                img_food.setImageURI(imageUri);
            }
        }
    }
}