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
    private final DateTimeFormatter mDateTimeFormatter;
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

        mDateTimeFormatter = new DateTimeFormatterBuilder().
                appendDayOfWeekText().
                appendLiteral(", ").
                appendDayOfMonth(2).
                appendLiteral(" ").
                appendMonthOfYearText().
                appendLiteral(" ").
                appendYear(4, 4).
                appendLiteral("   ").
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
        if (mGroups != null)
            return mGroups.get(position).getID();
        else
            return -1;
    }

    @Override
    public int getItemCount() {
        return mGroups.count();
    }

    void setList(GroupsList groups) {
        if (mGroups != null)
            mGroups.setChangesListener(null);
        mGroups = groups;
        if (mGroups != null)
            mGroups.setChangesListener(mGroupsListener);
    }

    void updateNotClosedView() {
        if (mGroups == null)
            return;
        for (int pos = 0; pos < mGroups.count(); pos++) {
            if (mGroups.get(pos).isRunning())
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
        private final TextView mElapsedView;
        private GroupsList.SessionGroup mGroup;

        ViewHolder(View itemView) {
            super(itemView);

            mLayout = itemView.findViewById(R.id.item_ex_layout);
            mStartView = itemView.findViewById(R.id.tvStart);
            mEndView = itemView.findViewById(R.id.tvEnd);
            mElapsedView = itemView.findViewById(R.id.tvElapsed);
        }

        void setSession(GroupsList.SessionGroup s) {
            mGroup = s;
        }

        void updateView(int pos) {
            boolean isChecked = (mItemSelection != null) && (mGroup != null) &&
                    (mItemSelection.isItemChecked(pos));
            mLayout.setActivated(isChecked);

            if (mGroup == null) {
                mStartView.setText("");
                mEndView.setText("");
                mElapsedView.setText("");
            }
            else {
                mStartView.setText((mGroup.getStart() > 0) ?
                        mDateTimeFormatter.print(new DateTime(mGroup.getStart() * 1000L)) : "");
                mEndView.setText((!mGroup.isRunning()) ?
                        mDateTimeFormatter.print(new DateTime(mGroup.getEnd() * 1000L)) : "");
                mElapsedView.setText(
                        mPeriodFormatter.print(new Period(mGroup.getDuration() * 1000L)));
            }
        }

    }

}
