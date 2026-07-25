package gui.controllers.events;

import javafx.event.Event;
import javafx.event.EventType;

public class UpdateEvent extends Event {
    public static final EventType<UpdateEvent> UPDATE = new EventType<>(Event.ANY, "UPDATE_EVENT");

    public UpdateEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

    public UpdateEvent() {
        super(UPDATE);
    }
}
