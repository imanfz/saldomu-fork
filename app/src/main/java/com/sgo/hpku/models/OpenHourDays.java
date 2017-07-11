package com.sgo.hpku.models;

/**
 * Created by Lenovo on 14/03/2017.
 */

public class OpenHourDays {
    private String namaHari;
    private String kodeHari;
    private String startHour;
    private String endHour;
    private int iStartHour;
    private int iStartMinute;
    private int iEndHour;
    private int iEndMinute;
    private int isDay;

    public OpenHourDays( String kodeHari, String namaHari, String startHour, String endHour, int isDay) {
        this.namaHari   = namaHari;
        this.kodeHari   = kodeHari;
        this.startHour  = startHour;
        this.endHour    = endHour;
        this.isDay      = isDay;
    }

    public int getIsDay() {
        return this.isDay;
    }

    public void setIsDay(int isDay) {
        this.isDay = isDay;
    }

    public String getNamaHari() {
        return this.namaHari;
    }

    public void setNamaHari(String namaHari) {
        this.namaHari = namaHari;
    }

    public String getKodeHari() {
        return this.kodeHari;
    }

    public void setKodeHari(String kodeHari) {
        this.kodeHari = kodeHari;
    }

    public String getStartHour() {
        return this.startHour;
    }

    public void setStartHour(String startHour) {
        this.startHour = startHour;
    }

    public String getEndHour() {
        return endHour;
    }

    public void setEndHour(String endHour) {
        this.endHour = endHour;
    }

    public int getiStartHour() {
        return iStartHour;
    }

    public void setiStartHour(int iStartHour) {
        this.iStartHour = iStartHour;
    }

    public int getiStartMinute() {
        return this.iStartMinute;
    }

    public void setiStartMinute(int iStartMinute) {
        this.iStartMinute = iStartMinute;
    }

    public int getiEndHour() {
        return this.iEndHour;
    }

    public void setiEndHour(int iEndHour) {
        this.iEndHour = iEndHour;
    }

    public int getiEndMinute() {
        return iEndMinute;
    }

    public void setiEndMinute(int iEndMinute) {
        this.iEndMinute = iEndMinute;
    }
}
