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
                                //reportEntity.setConnectionInfo(getRandomString());

                                double random = Math.random();
                                if (random < 0.5) {
                                    reportEntity.setConnectionInfo("Encrypted()");
                                } else {
                                    reportEntity.setConnectionInfo("Unencrypted()");
                                }

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
        System.out.println("Generation needed " + (after - before) / 1000 + " seconds, for the generation of " + (count - 1) + " reports");
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
