package com.dave.secureguard.secureguardpro2;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<Integer> _securityScore = new MutableLiveData<>(-1);
    public LiveData<Integer> securityScore = _securityScore;

    public void onSecurityScoreUpdated(int score) {
        _securityScore.setValue(score);
    }
}
