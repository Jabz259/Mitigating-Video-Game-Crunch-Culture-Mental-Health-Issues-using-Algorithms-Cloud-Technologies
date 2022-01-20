package com.example.finalyearprojectapp;

public class CalendarValidation {

    public static int getFlagger() {
        return flagger;
    }

    public static void setFlagger(int flagger) {
        CalendarValidation.flagger = flagger;
    }

    public static int flagger;


    public static boolean sameDateFlag;

    public static boolean isSameDateFlag() {
        return sameDateFlag;
    }

    public static void setSameDateFlag(boolean sameDateFlag) {
        CalendarValidation.sameDateFlag = sameDateFlag;
    }

    public static boolean similarFutureFlag;
    public static boolean similarPastFlag;

}
