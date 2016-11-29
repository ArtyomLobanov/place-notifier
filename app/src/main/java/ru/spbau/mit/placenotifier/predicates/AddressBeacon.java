package ru.spbau.mit.placenotifier.predicates;

import android.location.Address;
import android.support.annotation.NonNull;

import java.util.Locale;

public class AddressBeacon extends Beacon {

    private final Locale locale;
    private final double latitude;
    private final double longitude;
    private final String addressLine;

    public AddressBeacon(@NonNull Address address, @NonNull String addressLine) {
        locale = address.getLocale();
        latitude = address.getLatitude();
        longitude = address.getLongitude();
        this.addressLine = addressLine;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @NonNull
    public String getAddressLine() {
        return addressLine;
    }

    @NonNull
    public Address getAddress() {
        Address address = new Address(locale);
        address.setAddressLine(0, addressLine);
        address.setLatitude(latitude);
        address.setLongitude(longitude);
        return address;
    }
}
