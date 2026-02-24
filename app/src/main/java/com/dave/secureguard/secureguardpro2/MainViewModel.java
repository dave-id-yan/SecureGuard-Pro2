package com.dave.secureguard.secureguardpro2;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<Boolean> _isVpnConnected = new MutableLiveData<>(false);
    public LiveData<Boolean> isVpnConnected = _isVpnConnected;

    private final MutableLiveData<String> _selectedCountry = new MutableLiveData<>("");
    public LiveData<String> selectedCountry = _selectedCountry;

    public void onVpnConnectionChanged(boolean isConnected) {
        _isVpnConnected.setValue(isConnected);
    }

    public void onCountrySelected(String country) {
        _selectedCountry.setValue(country);
    }
}