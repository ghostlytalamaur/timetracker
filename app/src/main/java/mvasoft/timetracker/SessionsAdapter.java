package mvasoft.timetracker;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.SimpleDateFormat;

class SessionsAdapter extends RecyclerView.Adapter<SessionsAdapter.ViewHolder> {

    private PeriodFormatter mPeriodFormatter;
    private DateTimeFormatter mDateTimeFormatter;
    private SimpleDateFormat mDateFormat;
    private GroupsList mGroups;
    private GroupsList.IGroupsChangesListener mGruopsListener = new GroupsList.IGroupsChangesListener() {
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
    };


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
                toFormatter();

        setHasStableIds(true);
    }

    public void updateNotClosedView() {
        for (int pos = 0; pos < mGroups.count(); pos++) {
            if (mGroups.get(pos).isRunning())
                notifyItemChanged(pos);
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.session_item_ex, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setSession(mGroups.get(position));
        holder.updateView();
    }

    @Override
    public int getItemCount() {
        return mGroups.count();
    }

    public void setList(GroupsList groups) {
        if (mGroups != null)
            mGroups.setChangesListener(null);
        mGroups = groups;
        if (mGroups != null)
            mGroups.setChangesListener(mGruopsListener);
    }

    @Override
    public long getItemId(int position) {
        return mGroups.get(position).getID();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements Checkable {

        private GroupsList.SessionGroup mGroup;
        private View mLayout;
        private TextView mStartView;
        private TextView mEndView;
        private TextView mElapsedView;
        private boolean mChecked;

        public ViewHolder(View itemView) {
            super(itemView);

            mLayout = itemView.findViewById(R.id.item_ex_layout);
            mStartView = itemView.findViewById(R.id.tvStart);
            mEndView = itemView.findViewById(R.id.tvEnd);
            mElapsedView = itemView.findViewById(R.id.tvElapsed);
        }

        public void setSession(GroupsList.SessionGroup s) {
            mGroup = s;
        }

        public void updateView() {
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

        @Override
        public void setChecked(boolean b) {
            mChecked = b;
            mLayout.setActivated(mChecked);
        }

        @Override
        public boolean isChecked() {
            return mChecked;
        }

        @Override
        public void toggle() {
            setChecked(!mChecked);
        }
    }

}
