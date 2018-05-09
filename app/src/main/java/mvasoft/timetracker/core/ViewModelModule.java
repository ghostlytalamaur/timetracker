package mvasoft.timetracker.core;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import mvasoft.timetracker.ui.editdate.modelview.EditDateViewModel;
import mvasoft.timetracker.ui.editsession.viewmodel.EditSessionActivityViewModel;
import mvasoft.timetracker.ui.editsession.viewmodel.EditSessionFragmentViewModel;
import mvasoft.timetracker.ui.extlist.modelview.ExSessionListViewModel;
import mvasoft.timetracker.ui.extlist.modelview.TabbedActivityViewModel;

@SuppressWarnings("unused")
@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(TabbedActivityViewModel.class)
    abstract ViewModel bindTabbedActivityViewModel(TabbedActivityViewModel tabbedActivityViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ExSessionListViewModel.class)
    abstract ViewModel bindExSessionListViewModel(ExSessionListViewModel exSessionListViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(EditSessionFragmentViewModel.class)
    abstract ViewModel bindSessionEditViewModel(EditSessionFragmentViewModel editSessionFragmentViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(EditSessionActivityViewModel.class)
    abstract ViewModel bindEditSessionActivityViewModel(EditSessionActivityViewModel editSessionActivityViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(EditDateViewModel.class)
    abstract ViewModel bindEditDateFragmentViewModel(EditDateViewModel editDateViewModel);


    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(DIViewModelFactory factory);
}
