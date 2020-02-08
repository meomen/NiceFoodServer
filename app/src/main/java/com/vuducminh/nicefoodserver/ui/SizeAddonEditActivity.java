package com.vuducminh.nicefoodserver.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.vuducminh.nicefoodserver.adapter.MyAddonAdapter;
import com.vuducminh.nicefoodserver.adapter.MySizeAdapter;
import com.vuducminh.nicefoodserver.common.Common;
import com.vuducminh.nicefoodserver.common.CommonAgr;
import com.vuducminh.nicefoodserver.eventbus.AddonSizeEditEvent;
import com.vuducminh.nicefoodserver.eventbus.SelectAddonModel;
import com.vuducminh.nicefoodserver.eventbus.SelectSizeModel;
import com.vuducminh.nicefoodserver.eventbus.UpdateAddonModel;
import com.vuducminh.nicefoodserver.eventbus.UpdateSizeModel;
import com.vuducminh.nicefoodserver.model.AddonModel;
import com.vuducminh.nicefoodserver.model.SizeModel;
import com.vuducminh.nicefoodserver.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SizeAddonEditActivity extends AppCompatActivity {

    private MySizeAdapter sizeAdapter;
    private MyAddonAdapter addonAdapter;
    private int foodEditPosition = -1;
    private boolean needsave = false;
    private boolean isAddon = false;

    @BindView(R.id.tool_bar)
    Toolbar toolbar;
    @BindView(R.id.edt_name)
    EditText edt_name;
    @BindView(R.id.edt_price)
    EditText edt_price;
    @BindView(R.id.btn_create)
    Button btn_create;
    @BindView(R.id.btn_edit)
    Button btn_edit;
    @BindView(R.id.recycler_addon_size)
    RecyclerView recycler_addon_size;

    @OnClick(R.id.btn_create)
    void onCreateNew() {
        if(!isAddon) {
            if(sizeAdapter != null) {
                SizeModel sizeModel = new SizeModel();
                sizeModel.setName(edt_name.getText().toString());
                sizeModel.setPrice(Long.valueOf(edt_price.getText().toString()));
                sizeAdapter.addNewSize(sizeModel);
            }
        }
        else{
            if(addonAdapter != null) {
                AddonModel addonModel = new AddonModel();
                addonModel.setName(edt_name.getText().toString());
                addonModel.setPrice(Long.valueOf(edt_price.getText().toString()));
                addonAdapter.addNewAddon(addonModel);
            }

        }
    }

    @OnClick(R.id.btn_edit)
    void onEdit() {
        if(!isAddon) {
            if(sizeAdapter != null) {
                SizeModel sizeModel = new SizeModel();
                sizeModel.setName(edt_name.getText().toString());
                sizeModel.setPrice(Long.valueOf(edt_price.getText().toString()));
                sizeAdapter.editSize(sizeModel);
            }
        }
        else {
            if(addonAdapter != null) {
                AddonModel addonModel = new AddonModel();
                addonModel.setName(edt_name.getText().toString());
                addonModel.setPrice(Long.valueOf(edt_price.getText().toString()));
                addonAdapter.editAddon(addonModel);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_size_addon_edit);
        init();
    }

    private void init() {
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recycler_addon_size.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_addon_size.setLayoutManager(layoutManager);
        recycler_addon_size.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.addon_size_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save: {
                savaData();
                break;
            }
            case android.R.id.home: {
                if (needsave) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Cancle?")
                            .setMessage("Do you really close without saving ?")
                            .setNegativeButton("CANCLE", (dialogInterface, which) -> {
                                dialogInterface.dismiss();
                            })
                            .setPositiveButton("OK", (dialogInterface, which) -> {
                                dialogInterface.dismiss();
                                needsave = false;
                                closeActivity();
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    closeActivity();
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    private void savaData() {
        if (foodEditPosition != -1) {
            Common.categorySelected.getFoods().set(foodEditPosition, Common.selectedFood);

            Map<String, Object> updateData = new HashMap<>();
            updateData.put("foods", Common.categorySelected.getFoods());

            FirebaseDatabase.getInstance()
                    .getReference(CommonAgr.CATEGORY_REF)
                    .child(Common.categorySelected.getMenu_id())
                    .updateChildren(updateData)
                    .addOnFailureListener(e -> {
                        Toast.makeText(SizeAddonEditActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SizeAddonEditActivity.this, "Reload success!", Toast.LENGTH_SHORT).show();
                            needsave = false;
                            edt_name.setText("");
                            edt_price.setText("0");
                        }
                    });
        }
    }

    private void closeActivity() {
        edt_name.setText("");
        edt_price.setText("0");
        finish();
    }

    //Register event

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().removeStickyEvent(UpdateSizeModel.class);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onStop();
    }

    //Receive Event
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onAddonSizeReceive(AddonSizeEditEvent event) {
        if (!event.isAddon()) {
            if (Common.selectedFood.getSize() != null) {
                sizeAdapter = new MySizeAdapter(this, Common.selectedFood.getSize());
                foodEditPosition = event.getPosition();
                recycler_addon_size.setAdapter(sizeAdapter);
                isAddon = event.isAddon();
            }
        }
        else {
            if (Common.selectedFood.getAddon() != null) {
                addonAdapter = new MyAddonAdapter(this, Common.selectedFood.getAddon());
                foodEditPosition = event.getPosition();
                recycler_addon_size.setAdapter(addonAdapter);
                isAddon = event.isAddon();
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSizeModelUpdate(UpdateSizeModel event) {

        if (event.getSizeModelList() != null) {
            needsave = true;
            Common.selectedFood.setSize(event.getSizeModelList());
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onAddonModelUpdate(UpdateAddonModel event) {

        if (event.getAddonModelList() != null) {
            needsave = true;
            Common.selectedFood.setAddon(event.getAddonModelList());
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSelectSizeModel(SelectSizeModel event) {

        if(event.getSizeModel() != null) {
            edt_name.setText(event.getSizeModel().getName());
            edt_price.setText(String.valueOf(event.getSizeModel().getPrice()));

            btn_edit.setEnabled(true);
        }
        else {
            btn_edit.setEnabled(false);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSelectAddonModel(SelectAddonModel event) {

        if(event.getAddonModel() != null) {
            edt_name.setText(event.getAddonModel().getName());
            edt_price.setText(String.valueOf(event.getAddonModel().getPrice()));

            btn_edit.setEnabled(true);
        }
        else {
            btn_edit.setEnabled(false);
        }
    }
}
