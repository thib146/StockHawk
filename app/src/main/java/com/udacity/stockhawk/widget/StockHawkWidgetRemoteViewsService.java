package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by thib146 on 13/03/2017.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StockHawkWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = StockHawkWidgetRemoteViewsService.class.getSimpleName();

    private DecimalFormat dollarFormat;
    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat percentageFormat;

    private static final String[] STOCK_COLUMNS = {
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_COMPANY,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE
    };
    // these indices must match the projection
    private static final int INDEX_SYMBOL = 0;
    private static final int INDEX_COMPANY = 1;
    private static final int INDEX_PRICE = 2;
    private static final int INDEX_PERCENTAGE_CHANGE = 3;
    private static final int INDEX_ABSOLUTE_CHANGE = 4;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(Contract.Quote.URI, STOCK_COLUMNS, null,
                        null, Contract.Quote.COLUMN_SYMBOL + " ASC");

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+$");
                percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix("+");

                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);

                // Extract the weather data from the Cursor
                String symbol = data.getString(INDEX_SYMBOL);
                String company = data.getString(INDEX_COMPANY);
                String price = data.getString(INDEX_PRICE);

                float rawAbsoluteChange = data.getFloat(INDEX_ABSOLUTE_CHANGE);
                float percentageChange = data.getFloat(INDEX_PERCENTAGE_CHANGE);

                // Add the data to the RemoteViews
                views.setTextViewText(R.id.widget_item_symbol, symbol);
                views.setTextViewText(R.id.widget_item_stock_company_name, company);
                views.setTextViewText(R.id.widget_item_price, price);

                if (rawAbsoluteChange > 0) {
                    views.setInt(R.id.widget_item_change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.widget_item_change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }

                String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                String percentage = percentageFormat.format(percentageChange / 100);

                views.setTextViewText(R.id.widget_item_change, change);

                final Intent fillInIntent = new Intent();

                fillInIntent.setData(Contract.Quote.URI);
                views.setOnClickFillInIntent(R.id.widget_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_SYMBOL);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
