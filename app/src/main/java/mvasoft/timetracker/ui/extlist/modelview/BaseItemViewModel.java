package mvasoft.timetracker.ui.extlist.modelview;

import java.util.List;

import mvasoft.recyclerbinding.viewmodel.ItemViewModel;

public abstract class BaseItemViewModel extends ItemViewModel {

    abstract boolean getIsRunning();
    abstract void appendSessionIds(List<Long> destList);
    abstract void dataChanged();
    abstract void updateDuration();
    abstract String asString();
}
