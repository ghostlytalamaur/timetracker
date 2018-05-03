package mvasoft.timetracker.core;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import mvasoft.timetracker.extlist.modelview.ExSessionListViewModel;
import mvasoft.timetracker.extlist.modelview.TabbedActivityViewModel;
import mvasoft.timetracker.ui.SessionEditViewModel;

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
    @ViewModelKey(SessionEditViewModel.class)
    abstract ViewModel bindSessionEditViewModel(SessionEditViewModel sessionEditViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(DIViewModelFactory factory);
}
