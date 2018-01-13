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

import org.secuso.privacyfriendlynetmonitor.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tobias on 04.01.18.
 * https://github.com/PhilJay/MPAndroidChart/wiki/Setting-Data
 */

public class Fragment_day extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_day_layout, container, false);
        fillChart(view);

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

        return view;
    }

    private void fillChart(View view){
        BarChart chart = (BarChart) view.findViewById(R.id.chart);

        List<BarEntry> entries = new ArrayList<BarEntry>();
        entries.add(new BarEntry(0f,30f));
        entries.add(new BarEntry(1f,50f));
        //entries.add(new BarEntry(2f,100f));
        entries.add(new BarEntry(3f,80f));
        entries.add(new BarEntry(4f,10f));

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
        barData.setBarWidth(0.9f);
        chart.setData(barData);
        chart.setFitBars(true);
        //Sets the desc label at the bottom to " "
            Description description = new Description();
            description.setText("");
            chart.setDescription(description);
        chart.invalidate();

    }

}
