package com.vuducminh.nicefoodserver.ui.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vuducminh.nicefoodserver.Adapter.MyOrderAdapter;
import com.vuducminh.nicefoodserver.Common.BottomSheetOrderFragment;
import com.vuducminh.nicefoodserver.EventBus.ChangeMenuClick;
import com.vuducminh.nicefoodserver.EventBus.LoadOrderEvent;
import com.vuducminh.nicefoodserver.Model.OrderModel;
import com.vuducminh.nicefoodserver.R;

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
        unbinder = ButterKnife.bind(this,root);
        initViews();
        orderViewModel.getMessageError().observe(this, s -> {
            Toast.makeText(getContext(),s,Toast.LENGTH_SHORT).show();
        });
        orderViewModel.getMutableLiveDataOrderModel().observe(this, orderModels -> {
            if(orderModels != null) {
                adapter = new MyOrderAdapter(getContext(),orderModels);
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

        setHasOptionsMenu(true);
        recycler_order.setHasFixedSize(true);
        recycler_order.setLayoutManager(new LinearLayoutManager(getContext()));

        layoutAnimationControllerl = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.order_filter_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter: {
                BottomSheetOrderFragment bottomSheetOrderFragment = BottomSheetOrderFragment.getInstance();
                bottomSheetOrderFragment.show(getActivity().getSupportFragmentManager(),"OrderFilter");
                break;
            }
        }
        return true;
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
        if(EventBus.getDefault().hasSubscriberForEvent(LoadOrderEvent.class)) {
            EventBus.getDefault().removeStickyEvent(LoadOrderEvent.class);
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
    public void onLoadOrderEvent(LoadOrderEvent event) {
        orderViewModel.loadOrderByStatus(event.getStatus());
    }
}