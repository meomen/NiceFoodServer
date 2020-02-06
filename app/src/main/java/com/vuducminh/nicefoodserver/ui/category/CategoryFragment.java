package com.vuducminh.nicefoodserver.ui.category;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vuducminh.nicefoodserver.Common.CommonAgr;
import com.vuducminh.nicefoodserver.Common.SpacesIiemDecoration;
import com.vuducminh.nicefoodserver.R;
import com.vuducminh.nicefoodserver.Adapter.MyCategoriesAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class CategoryFragment extends Fragment {

    private CategoryViewModel categoryViewModel;

    Unbinder unbinder;
    @BindView(R.id.recycler_menu)
    RecyclerView recycler_menu;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyCategoriesAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        categoryViewModel =
                ViewModelProviders.of(this).get(CategoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_category, container, false);

        unbinder = ButterKnife.bind(this,root);
        initView();
        categoryViewModel.getMessageError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(getContext(),""+s,Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        categoryViewModel.getCategoryList().observe(this,categoryModels -> {
            dialog.dismiss();
            adapter = new MyCategoriesAdapter(getContext(),categoryModels);
            recycler_menu.setAdapter(adapter);
            recycler_menu.setLayoutAnimation(layoutAnimationController);
        });

        return root;
    }

    private void initView() {
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        dialog.show();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),2);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(adapter != null) {
                    switch (adapter.getItemViewType(position)) {
                        case CommonAgr.DEFAULT_COLUMN_COUNT: return 1;
                        case CommonAgr.FULL_WIDTH_COLUMN: return 2;
                        default: return -1;
                    }
                }
                return -1;
            }
        });
        recycler_menu.setLayoutManager(layoutManager);
        recycler_menu.addItemDecoration(new SpacesIiemDecoration(8));
    }
}