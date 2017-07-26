package com.sgo.saldomu.models;

import java.util.ArrayList;

/**
 * Created by Lenovo on 14/03/2017.
 */

public class SetupOpenHour {

    private ArrayList<OpenHourDays> setupOpenHours;
    private ArrayList<String> weekDays = new ArrayList<>();
    private ArrayList<Integer> days = new ArrayList<>();
    private ArrayList<Integer> selectedWeekDays = new ArrayList<>();
    private ArrayList<Integer> selectedDays = new ArrayList<>();

    public SetupOpenHour() {
        setupOpenHours = new ArrayList<OpenHourDays>();
        setupOpenHours.add(new OpenHourDays("DEF", "Default", "", "", 0));
        setupOpenHours.add(new OpenHourDays("SUN", "Minggu", "", "", 1));
        setupOpenHours.add(new OpenHourDays("MON", "Senin", "", "", 1));
        setupOpenHours.add(new OpenHourDays("TUE", "Selasa", "", "",1));
        setupOpenHours.add(new OpenHourDays("WED", "Rabu", "", "", 1));
        setupOpenHours.add(new OpenHourDays("THU", "Kamis", "", "", 1));
        setupOpenHours.add(new OpenHourDays("FRI", "Jumat", "", "", 1));
        setupOpenHours.add(new OpenHourDays("SAT", "Sabtu", "", "", 1));

        for( int j = 0; j < setupOpenHours.size(); j++ ){
            if ( setupOpenHours.get(j).getIsDay() == 1 ) {
                weekDays.add(setupOpenHours.get(j).getNamaHari());
            }
        }

        for(int j = 0; j < 31; j++ ) {
            days.add(j);
        }
    }

    public void updateOpenHourDaysByKode(int position, String startHour, String endHour, int iStartHour, int iStartMinute, int iEndHour, int iEndMinute) {
        int isDay = 1;
        int isUpdateAll = 0;
        if ( setupOpenHours.get(position).getIsDay() == isDay )
        {
            isUpdateAll = 0;
            setupOpenHours.get(position).setEndHour(endHour);
            setupOpenHours.get(position).setStartHour(startHour);
            setupOpenHours.get(position).setiStartHour(iStartHour);
            setupOpenHours.get(position).setiStartMinute(iStartMinute);
            setupOpenHours.get(position).setiEndHour(iEndHour);
            setupOpenHours.get(position).setiEndMinute(iEndMinute);
        }
        else
        {
            isUpdateAll = 1;
        }

        if ( isUpdateAll == 1)
        {
            for(int i = 0; i < setupOpenHours.size(); i++ ) {
                setupOpenHours.get(i).setEndHour(endHour);
                setupOpenHours.get(i).setStartHour(startHour);
                setupOpenHours.get(i).setiStartHour(iStartHour);
                setupOpenHours.get(i).setiStartMinute(iStartMinute);
                setupOpenHours.get(i).setiEndHour(iEndHour);
                setupOpenHours.get(i).setiEndMinute(iEndMinute);
            }
        }

    }

    public ArrayList<OpenHourDays> getSetupOpenHours() {
        return this.setupOpenHours;
    }

    public ArrayList<Integer> getSelectedWeekDays() {
        return this.selectedWeekDays;
    }

    public ArrayList<Integer> getSelectedDays() {
        return this.selectedDays;
    }

    public void setSelectedWeekDays(ArrayList<Integer> selectedWeekDays ) {
        this.selectedWeekDays = selectedWeekDays;
    }

    public void setSelectedDays(ArrayList<Integer> selectedDays ) {
        this.selectedDays = selectedDays;
    }
    public void setSetupOpenHours(ArrayList<OpenHourDays> setupOpenHours) {
        this.setupOpenHours = setupOpenHours;
    }

    public ArrayList<String> getWeekDays() {
        return weekDays;
    }

    public void setWeekDays(ArrayList<String> weekDays) {
        this.weekDays = weekDays;
    }

    public ArrayList<Integer> getDays() {
        return days;
    }

    public void setDays(ArrayList<Integer> days) {
        this.days = days;
    }
}
