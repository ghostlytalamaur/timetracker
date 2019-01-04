package mvasoft.timetracker.ui.extlist;

import androidx.annotation.Nullable;

import java.util.List;

import mvasoft.recyclerbinding.viewmodel.ItemViewModel;
import mvasoft.timetracker.utils.DateTimeFormatters;

public abstract class BaseItemViewModel extends ItemViewModel {

    abstract boolean getIsRunning();
    abstract void appendSessionIds(List<Long> destList);
    abstract void updateDuration();
    abstract String getClipboardString(@Nullable DateTimeFormatters formatter);
}
