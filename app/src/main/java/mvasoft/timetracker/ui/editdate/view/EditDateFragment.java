package mvasoft.timetracker.ui.editdate.view;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import javax.inject.Inject;

import mvasoft.timetracker.BR;
import mvasoft.timetracker.R;
import mvasoft.timetracker.databinding.FragmentEditDateBinding;
import mvasoft.timetracker.ui.common.BindingSupportFragment;
import mvasoft.timetracker.ui.editdate.modelview.EditDateViewModel;

public class EditDateFragment extends BindingSupportFragment<FragmentEditDateBinding, EditDateViewModel> {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit_date, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                getViewModel().save();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected EditDateViewModel onCreateViewModel() {
        return ViewModelProviders.of(this, viewModelFactory).get(EditDateViewModel.class);
    }

    protected @IdRes
    int getModelVariableId() {
        return BR.view_model;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_edit_date;
    }

    public void setDate(long date) {
        getViewModel().setDate(date);
    }
}
