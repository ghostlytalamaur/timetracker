package mvasoft.timetracker;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.lucasr.twowayview.ItemSelectionSupport;

import mvasoft.timetracker.ui.DateTimeFormatters;


class SessionsAdapter extends RecyclerView.Adapter<SessionsAdapter.ViewHolder> {

    private final DateTimeFormatters mFormatter;

    private final GroupsList.IGroupsChangesListener mGroupsListener = new GroupsList.IGroupsChangesListener() {
        @Override
        public void onDataChanged() {
            notifyDataSetChanged();
        }

    };

    private GroupsList mGroups;
    private ItemSelectionSupport mItemSelection;

    SessionsAdapter() {
        super();

        mFormatter = new DateTimeFormatters();
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.session_item_ex, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mGroups != null)
            holder.setSession(mGroups.get(position));
        holder.updateView(position);
    }

    @Override
    public long getItemId(int position) {
        GroupsList.SessionGroup group = null;
        if (mGroups != null)
            group = mGroups.get(position);

        if (group != null)
            return group.getID();
        else
            return -1;
    }

    @Override
    public int getItemCount() {
        return mGroups.count();
    }

    void setList(GroupsList groups) {
        if (mGroups != null)
            mGroups.removeChangesListener(mGroupsListener);
        mGroups = groups;
        if (mGroups != null)
            mGroups.addChangesListener(mGroupsListener);
    }

    void updateNotClosedView() {
        if (mGroups == null)
            return;
        for (int pos = 0; pos < mGroups.count(); pos++) {
            GroupsList.SessionGroup group = mGroups.get(pos);
            if ((group != null) && group.isRunning())
                notifyItemChanged(pos);
        }

    }

    void setItemSelection(ItemSelectionSupport itemSelection) {
        mItemSelection = itemSelection;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final View mLayout;
        private final TextView mStartView;
        private final TextView mEndView;
        private final TextView mStartTimeView;
        private final TextView mEndTimeView;
        private final TextView mElapsedView;
        private GroupsList.SessionGroup mGroup;

        ViewHolder(View itemView) {
            super(itemView);

            mLayout = itemView.findViewById(R.id.item_ex_layout);
            mStartView = itemView.findViewById(R.id.tvStart);
            mEndView = itemView.findViewById(R.id.tvEnd);
            mStartTimeView = itemView.findViewById(R.id.tvStartTime);
            mEndTimeView = itemView.findViewById(R.id.tvEndTime);
            mElapsedView = itemView.findViewById(R.id.tvElapsed);
        }

        void setSession(GroupsList.SessionGroup s) {
            mGroup = s;
        }

        void updateView(int pos) {
            boolean isChecked = (mItemSelection != null) && (mGroup != null) &&
                    (mItemSelection.isItemChecked(pos));
            mLayout.setActivated(isChecked);

            String startDateText = "";
            String endDateText = "";
            String startTimeText = "";
            String endTimeText = "";
            String durationText = "";
            
            if (mGroup != null) {
                if (mGroup.getStart() > 0) {
                    startDateText = mFormatter.formatDate(mGroup.getStart());
                    startTimeText = mFormatter.formatTime(mGroup.getStart());
                }
                if (!mGroup.isRunning()) {
                    endDateText = mFormatter.formatDate(mGroup.getEnd());
                    endTimeText = mFormatter.formatTime(mGroup.getEnd());
                }
                durationText = mFormatter.formatPeriod(mGroup.getDuration());
            }

            mStartView.setText(startDateText);
            mEndView.setText(endDateText);
            mElapsedView.setText(durationText);
            mStartTimeView.setText(startTimeText);
            mEndTimeView.setText(endTimeText);
        }

    }

}
