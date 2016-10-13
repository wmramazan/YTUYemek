package com.adnagu.ytuyemek;

import android.util.Log;

public class Meal {

    int id;
	String lunch, dinner;
	
    public Meal(int id, String lunch, String dinner) {
        this.id = id;
        this.lunch = lunch;
        this.dinner = dinner;
    }

    public int getId() {
        return id;
    }

    public String getLunch() {
        return lunch;
    }

    public String getDinner() {
        return dinner;
    }
}