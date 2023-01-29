package me.dinozoid.strife.alpine.bus;

import me.dinozoid.strife.alpine.listener.Listenable;
import me.dinozoid.strife.alpine.listener.Listener;

import java.util.Arrays;

/**
 * An Event Bus is used to manage the flow of events. Listenables and
 * individual/standalone Listeners may be subscribed/unsubscribed from
 * the event bus to listen to events.
 *
 * @author Brady
 * @since 5/27/2017
 */
public interface EventBus {

    /**
     * Discovers all of the valid Listeners defined in the
     * specified Listenable and subscribes them to the event bus.
     *
     * @param listenable The Listenable to be subscribed to the event bus
     * @see Listener
     */
    void subscribe(Listenable listenable);

    /**
     * Subscribes an individual listener object, as opposed to subscribing
     * all of the listener fields that are defined in a class.
     *
     * @param listener The individual listener to subscribe
     * @see Listener
     */
    void subscribe(Listener listener);

    /**
     * Subscribes all of the specified Listenables
     *
     * @param listenables An array of Listenable objects
     * @see Listener
     * @see #subscribe(Listenable)
     */
    default void subscribeAll(Listenable... listenables) {
        Arrays.stream(listenables).forEach(this::subscribe);
    }

    /**
     * Subscribes all of the specified Listenables
     *
     * @param listenables An iterable of Listenable objects
     * @see Listener
     * @see #subscribe(Listenable)
     */
    default void subscribeAll(Iterable<Listenable> listenables) {
        listenables.forEach(this::subscribe);
    }

    /**
     * Subscribes all of the specified Listeners
     *
     * @param listeners The array of listeners
     * @see Listener
     * @see #subscribe(Listener)
     */
    default void subscribeAll(Listener... listeners) {
        Arrays.stream(listeners).forEach(this::subscribe);
    }

    /**
     * Unsubscribes all of the Listeners that are defined by the Listenable
     *
     * @param listenable The Listenable to be unsubscribed from the event bus
     * @see #subscribe(Listenable)
     */
    void unsubscribe(Listenable listenable);

    /**
     * Unsubscribe an individual listener object.
     *
     * @param listener The individual listener to unsubscribe
     * @see Listener
     */
    void unsubscribe(Listener listener);

    /**
     * Unsubscribes all of the specified Listenables
     *
     * @param listenables The array of objects
     * @see Listener
     * @see #unsubscribe(Listenable)
     */
    default void unsubscribeAll(Listenable... listenables) {
        Arrays.stream(listenables).forEach(this::unsubscribe);
    }

    /**
     * Unsubscribes all of the specified Listenables
     *
     * @param listenables The list of objects
     * @see Listener
     * @see #unsubscribe(Listenable)
     */
    default void unsubscribeAll(Iterable<Listenable> listenables) {
        listenables.forEach(this::unsubscribe);
    }

    /**
     * Unsubscribes all of the specified Listeners
     *
     * @param listeners The array of listeners
     * @see Listener
     * @see #unsubscribe(Listener)
     */
    default void unsubscribeAll(Listener... listeners) {
        Arrays.stream(listeners).forEach(this::unsubscribe);
    }

    /**
     * Posts an event to all registered {@code Listeners}.
     *
     * @param event Event being called
     * @see Listener#invoke(Object)
     */
    void post(Object event);
}
