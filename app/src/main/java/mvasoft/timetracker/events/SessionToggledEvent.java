package mvasoft.timetracker.events;

import mvasoft.timetracker.data.DataRepository;

public class SessionToggledEvent {
    public final DataRepository.ToggleSessionResult toggleResult;


    public SessionToggledEvent(DataRepository.ToggleSessionResult toggleResult) {
        this.toggleResult = toggleResult;
    }
}
