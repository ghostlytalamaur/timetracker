package mvasoft.timetracker.data;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import mvasoft.timetracker.data.room.RoomDataRepositoryImpl;

@Module
public abstract class RepositoryModule {

    // Результат - то, что провайдим.
    // параметр - конкретная реализация. В реализации используется инекция конструктора
    @Binds
    // Всегда провайдим один и тот же инстанс
    @Singleton
    abstract DataRepository bindDataRepository(RoomDataRepositoryImpl impl);
}
