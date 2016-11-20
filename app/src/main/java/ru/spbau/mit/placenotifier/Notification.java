package ru.spbau.mit.placenotifier;

public class Notification {
    // TODO: 12.11.2016 think about structure
    boolean isActive;
    String name;
    String comment;
//    something like that, maybe
//    but i'm not sure, that use com.google.android.gms.maps.model.LatLng is good idea
//
//    and maybe we should use something like Data or Calendar and Location here
//    Predicate<LatLng> placePredicate;
//    Predicate<Long> timePredicate;

    public Notification(String name, String comment) {
        this.name = name;
        this.comment = comment;
    }
}
