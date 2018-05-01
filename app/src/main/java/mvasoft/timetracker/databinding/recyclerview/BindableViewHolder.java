package mvasoft.timetracker.databinding.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import mvasoft.timetracker.extlist.model.BaseItemModel;

public class BindableViewHolder<ViewModel extends BaseItemModel> extends RecyclerView.ViewHolder {
    public BindableViewHolder(View itemView) {
        super(itemView);
    }
}
