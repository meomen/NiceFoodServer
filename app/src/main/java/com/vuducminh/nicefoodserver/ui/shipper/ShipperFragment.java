package com.vuducminh.nicefoodserver.ui.shipper;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.vuducminh.nicefoodserver.R;
import com.vuducminh.nicefoodserver.adapter.MyShipperAdapter;
import com.vuducminh.nicefoodserver.common.CommonAgr;
import com.vuducminh.nicefoodserver.eventbus.ChangeMenuClick;
import com.vuducminh.nicefoodserver.eventbus.UpdateShipperEvent;
import com.vuducminh.nicefoodserver.model.ShipperModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class ShipperFragment extends Fragment {

    private ShipperViewModel mViewModel;

    private Unbinder unbinder;
    @BindView(R.id.recycler_shipper)
    RecyclerView recycler_shipper;
    private AlertDialog dialog;
    LayoutAnimationController layoutAnimationControllerl;
    MyShipperAdapter adapter;
    List<ShipperModel> shipperModelList;

    public static ShipperFragment newInstance() {
        return new ShipperFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_shipper, container, false);
        mViewModel = ViewModelProviders.of(this).get(ShipperViewModel.class);
        unbinder = ButterKnife.bind(this,itemView);
        initViews();
        mViewModel.getMessageError().observe(this, s -> {
            Toast.makeText(getContext(),""+s,Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        mViewModel.getMutableLiveDataShipper().observe(this, shipperModels -> {
            dialog.dismiss();
            shipperModelList = shipperModels;
            adapter = new MyShipperAdapter(getContext(),shipperModelList);
            recycler_shipper.setAdapter(adapter);
            recycler_shipper.setLayoutAnimation(layoutAnimationControllerl);
        });
        return itemView;
    }

    private void initViews() {
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        layoutAnimationControllerl = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_shipper.setLayoutManager(layoutManager);
        recycler_shipper.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        if(EventBus.getDefault().hasSubscriberForEvent(UpdateShipperEvent.class)) {
            EventBus.getDefault().removeStickyEvent(UpdateShipperEvent.class);
        }
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onStop();

    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onUpdateShipperActive(UpdateShipperEvent event) {
        Map<String,Object> updateData = new HashMap<>();
        updateData.put("active",event.isActive());
        FirebaseDatabase.getInstance()
                .getReference(CommonAgr.SHIPPER)
                .child(event.getShipperModel().getKey())
                .updateChildren(updateData)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                })
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(),"Update status to "+event.isActive(),Toast.LENGTH_SHORT).show();
                });
    }
}
