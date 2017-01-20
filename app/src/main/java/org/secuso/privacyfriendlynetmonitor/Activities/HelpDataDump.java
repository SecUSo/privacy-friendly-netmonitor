package org.secuso.privacyfriendlynetmonitor.Activities;

import android.content.Context;

import org.secuso.privacyfriendlynetmonitor.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Class structure taken from tutorial at http://www.journaldev.com/9942/android-expandablelistview-example-tutorial
 * last access 27th October 2016
 */

class HelpDataDump {

    private Context context;

    HelpDataDump(Context context) {
        this.context = context;
    }

    HashMap<String, List<String>> getDataGeneral() {
        HashMap<String, List<String>> expandableListDetail = new LinkedHashMap<>();

        List<String> general = new ArrayList<>();
        general.add(context.getResources().getString(R.string.help_whatis_answer));
        expandableListDetail.put(context.getResources().getString(R.string.help_whatis), general);

        List<String> features1 = new ArrayList<>();
        features1.add(context.getResources().getString(R.string.help_feature_one_answer));
        expandableListDetail.put(context.getResources().getString(R.string.help_feature_one), features1);

        List<String> features2 = new ArrayList<>();
        features2.add(context.getResources().getString(R.string.help_feature_two_answer));
        expandableListDetail.put(context.getResources().getString(R.string.help_feature_two), features2);

        List<String> features3 = new ArrayList<>();
        features3.add(context.getResources().getString(R.string.help_feature_three_answer));
        expandableListDetail.put(context.getResources().getString(R.string.help_feature_three), features3);

        List<String> features4 = new ArrayList<>();
        features4.add(context.getResources().getString(R.string.help_feature_four_answer));
        expandableListDetail.put(context.getResources().getString(R.string.help_feature_four), features4);

        List<String> features5 = new ArrayList<>();
        features5.add(context.getResources().getString(R.string.help_feature_five_answer));
        expandableListDetail.put(context.getResources().getString(R.string.help_feature_five), features5);

        List<String> privacy = new ArrayList<>();
        privacy.add(context.getResources().getString(R.string.help_privacy_answer));
        expandableListDetail.put(context.getResources().getString(R.string.help_privacy), privacy);

        List<String> permissions = new ArrayList<>();
        permissions.add(context.getResources().getString(R.string.help_permission_answer));
        expandableListDetail.put(context.getResources().getString(R.string.help_permission), permissions);

        return expandableListDetail;
    }
}
