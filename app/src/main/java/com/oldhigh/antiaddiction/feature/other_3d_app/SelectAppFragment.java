package com.oldhigh.antiaddiction.feature.other_3d_app;

import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.oldhigh.antiaddiction.R;
import com.oldhigh.antiaddiction.util.LogControl;

import java.util.List;

public class SelectAppFragment extends Fragment {

    private PackageManager mPackageManager;
    private OnAppItemClickListener mAppItemClickListener;

    public interface OnAppItemClickListener {
        void onAppItemClick(PackageInfo packageInfo);
    }

    public void setOnAppItemClickListener(OnAppItemClickListener listener) {
        mAppItemClickListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_select_app, container, false);
        // 防止穿透点击activity的按钮
        view.setClickable(true);
        view.setFocusable(true);
        final RecyclerView recyclerView = view.findViewById(R.id.rv_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        mPackageManager = getActivity().getPackageManager();

        LogControl.d();
        final List<PackageInfo> mrApps = MRAppHelper.getMRApps(getContext());

        recyclerView.setAdapter(new AppsAdapter(mrApps));

        return view;
    }


    public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.ViewHolder> {

        private final List<PackageInfo> mMrApps;

        public AppsAdapter(List<PackageInfo> mrApps) {
            mMrApps = mrApps;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //加载布局
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_info, parent, false);
            //将视图放入viewHolder
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final PackageInfo packageInfo = mMrApps.get(position);
            holder.ivIcon.setImageDrawable(packageInfo.applicationInfo.loadIcon(mPackageManager));
            holder.tvName.setText(packageInfo.applicationInfo.loadLabel(mPackageManager));
            holder.tvPkgName.setText(packageInfo.packageName);
            holder.itemView.setOnClickListener(v -> {
                if (mAppItemClickListener != null) {
                    mAppItemClickListener.onAppItemClick(packageInfo);
                    getActivity().getFragmentManager().beginTransaction()
                            .remove(SelectAppFragment.this).commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mMrApps.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView tvName;
            TextView tvPkgName;
            ImageView ivIcon;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_name);
                tvPkgName = itemView.findViewById(R.id.tv_pkg_name);
                ivIcon = itemView.findViewById(R.id.iv_icon);

            }
        }
    }
}
