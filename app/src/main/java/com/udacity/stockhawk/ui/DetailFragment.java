package com.udacity.stockhawk.ui;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.common.collect.Lists;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.DayAxisValueFormatter;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * Created by thib146 on 08/03/2017.
 */

public class DetailFragment extends Fragment implements StockAdapter.StockAdapterOnClickHandler {

    //@BindView(R.id.stock_title) TextView mStockTitle;
    //@BindView(R.id.stock_chart) LineChart mLineChart;

    private TextView mStockTitle;
    private LineChart mLineChart;

    private String mSymbol;

    private Cursor mCursor;

    private static int COLUMN_HISTORY_INDEX = 4;

    // Key used to get the quote ID through the fragment creation
    public static final String ARG_ITEM_ID = "item_id";

    // Columns of data we want to display in the Detailed view
    public static final String[] QUOTE_PROJECTION = {
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE,
            Contract.Quote.COLUMN_HISTORY
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_stock_detail, container, false);

        //ButterKnife.bind(getActivity());

        // Get the intent that started the activity
        Intent intentThatStartedThatActivity = getActivity().getIntent();
        Bundle bundle = getArguments();

        //if (bundle != null) {
        //    mSymbol = bundle.getString(DetailFragment.ARG_ITEM_ID);
        //} else {
            mSymbol = intentThatStartedThatActivity.getStringExtra(Intent.EXTRA_TEXT);
        //}

        mStockTitle = (TextView) view.findViewById(R.id.stock_title);
        mLineChart = (LineChart) view.findViewById(R.id.stock_chart);

        mStockTitle.setText("Stock: " + mSymbol);

        // Get the History Data from our Content Provider
        String[] mSelectionArgs = {""};
        mSelectionArgs[0] = mSymbol;
        if (mSymbol != null) {
            mCursor = getActivity().getContentResolver().query(
                    Contract.Quote.URI,
                    QUOTE_PROJECTION,
                    Contract.Quote.COLUMN_SYMBOL + " = ?",
                    mSelectionArgs,
                    null);
        }

        mCursor.moveToFirst();
        int index = mCursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY);
        String stringHistory = mCursor.getString(index);

        Pattern pattern = Pattern.compile("(\\d{1,14})\\, (\\d{1,5})\\.(\\d{1,7})\\n");

        Matcher matcher = pattern.matcher(stringHistory);
        List<Entry> entriesHist = new ArrayList<Entry>();
        while(matcher.find()){
            String strCoordX = matcher.group(1);
            String strCoordY = matcher.group(2) + "." + matcher.group(3);

            float CoordX = Float.parseFloat(strCoordX);
            float CoordY = Float.parseFloat(strCoordY);

            entriesHist.add(new Entry(CoordX, CoordY));
        }

        Calendar clCurrent = Calendar.getInstance();
        int currentDay = clCurrent.get(Calendar.DAY_OF_MONTH);
        int currentMonth = clCurrent.get(Calendar.MONTH);
        int currentYear = clCurrent.get(Calendar.YEAR);

        boolean isDayListCreated = false,
                isMonthListCreated = false,
                isYearListCreated = false;

        int color1 = getResources().getColor(R.color.colorAccent);

        List<Entry> entriesOneDay = new ArrayList<Entry>();

        for (int i=0; i<=entriesHist.size(); i++) {
            long date = (long) entriesHist.get(i).getX();
            Calendar clCurve = Calendar.getInstance();
            // TODO : out of bound index here...
            clCurve.setTimeInMillis(date);

            int curveDay = clCurve.get(Calendar.DAY_OF_MONTH);
            int curveMonth = clCurve.get(Calendar.MONTH);
            int curveYear = clCurve.get(Calendar.YEAR);

            if (curveDay == currentDay + 1 && curveMonth == currentMonth && !isDayListCreated) {
                entriesOneDay = entriesHist.subList(0, i);
                isDayListCreated = true;
            }
        }

        List<Entry> entriesHistReverse = Lists.reverse(entriesHist);
        List<Entry> entriesOneDayReverse = Lists.reverse(entriesOneDay);

        LineDataSet dataSet = new LineDataSet(entriesOneDayReverse, "Price"); // add entries to dataset
        dataSet.setColor(Color.GRAY);
        dataSet.setFillColor(Color.GREEN);
        dataSet.setFillAlpha(100);
        dataSet.setDrawFilled(true);
        dataSet.setDrawCircles(false);

        LineData lineData = new LineData(dataSet);

        IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(mLineChart);

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        xAxis.setValueFormatter(xAxisFormatter);

        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setEnabled(false);

        YAxis rightAxis = mLineChart.getAxisRight();
        rightAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        rightAxis.setTextSize(10f);
        rightAxis.setTextColor(Color.WHITE);
        rightAxis.setDrawAxisLine(true);
        rightAxis.setDrawGridLines(true);

        mLineChart.setData(lineData);
        mLineChart.setTouchEnabled(false);
        mLineChart.setDescription(null);
        mLineChart.setDrawBorders(true);
        mLineChart.setBorderColor(Color.WHITE);
        mLineChart.setBorderWidth(1);
        mLineChart.setMaxVisibleValueCount(3);
        mLineChart.invalidate(); // refresh

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(String itemId) {

    }
}