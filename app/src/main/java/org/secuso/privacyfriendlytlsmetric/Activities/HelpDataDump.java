package org.secuso.privacyfriendlytlsmetric.Activities;

import android.content.Context;

import org.secuso.privacyfriendlytlsmetric.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class structure taken from tutorial at http://www.journaldev.com/9942/android-expandablelistview-example-tutorial
 * last access 27th October 2016
 */

public class HelpDataDump {

    private Context context;

    public HelpDataDump(Context context) {
        this.context = context;
    }

    public HashMap<String, List<String>> getDataGeneral() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> general = new ArrayList<String>();
        general.add(context.getResources().getString(R.string.help_whatis_answer));

        expandableListDetail.put(context.getResources().getString(R.string.help_whatis), general);
        return expandableListDetail;
    }

    public HashMap<String, List<String>> getDataFeatures() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> features = new ArrayList<String>();
        features.add(context.getResources().getString(R.string.help_feature_one_answer));

        expandableListDetail.put(context.getResources().getString(R.string.help_feature_one), features);

        return expandableListDetail;
    }

    public HashMap<String, List<String>> getDataPrivacy() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();


        List<String> privacy = new ArrayList<String>();
        privacy.add(context.getResources().getString(R.string.help_privacy_answer));

        List<String> permissions = new ArrayList<String>();
        permissions.add(context.getResources().getString(R.string.help_permission_answer));

        expandableListDetail.put(context.getResources().getString(R.string.help_privacy), privacy);
        expandableListDetail.put(context.getResources().getString(R.string.help_permission), permissions);
        return expandableListDetail;
    }
}
