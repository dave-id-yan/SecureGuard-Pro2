package com.dave.secureguard.secureguardpro2;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MainViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> _securityScore = new MutableLiveData<>(-1);
    public LiveData<Integer> securityScore = _securityScore;

    public MainViewModel(@NonNull Application application) {
        super(application);
        // Загружаем оценку сразу при создании ViewModel
        loadSavedScore();
    }

    public void loadSavedScore() {
        SharedPreferences prefs = getApplication().getSharedPreferences("security_prefs", Context.MODE_PRIVATE);
        int score = prefs.getInt("last_score", -1);
        _securityScore.setValue(score);
    }

    public void onSecurityScoreUpdated(int score) {
        _securityScore.setValue(score);
    }
}
