package org.secuso.privacyfriendlynetmonitor.fragment;

import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
 */

public class Fragment_week extends Fragment {

    // ReportEntity Table and ReportEntities List
    private static ReportEntityDao reportEntityDao;
    private static List<ReportEntity> reportEntities;
    private static List<ReportEntity> filtered_Entities = new ArrayList<>();
    private static List<String> entitiesString = new ArrayList<>();
    private static Date dateBefore1week = null;
    private static Date currentDate = null;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View view = inflater.inflate(R.layout.fragment_charts, container, false);

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

        //calc dateBefore1week
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try {
            currentDate = dateFormat.parse(dateFormat.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.add(Calendar.DATE, -6);

        //This is the date 7 days ago == 1 week
        dateBefore1week = cal.getTime();

        //Build the Barchart
        final BarChart chart = (BarChart) view.findViewById(R.id.chart);
        loadFilteredList(appName); //method to get all connection from the app "appName"
        fillChart(view, chart); //method to fill the chart with the filteredList
        fillRecyclerList(view, filtered_Entities); //method to show all connection

        //Listener for Value Selection
        chart.setOnChartValueSelectedListener( new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                //Handling the current time in Hour
                int currentDay = currentDate.getDate();
                int shift = currentDay-6; //the shift that is needed to get the correct connections
                //extra cacheList to only show the reports to the selected value in the chart
                List<ReportEntity> cacheList = new ArrayList<ReportEntity>();
                if(e.getY() != 0){
                    for (ReportEntity cacheEntity : filtered_Entities){
                        int daysBetween = getDaysBetween(dateBefore1week, getEntityDate(cacheEntity));
                        if(daysBetween == e.getX()){
                            if(h.getStackIndex()==0 && cacheEntity.getConnectionInfo().contains("Unknown")){
                                cacheList.add(cacheEntity);
                            }
                            if(h.getStackIndex()==1 && cacheEntity.getConnectionInfo().contains("Encrypted")){
                                cacheList.add(cacheEntity);
                            }
                            if(h.getStackIndex()==2 && cacheEntity.getConnectionInfo().contains("Unencrypted")){
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
        int currentDay = currentDate.getDate();

        //Putting reportEntitites into a array for chart
        List<BarEntry> entry = new ArrayList<BarEntry>();

        int[] lastWeek_encrypted = new int[7];
        int[] lastWeek_unencrypted = new int[7];
        int[] lastWeek_unknown = new int[7];

        for (ReportEntity reportEntity : filtered_Entities) {
            int daysBetween = getDaysBetween(dateBefore1week, getEntityDate(reportEntity));
            //Increase the field of the array of the entityDay
            if(reportEntity.getConnectionInfo().contains("Encrypted")){
                lastWeek_encrypted[daysBetween] = lastWeek_encrypted[daysBetween] + 1;
            }else if(reportEntity.getConnectionInfo().contains("Unencrypted")){
                lastWeek_unencrypted[daysBetween] = lastWeek_unencrypted[daysBetween] + 1;
            }else if(reportEntity.getConnectionInfo().contains("Unknown")){
                lastWeek_unknown[daysBetween] = lastWeek_unknown[daysBetween] + 1;
            }

        }
        //adding data to chart
        for (int i = 0; i < lastWeek_encrypted.length;i++){
            entry.add(new BarEntry(i , new float[] {lastWeek_unknown[i],
                    lastWeek_encrypted[i],lastWeek_unencrypted[i]}));
        }

        BarDataSet barset = new BarDataSet(entry, "Days"); //TODO put in in german as well
        barset.setStackLabels(new String[]{"Unknown", "Encrypted", "Unencrypted"}); //TODO put in in german as well
        barset.setColors(new int[] {ContextCompat.getColor(getContext(), R.color.text_dark),
                ContextCompat.getColor(getContext(), R.color.green),
                ContextCompat.getColor(getContext(), R.color.red)});

        //X Achse Formatter--------------------------------------------------------

        // the labels that should be drawn on the XAxis
        final String[] days = new String[lastWeek_encrypted.length];

        for(int i = 0; i<days.length; i++){
            if(i == days.length-1){
                days[i] = currentDay+ " ."; //TODO add to german lang
            }else {
                days[i] = "- " + Integer.toString(6 - i) + "d"; //TODO add to german lang
            }
        }

        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return days[(int) value];
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
                for (String s : entitiesString){
                    if(s.equals(stringWithoutTimeStamp)){
                        isIncluded = true;
                    }
                }
                //if it is NOT included do....
                if (isIncluded == false) {
                    //Only entities 24 hours ago

                        String string_date = reportEntity.getTimeStamp();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                        // sdf.parse(string_date) --> this is the Entity date
                    try {
                        if (!sdf.parse(string_date).after(dateBefore1week)) {

                        }else{
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
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FragmentDayListAdapter(reportEntityList, getContext());
        mRecyclerView.setAdapter(mAdapter);
    }

    private Date getEntityDate(ReportEntity reportEntity){
        String string_timestamp = reportEntity.getTimeStamp();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date entity_date = null;
        try {
            entity_date = sdf.parse(string_timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return entity_date;
    }

    //https://www.java-forum.org/thema/datum-differenz-in-tagen-berechen.41934/
    static final long ONE_HOUR = 60 * 60 * 1000L;
    public int getDaysBetween(Date d1, Date d2){
        return (int) ( (d2.getTime() - d1.getTime() + ONE_HOUR) / (ONE_HOUR * 24));
    }

}
