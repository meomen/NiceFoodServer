package com.vuducminh.nicefoodserver.ui.order;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.vuducminh.nicefoodserver.Adapter.MyOrderAdapter;
import com.vuducminh.nicefoodserver.Common.BottomSheetOrderFragment;
import com.vuducminh.nicefoodserver.Common.Common;
import com.vuducminh.nicefoodserver.Common.CommonAgr;
import com.vuducminh.nicefoodserver.Common.MySwiperHelper;
import com.vuducminh.nicefoodserver.EventBus.AddonSizeEditEvent;
import com.vuducminh.nicefoodserver.EventBus.ChangeMenuClick;
import com.vuducminh.nicefoodserver.EventBus.LoadOrderEvent;
import com.vuducminh.nicefoodserver.Model.FoodModel;
import com.vuducminh.nicefoodserver.Model.OrderModel;
import com.vuducminh.nicefoodserver.R;
import com.vuducminh.nicefoodserver.ui.SizeAddonEditActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class OrderFragment extends Fragment {
    @BindView(R.id.recycler_order)
    RecyclerView recycler_order;
    @BindView(R.id.tv_order_filter)
    TextView tv_order_filter;

    private Unbinder unbinder;

    private OrderViewModel orderViewModel;
    private LayoutAnimationController layoutAnimationControllerl;
    private MyOrderAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        orderViewModel =
                ViewModelProviders.of(this).get(OrderViewModel.class);
        View root = inflater.inflate(R.layout.fragment_order, container, false);
        unbinder = ButterKnife.bind(this, root);
        initViews();
        orderViewModel.getMessageError().observe(this, s -> {
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
        });
        orderViewModel.getMutableLiveDataOrderModel().observe(this, orderModels -> {
            if (orderModels != null) {
                adapter = new MyOrderAdapter(getContext(), orderModels);
                recycler_order.setAdapter(adapter);
                recycler_order.setLayoutAnimation(layoutAnimationControllerl);
                tv_order_filter.setText(new StringBuilder("Orders (")
                        .append(orderModels.size())
                        .append(")"));
            }
        });
        return root;
    }

    private void initViews() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;


        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_order, width / 6) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Directions", 30, 0, Color.parseColor("#9B0000"),
                        position -> {

                        })
                );

                buf.add(new MyButton(getContext(), "Call", 30, 0, Color.parseColor("#560027"),
                        position -> {
                            Dexter.withActivity(getActivity())
                                    .withPermission(Manifest.permission.CALL_PHONE)
                                    .withListener(new PermissionListener() {
                                        @Override
                                        public void onPermissionGranted(PermissionGrantedResponse response) {
                                            OrderModel orderModel = adapter.getItemAtPosition(position);
                                            Intent intent = new Intent();
                                            intent.setAction(Intent.ACTION_DIAL);
                                            intent.setData(Uri.parse(new StringBuilder("tel :")
                                            .append(orderModel.getUserPhone()).toString()));
                                            startActivity(intent);

                                        }
                                        @Override
                                        public void onPermissionDenied(PermissionDeniedResponse response) {
                                            Toast.makeText(getContext(),"You must accept "+response.getPermissionName(),Toast.LENGTH_SHORT).show();

                                        }

                                        @Override
                                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                                        }
                                    }).check();
                        })
                );
                buf.add(new MyButton(getContext(), "Remove", 30, 0, Color.parseColor("#12005E"),
                        position -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                                    .setTitle("Delete")
                                    .setMessage("Do you really want to delete this order?")
                                    .setNegativeButton("CANCLE", (dialogInterface, which) -> {
                                        dialogInterface.dismiss();
                                    })
                                    .setPositiveButton("DELETE", (dialogInterface, which) -> {
                                        OrderModel orderModel = adapter.getItemAtPosition(position);
                                        FirebaseDatabase.getInstance()
                                                .getReference(CommonAgr.ORDER_REF)
                                                .child(orderModel.getKey())
                                                .removeValue()
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnSuccessListener(task -> {
                                                    adapter.removeItem(position);
                                                    adapter.notifyItemRemoved(position);
                                                    tv_order_filter.setText(new StringBuilder("Orders (")
                                                            .append(adapter.getItemCount())
                                                            .append(")"));
                                                    dialogInterface.dismiss();
                                                    Toast.makeText(getContext(), "Order has been delete!", Toast.LENGTH_SHORT).show();
                                                });
                                    });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                            Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                            negativeButton.setTextColor(Color.GRAY);
                            Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                            positiveButton.setTextColor(Color.RED);
                        }
                ));
                buf.add(new MyButton(getContext(), "Edit", 30, 0, Color.parseColor("#336699"),
                        position -> {

                        })
                );
            }
        };

        setHasOptionsMenu(true);
        recycler_order.setHasFixedSize(true);
        recycler_order.setLayoutManager(new LinearLayoutManager(getContext()));

        layoutAnimationControllerl = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.order_filter_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter: {
                BottomSheetOrderFragment bottomSheetOrderFragment = BottomSheetOrderFragment.getInstance();
                bottomSheetOrderFragment.show(getActivity().getSupportFragmentManager(), "OrderFilter");
                break;
            }
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(LoadOrderEvent.class)) {
            EventBus.getDefault().removeStickyEvent(LoadOrderEvent.class);
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLoadOrderEvent(LoadOrderEvent event) {
        orderViewModel.loadOrderByStatus(event.getStatus());
    }
}