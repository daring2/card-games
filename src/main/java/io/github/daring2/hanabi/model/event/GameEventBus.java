package io.github.daring2.hanabi.model.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GameEventBus {

    final List<Consumer<GameEvent>> listeners = new ArrayList<>();

    public Subscription subscribe(Consumer<GameEvent> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public void publish(GameEvent event) {
        listeners.forEach(it -> it.accept(event));
    }

    @FunctionalInterface
    public interface Subscription {
        void remove();
    }

}
