package mvasoft.timetracker.extlist.model;

import mvasoft.timetracker.core.IdProvider;

public interface BaseItemModel extends IdProvider {
    boolean getIsSelected();
    void setIsSelected(boolean isChecked);
    void onCleared();
}
