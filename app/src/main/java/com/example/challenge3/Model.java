package com.example.challenge3;

public class Model {
    
    private int imageview1;
    private String ActivityType, CurrentTime, divider;

    Model(int imageview1, String ActivityType, String CurrentTime, String divider){
        this.imageview1= imageview1;
        this.ActivityType= ActivityType;
        this.CurrentTime= CurrentTime;
        this.divider= divider;
    }


    public int getImageview1() {
        return imageview1;
    }

    public String getActivityType() {
        return ActivityType;
    }

    public String getCurrentTime() {
        return CurrentTime;
    }

    public String getDivider() {
        return divider;
    }
}
