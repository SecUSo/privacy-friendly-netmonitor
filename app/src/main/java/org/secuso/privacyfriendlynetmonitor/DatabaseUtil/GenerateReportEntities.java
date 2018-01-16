package org.secuso.privacyfriendlynetmonitor.DatabaseUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by m4rc0 on 13.01.2018.
 */

public class GenerateReportEntities {

    public static void generateReportEntities(Context context, ReportEntityDao reportEntityDao) {

        System.out.println("Start with entity generation");

        List<String> appNames = new ArrayList<>();
        List<ReportEntity> reportEntities = new ArrayList<>();
        appNames.add("com.android.chrome");
        appNames.add("com.android.vending");
        appNames.add("com.google.android.youtube");

        SharedPreferences selectedAppsPreferences = context.getSharedPreferences("SELECTEDAPPS", 0);
        SharedPreferences.Editor editor = selectedAppsPreferences.edit();

        PackageManager packageManager = context.getPackageManager();

        Set<String> set = selectedAppsPreferences.getAll().keySet();
        for (String appName : appNames) {
            if (!selectedAppsPreferences.contains(appName)) {
                editor.putString(appName, appName);
                editor.commit();
            }
        }

        int count = 1;
        long before = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {

            for (int year = 2018; year <= 2018; year++) {

                System.out.println("New year: " + year);

                for (int month = 1; month <= 1; month++) {

                    System.out.println("New Month: " + month);

                    for (int day = 1; day <= 30; day++) {

                        System.out.println("New Day: " + day);

                        for (int hour = 0; hour < 24; hour++) {


                            for (String appName : appNames) {

                                ReportEntity reportEntity = new ReportEntity();
                                PackageInfo packageInfo = null;
                                try {
                                    packageInfo = packageManager.getPackageInfo(appName, 0);
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                }

                                reportEntity.setUserID((new String()).valueOf(packageInfo.applicationInfo.uid));
                                reportEntity.setAppName(appName);
                                reportEntity.setRemoteAddress(getRandomString());
                                reportEntity.setRemoteHex(getRandomString());
                                reportEntity.setRemoteHost(getRandomString());
                                reportEntity.setLocalAddress(getRandomString());
                                reportEntity.setLocalHex(getRandomString());
                                reportEntity.setServicePort(getRandomString());
                                reportEntity.setPayloadProtocol(getRandomString());
                                reportEntity.setTransportProtocol(getRandomString());
                                reportEntity.setLocalPort(getRandomString());
                                reportEntity.setConnectionInfo(getRandomString());

                                String monthString = "";
                                if (month < 10) {
                                    monthString = "0" + (new String()).valueOf(month);
                                } else {
                                    monthString = (new String()).valueOf(month);
                                }
                                String dayString = "";
                                if (day < 10) {
                                    dayString = "0" + (new String()).valueOf(day);
                                } else {
                                    dayString = (new String()).valueOf(day);
                                }
                                String hourString = "";
                                if (hour < 10) {
                                    hourString = "0" + (new String()).valueOf(hour);
                                } else {
                                    hourString = (new String()).valueOf(hour);
                                }

                                String date = (new String()).valueOf(year) + "-" + monthString + "-" + dayString + " " + hourString + ":04:20.420";

                                reportEntity.setTimeStamp(date);

                                reportEntityDao.insertOrReplace(reportEntity);
                                count++;
                            }

                        }

                    }

                }

            }

        }
        long after = System.currentTimeMillis();
        System.out.println("Generation needed " + (after-before)/1000 + " seconds, for the generation of " + (count-1) + " reports");
    }

    private static String getRandomString() {

        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder stringBuilder = new StringBuilder();
        Random rnd = new Random();

        while (stringBuilder.length() < 18) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            stringBuilder.append(SALTCHARS.charAt(index));
        }
        String randomString = stringBuilder.toString();
        return randomString;
    }

}
