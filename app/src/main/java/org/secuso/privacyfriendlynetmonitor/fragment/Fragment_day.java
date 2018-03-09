/*
    Privacy Friendly Net Monitor (Net Monitor)
    - Copyright (2015 - 2017) Felix Tsala Schiller

    ###################################################################

    This file is part of Net Monitor.

    Net Monitor is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Net Monitor is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Net Monitor.  If not, see <http://www.gnu.org/licenses/>.

    Diese Datei ist Teil von Net Monitor.

    Net Monitor ist Freie Software: Sie können es unter den Bedingungen
    der GNU General Public License, wie von der Free Software Foundation,
    Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
    veröffentlichten Version, weiterverbreiten und/oder modifizieren.

    Net Monitor wird in der Hoffnung, dass es nützlich sein wird, aber
    OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
    Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Siehe die GNU General Public License für weitere Details.

    Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
    Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.

    ###################################################################

    This app has been created in affiliation with SecUSo-Department of Technische Universität
    Darmstadt.

    The design is based on the Privacy Friendly Example App template by Karola Marky, Christopher
    Beckmann and Markus Hau (https://github.com/SecUSo/privacy-friendly-app-example).

    Privacy Friendly Net Monitor is based on TLSMetric by Felix Tsala Schiller
    https://bitbucket.org/schillef/tlsmetric/overview.

 */

package org.secuso.privacyfriendlynetmonitor.fragment;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.secuso.privacyfriendlynetmonitor.Activities.Adapter.FragmentDayListAdapter;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.DBApp;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.DaoSession;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.ReportEntity;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.ReportEntityDao;
import org.secuso.privacyfriendlynetmonitor.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by tobias on 04.01.18.
 * https://github.com/PhilJay/MPAndroidChart/wiki/Setting-Data
 * Fragment which represent one day.
 */

public class Fragment_day extends Fragment {

    // ReportEntity Table and ReportEntities List
    private static ReportEntityDao reportEntityDao;
    private static List<ReportEntity> reportEntities;
    private static List<ReportEntity> filtered_Entities = new ArrayList<>();
    private static List<String> entitiesString = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View view = inflater.inflate(R.layout.fragment_charts, container, false);
        ScrollView scrollview = view.findViewById(R.id.scrollViewChart);
        scrollview.setSmoothScrollingEnabled(true);

        //Fill Icon, AppGroupTitle, AppName
        PackageManager packageManager = getActivity().getPackageManager();

        TextView tx_appName = view.findViewById(R.id.historyGroupSubtitle);
        final String appName = getArguments().getString("AppName");
        tx_appName.setText(appName);

        try {
            ImageView appIcon = (ImageView) view.findViewById(R.id.historyGroupIcon);
            String appGroupTitle = (String) packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(appName, PackageManager.GET_META_DATA));
            TextView tx_appGroupTitle = view.findViewById(R.id.historyGroupTitle);
            tx_appGroupTitle.setText(appGroupTitle);
            appIcon.setImageDrawable(packageManager.getApplicationIcon(appName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //END Fill Icon, AppGroupTitle, AppName

        //Build the Barchart
        final BarChart chart = (BarChart) view.findViewById(R.id.chart);
        loadFilteredList(appName); //method to get all connection from the app "appName"
        fillChart(view, chart); //method to fill the chart with the filteredList
        fillRecyclerList(view, filtered_Entities); //method to show all connection

        //Listener for Value Selection
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                //Handling the current time in Hour
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                Date currentTime = null;
                try {
                    currentTime = dateFormat.parse(dateFormat.format(new Date()));
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
                //End current time handling
                int currentHour = currentTime.getHours();
                int shift = 23 - currentHour; //the shift that is needed to get the correct connections
                //extra cacheList to only show the reports to the selected value in the chart
                List<ReportEntity> cacheList = new ArrayList<ReportEntity>();
                if (e.getY() != 0) {
                    for (ReportEntity cacheEntity : filtered_Entities) {
                        int cacheEntityHour = (getEntityHour(cacheEntity) + shift) % 24;
                        if (cacheEntityHour == e.getX()) {
                            if (h.getStackIndex() == 0 && cacheEntity.getConnectionInfo().contains("Unknown")) {
                                cacheList.add(cacheEntity);
                            }
                            if (h.getStackIndex() == 1 && cacheEntity.getConnectionInfo().contains("Encrypted")) {
                                cacheList.add(cacheEntity);
                            }
                            if (h.getStackIndex() == 2 && cacheEntity.getConnectionInfo().contains("Unencrypted")) {
                                cacheList.add(cacheEntity);
                            }
                        }
                    }
                    fillRecyclerList(view, cacheList); //method to show conn. according to the value
                }
            }

            @Override
            public void onNothingSelected() {
                fillRecyclerList(view, filtered_Entities);
            }
        });

        return view;
    }

    private void fillChart(View view, BarChart chart) {

        //Handling the current time in Hour
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date currentTime = null;
        try {
            currentTime = dateFormat.parse(dateFormat.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int currentHour = currentTime.getHours();

        //Putting reportEntitites into a array for chart
        List<BarEntry> entry = new ArrayList<BarEntry>();

        int[] last24hours_encrypted = new int[24];
        int[] last24hours_unencrypted = new int[24];
        int[] last24hours_unknown = new int[24];

        for (ReportEntity reportEntity : filtered_Entities) {
            int hourEntity = getEntityHour(reportEntity);
            if (hourEntity == 24) {
                hourEntity = 0;
            }
            //Increase the field of the array of the entityHour
            if (reportEntity.getConnectionInfo().contains("Encrypted")) {
                last24hours_encrypted[hourEntity] = last24hours_encrypted[hourEntity] + 1;
            } else if (reportEntity.getConnectionInfo().contains("Unencrypted")) {
                last24hours_unencrypted[hourEntity] = last24hours_unencrypted[hourEntity] + 1;
            } else if (reportEntity.getConnectionInfo().contains("Unknown")) {
                last24hours_unknown[hourEntity] = last24hours_unknown[hourEntity] + 1;
            }

        }
        //adding data to chart
        int slide = 23 - currentHour;
        int[] cache24hours_encrypted = new int[24];
        int[] cache24hours_unencrypted = new int[24];
        int[] cache24hours_unknown = new int[24];
        for (int i = 0; i < last24hours_encrypted.length; i++) {
            int xValueCache = (i + slide) % 24;
            cache24hours_encrypted[xValueCache] = last24hours_encrypted[i];
            cache24hours_unencrypted[xValueCache] = last24hours_unencrypted[i];
            cache24hours_unknown[xValueCache] = last24hours_unknown[i];
        }
        //extra "for-loop" beause the chart has to be filled from "0" to...value
        for (int i = 0; i < cache24hours_encrypted.length; i++) {
            entry.add(new BarEntry(i, new float[]{cache24hours_unknown[i],
                    cache24hours_encrypted[i], cache24hours_unencrypted[i]}));
        }

        BarDataSet barset = new BarDataSet(entry, Fragment_day.this.getResources().getString(R.string.hours));
        barset.setStackLabels(new String[]{Fragment_day.this.getResources().getString(R.string.unknown), Fragment_day.this.getResources().getString(R.string.encrypted), Fragment_day.this.getResources().getString(R.string.unencrypted)});
        barset.setColors(new int[]{ContextCompat.getColor(getContext(), R.color.text_dark),
                ContextCompat.getColor(getContext(), R.color.green),
                ContextCompat.getColor(getContext(), R.color.red)});

        //X Achse Formatter--------------------------------------------------------

        // the labels that should be drawn on the XAxis
        final String[] hours = new String[last24hours_encrypted.length];

        for (int i = 0; i < hours.length; i++) {
            if (i == hours.length - 1) {
                hours[i] = currentHour + Fragment_day.this.getResources().getString(R.string.oclock);
            } else {
                hours[i] = "- " + Integer.toString(23 - i) + Fragment_day.this.getResources().getString(R.string.hr);
            }
        }

        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return hours[(int) value];
            }
        };

        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        //Y Achse Formatter----------------------------------------------------------
        YAxis yAxis_left = chart.getAxisLeft();
        yAxis_left.setAxisMinimum(0f);
        YAxis yAxis_right = chart.getAxisRight();
        yAxis_right.setAxisMinimum(0f);

        BarData barData = new BarData(barset);
        barData.setBarWidth(0.5f);
        chart.setData(barData);
        chart.setFitBars(true);
        //Sets the desc label at the bottom to " "
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);
        chart.invalidate();

    }

    private void loadFilteredList(String appName) {
        filtered_Entities.clear();
        entitiesString.clear();

        // load DB
        DaoSession daoSession = ((DBApp) getActivity().getApplication()).getDaoSession();
        reportEntityDao = daoSession.getReportEntityDao();
        reportEntities = reportEntityDao.loadAll(); //END load DB

        boolean isIncluded = false; //variable to check if conn. is already included

        for (ReportEntity reportEntity : reportEntities) {
            //Only entities from the AppName
            if (reportEntity.getAppName().equals(appName)) {
                String stringWithoutTimeStamp = reportEntity.toStringWithoutTimestamp();
                //search if it is included allready
                for (String s : entitiesString) {
                    if (s.equals(stringWithoutTimeStamp)) {
                        isIncluded = true;
                    }
                }
                //if it is NOT included do....
                if (isIncluded == false) {
                    //Only entities 24 hours ago
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    try {
                        Date currentTime = dateFormat.parse(dateFormat.format(new Date()));
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(currentTime);
                        cal.add(Calendar.DATE, -1);

                        //This is the date one day ago == 24 hours ago
                        Date dateBefore1Days = cal.getTime();

                        String string_date = reportEntity.getTimeStamp();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                        // sdf.parse(string_date) --> this is the Entity date
                        if (!sdf.parse(string_date).after(dateBefore1Days)) {

                        } else {
                            filtered_Entities.add(reportEntity); // add only that report from that app and 24hours ago
                            entitiesString.add(stringWithoutTimeStamp);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                isIncluded = false; //reset to false
            }
        }
    }

    private void fillRecyclerList(View view, List<ReportEntity> reportEntityList) {

        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        mRecyclerView.setFocusable(false);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FragmentDayListAdapter(reportEntityList, getContext());
        mRecyclerView.setAdapter(mAdapter);
    }

    private int getEntityHour(ReportEntity reportEntity) {
        String string_timestamp = reportEntity.getTimeStamp();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date entity_date = null;
        try {
            entity_date = sdf.parse(string_timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return entity_date.getHours();
    }
}
