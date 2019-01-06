package mvasoft.timetracker.ui.editdate;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Set;

import javax.inject.Inject;

import androidx.navigation.fragment.NavHostFragment;
import mvasoft.timetracker.R;
import mvasoft.timetracker.core.Injectable;
import mvasoft.timetracker.databinding.FragmentDatesViewBinding;
import mvasoft.timetracker.ui.common.BindingSupportFragment;
import mvasoft.timetracker.utils.DateTimeHelper;

public class DatesViewFragment extends
        BindingSupportFragment<FragmentDatesViewBinding, DatesViewModel>
        implements Injectable {

    @Inject
    ViewModelProvider.Factory mFactory;
    private DayDecorator mDayDecorator;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  super.onCreateView(inflater, container, savedInstanceState);
        getBinding().calendarView.setOnDateChangedListener((materialCalendarView, calendarDay, b) -> {
            long unixTime = DateTimeHelper.getUnixTime(calendarDay.getYear(), calendarDay.getMonth(), calendarDay.getDay());
            showDate(unixTime);
        });

        getBinding().calendarView.setOnMonthChangedListener((materialCalendarView, calendarDay) ->
                getViewModel().updateWorkingDays(calendarDay));

        int color = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null);
        mDayDecorator = new DayDecorator(null, color);
        getViewModel().getWorkingDays().observe(this, days -> {
            mDayDecorator.mWorkingDays = days;
            getBinding().calendarView.invalidateDecorators();
        });

        getBinding().calendarView.addDecorator(mDayDecorator);

        CalendarDay today = CalendarDay.today();
        getBinding().calendarView.setSelectedDate(today);
        getViewModel().updateWorkingDays(today);
        return v;
    }

    private void showDate(long unixTime) {
        NavHostFragment.findNavController(this).navigate(
                DatesViewFragmentDirections.actionEditDate().setDayUnixTime(unixTime));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_dates_view;
    }

    @Override
    protected DatesViewModel onCreateViewModel() {
        return ViewModelProviders.of(this, mFactory).get(DatesViewModel.class);

    }

    static class DayDecorator implements DayViewDecorator {

        private final int mColor;
        private Set<CalendarDay> mWorkingDays;

        DayDecorator(Set<CalendarDay> workingDays, int color) {
            mWorkingDays = workingDays;
            mColor = color;
        }

        @Override
        public boolean shouldDecorate(CalendarDay calendarDay) {
            return (mWorkingDays != null) && mWorkingDays.contains(calendarDay);
        }

        @Override
        public void decorate(DayViewFacade dayViewFacade) {
            dayViewFacade.addSpan(new DotSpan(15f, mColor));
        }
    }
}
