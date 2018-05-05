package mvasoft.timetracker.databinding.recyclerview;

public interface BaseItemModel extends IdProvider {
    boolean getIsSelected();
    void setIsSelected(boolean isChecked);
    void onCleared();

    void dataChanged();
}
