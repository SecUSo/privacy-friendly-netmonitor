package org.secuso.privacyfriendlynetmonitor.fragment;

import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

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
 */

//        String someDate = "2018-01-06 13:39:33.888";
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//        Date date = null;
//        try {
//            date = sdf.parse(someDate);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        System.out.println(date.getTime());

public class Fragment_day extends Fragment {

    // ReportEntity Table and ReportEntities List
    private static ReportEntityDao reportEntityDao;
    private static List<ReportEntity> reportEntities;
    private static List<ReportEntity> filtered_Entities = new ArrayList<>();
    private static List<String> entitiesString = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_day_layout, container, false);

        //Fill Icon, AppGroupTitle, AppName
        TextView tx_appName = view.findViewById(R.id.historyGroupSubtitle);
        String appName = getArguments().getString("AppName");
        tx_appName.setText(appName);

        PackageManager packageManager = getActivity().getPackageManager();
        try {
            String appGroupTitle = (String) packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(appName, PackageManager.GET_META_DATA));
            TextView tx_appGroupTitle = view.findViewById(R.id.historyGroupTitle);
            tx_appGroupTitle.setText(appGroupTitle);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ImageView appIcon = (ImageView) view.findViewById(R.id.historyGroupIcon);
        try {
            appIcon.setImageDrawable(packageManager.getApplicationIcon(appName));
        } catch (PackageManager.NameNotFoundException e) {
        }
        //END Fill Icon, AppGroupTitle, AppName

        loadFilteredList(appName);
        fillChart(view);

        return view;
    }

    private void loadFilteredList(String appName) {
        filtered_Entities.clear();
        entitiesString.clear();

        // load DB
        DaoSession daoSession = ((DBApp) getActivity().getApplication()).getDaoSession();
        reportEntityDao = daoSession.getReportEntityDao();
        reportEntities = reportEntityDao.loadAll();

        boolean isIncluded = false;

        for (ReportEntity reportEntity : reportEntities) {
            //Only entities from the AppName
            if (reportEntity.getAppName().equals(appName)) {
                String stringWithoutTimeStamp = reportEntity.toStringWithoutTimestamp();
                for (String s : entitiesString){
                    if(s.equals(stringWithoutTimeStamp)){
                        isIncluded = true;
                    }
                }
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
                        Date entity_date = null;
                        try {
                            entity_date = sdf.parse(string_date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (entity_date.after(dateBefore1Days)) {
                            filtered_Entities.add(reportEntity); // add only that report from that app and 24hours ago
                            entitiesString.add(stringWithoutTimeStamp);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                isIncluded = false;
            }
        }
    }

    private void fillChart(View view) {
        BarChart chart = (BarChart) view.findViewById(R.id.chart);
        List<BarEntry> entries = new ArrayList<BarEntry>();

        int[] last24hours = new int[23];

        for (ReportEntity reportEntity : filtered_Entities) {
            String string_timestamp = reportEntity.getTimeStamp();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Date entity_date = null;
            try {
                entity_date = sdf.parse(string_timestamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            int hourEntity = entity_date.getHours();
            last24hours[hourEntity-1] = last24hours[hourEntity-1] + 1;
        }

        for (int i = 0; i < last24hours.length;i++){
            entries.add(new BarEntry(i, last24hours[i]));
        }


        BarDataSet barset = new BarDataSet(entries, "Hours"); //TODO put in in german as well
        barset.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));

        //X Achse Formatter--------------------------------------------------------
        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        //Y Achse Formatter----------------------------------------------------------
        YAxis yAxis_left = chart.getAxisLeft();
        yAxis_left.setAxisMinimum(0f);
        YAxis yAxis_right = chart.getAxisRight();
        yAxis_right.setAxisMinimum(0f);

        BarData barData = new BarData(barset);
        barData.setBarWidth(0.2f);
        chart.setData(barData);
        chart.setFitBars(true);
        //Sets the desc label at the bottom to " "
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);
        chart.invalidate();

    }


}
