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

import java.text.SimpleDateFormat;

class SessionsAdapter extends RecyclerView.Adapter<SessionsAdapter.ViewHolder> {

    private PeriodFormatter mPeriodFormatter;
    private DateTimeFormatter mDateTimeFormatter;
    private SimpleDateFormat mDateFormat;
    private GroupsList mGroups;


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
                R.layout.session_item, parent, false);
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
        mGroups = groups;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private GroupsList.SessionGroup mGroup;
        private TextView mStartView;
        private TextView mEndView;
        private TextView mElapsedView;

        public ViewHolder(View itemView) {
            super(itemView);

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

    }

}
