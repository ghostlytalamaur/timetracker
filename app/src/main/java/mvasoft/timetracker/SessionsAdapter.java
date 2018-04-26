package mvasoft.timetracker;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.lucasr.twowayview.ItemSelectionSupport;


class SessionsAdapter extends RecyclerView.Adapter<SessionsAdapter.ViewHolder> {

    private final PeriodFormatter mPeriodFormatter;
    private final DateTimeFormatter mDateFormatter;
    private final DateTimeFormatter mTimeFormatter;

    private final GroupsList.IGroupsChangesListener mGroupsListener = new GroupsList.IGroupsChangesListener() {
        @Override
        public void onDataChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRemoved(int index) {
            notifyItemRemoved(index);
        }

        @Override
        public void onItemChanged(int index) {
            notifyItemChanged(index);
        }

        @Override
        public void onItemMoved(int oldIndex, int newIndex) {
            notifyItemMoved(oldIndex, newIndex);
        }

        @Override
        public void onItemInserted(int index) {
            notifyItemInserted(index);
        }
    };
    private GroupsList mGroups;
    private ItemSelectionSupport mItemSelection;


    SessionsAdapter() {
        super();
        mPeriodFormatter = new PeriodFormatterBuilder()
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendHours()
                .appendSeparator(":")
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendMinutes()
                .appendSeparator(":")
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendSeconds()
                .toFormatter();

        mDateFormatter = new DateTimeFormatterBuilder().
//                appendDayOfWeekShortText().
                appendDayOfWeekText().
                appendLiteral(", ").
                appendDayOfMonth(2).
                appendLiteral(" ").
//                        appendMonthOfYearShortText().
                appendMonthOfYearText().
                appendLiteral(" ").
                appendYear(4, 4).
                toFormatter();

        mTimeFormatter = new DateTimeFormatterBuilder().
                        appendHourOfDay(2).
                        appendLiteral(":").
                        appendMinuteOfHour(2).
                        toFormatter();

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
                    DateTime dt = new DateTime(mGroup.getStart() * 1000L);
                    startDateText = mDateFormatter.print(dt);
                    startTimeText = mTimeFormatter.print(dt);
                }
                if (!mGroup.isRunning()) {
                    DateTime dt = new DateTime(mGroup.getEnd() * 1000L);
                    endDateText = mDateFormatter.print(dt);
                    endTimeText = mTimeFormatter.print(dt);
                }
                durationText = mPeriodFormatter.print(
                        new Period(mGroup.getDuration() * 1000L));
            }

            mStartView.setText(startDateText);
            mEndView.setText(endDateText);
            mElapsedView.setText(durationText);
            mStartTimeView.setText(startTimeText);
            mEndTimeView.setText(endTimeText);
        }

    }

}
