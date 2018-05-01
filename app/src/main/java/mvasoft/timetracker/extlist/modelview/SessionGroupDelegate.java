package mvasoft.timetracker.extlist.modelview;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.drextended.rvdatabinding.adapter.BindingHolder;
import com.drextended.rvdatabinding.delegate.BaseListBindingAdapterDelegate;

import java.util.List;

import mvasoft.timetracker.R;
import mvasoft.timetracker.databinding.ExSessionListItemBinding;
import mvasoft.timetracker.extlist.model.BaseItemModel;

public class SessionGroupDelegate extends BaseListBindingAdapterDelegate<BaseItemModel,
        ExSessionListItemBinding> {

    @Override
    public boolean isForViewType(@NonNull List<BaseItemModel> items, int position) {
        return items.get(position) instanceof SessionGroupViewModel;
    }

    @NonNull
    @Override
    public BindingHolder<ExSessionListItemBinding> onCreateViewHolder(ViewGroup parent) {
        return BindingHolder.newInstance(R.layout.ex_session_list_item,
                LayoutInflater.from(parent.getContext()), parent,false);
    }

    @Override
    public void onBindViewHolder(@NonNull List<BaseItemModel> items, int position,
                                 @NonNull BindingHolder<ExSessionListItemBinding> holder) {
        final SessionGroupViewModel group = (SessionGroupViewModel) items.get(position);
        holder.getBinding().setSessionGroup(group);
    }

    public long getItemId(final List<BaseItemModel> items, final int position) {
        return items.get(position).getId();
    }
}
