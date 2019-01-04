package mvasoft.timetracker.core;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import mvasoft.timetracker.ui.editdate.DatesViewModel;
import mvasoft.timetracker.ui.editdate.EditDateViewModel;
import mvasoft.timetracker.ui.editsession.EditSessionViewModel;
import mvasoft.timetracker.ui.extlist.ExSessionListViewModel;

@SuppressWarnings("unused")
@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(ExSessionListViewModel.class)
    abstract ViewModel bindExSessionListViewModel(ExSessionListViewModel exSessionListViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(EditSessionViewModel.class)
    abstract ViewModel bindEditSessionViewModel(EditSessionViewModel editSessionViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(EditDateViewModel.class)
    abstract ViewModel bindEditDateFragmentViewModel(EditDateViewModel editDateViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DatesViewModel.class)
    abstract ViewModel bindDatesViewModel(DatesViewModel vm);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(DIViewModelFactory factory);
}
