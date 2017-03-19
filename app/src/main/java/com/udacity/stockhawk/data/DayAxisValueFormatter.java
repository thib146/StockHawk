package com.udacity.stockhawk.data;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.Calendar;

/**
 * Created by thib146 on 19/03/2017.
 */

public class DayAxisValueFormatter implements IAxisValueFormatter {
    protected String[] mMonths = new String[]{
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private BarLineChartBase<?> chart;

    public DayAxisValueFormatter(BarLineChartBase<?> chart) {
        this.chart = chart;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        long valueLg = (long) value;

        Calendar cl = Calendar.getInstance();
        cl.setTimeInMillis(valueLg);
        String dateStr = "" + cl.get(Calendar.DAY_OF_MONTH) + "/" + cl.get(Calendar.MONTH) + "/" + cl.get(Calendar.YEAR);
        String timeStr = "" + cl.get(Calendar.HOUR_OF_DAY) + ":" + cl.get(Calendar.MINUTE) + ":" + cl.get(Calendar.SECOND);

        String monthName = mMonths[cl.get(Calendar.MONTH) % mMonths.length];
        String yearName = "" + cl.get(Calendar.YEAR);

        float range = chart.getVisibleXRange();

        if (chart.getVisibleXRange() > (long) 1500000000) {

            return monthName + " " + yearName;
        } else {

            int dayOfMonth = cl.get(Calendar.DAY_OF_MONTH);

            String appendix = "th";

            switch (dayOfMonth) {
                case 1:
                    appendix = "st";
                    break;
                case 2:
                    appendix = "nd";
                    break;
                case 3:
                    appendix = "rd";
                    break;
                case 21:
                    appendix = "st";
                    break;
                case 22:
                    appendix = "nd";
                    break;
                case 23:
                    appendix = "rd";
                    break;
                case 31:
                    appendix = "st";
                    break;
            }

            return dayOfMonth == 0 ? "" : dayOfMonth + appendix + " " + monthName;
        }
    }

}
