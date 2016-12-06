package org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Set;

/**
 * Collector class collects data from the services and processes it for inter process communication
 * with the UI.
 */

public class Collector {

    private static ArrayList<Report> mReportList;
    public static Report[] mReportArray;
    //Pushed the newest availiable information to binder. The param indicates the update-strategy
    //of the reports. See Detector for further information

    public static void provideReports(){
        updateReportList();

        mReportArray = new Report[mReportList.size()];
        for (int i = 0; i < mReportList.size(); i++){
            mReportArray[i] = mReportList.get(i);
        }
    }

    public static void updateReportList(){
        pull();
        processPassive();
    }

    //pull records from detector and make a deep copy for frontend
    private static void pull() {
        ArrayList<Report> reportList = new ArrayList<>();
        Set<Integer> keySet = Detector.sReportMap.keySet();
        for(int i : keySet){
            reportList.add(Detector.sReportMap.get(i));
        }
        mReportList = deepCloneReportList(reportList);
        //mReportList = (ArrayList<Report>) mReportList.clone();
    }

    //Process all Records in the List, based on a passive service
    private static void processPassive() {
        //TODO: Process Packet information
    }

    //Make a deep copy of the report list
    private static ArrayList<Report> deepCloneReportList(ArrayList<Report> reportList) {
        ArrayList<Report> cloneList = new ArrayList<>();
        try {
            for (int i = 0; i < reportList.size(); i++) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(byteOut);
                out.writeObject(reportList.get(i));
                out.flush();
                ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()));
                cloneList.add(Report.class.cast(in.readObject()));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return cloneList;
    }
}
