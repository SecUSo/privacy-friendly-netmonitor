package org.secuso.privacyfriendlytlsmetric.Activities;
import org.secuso.privacyfriendlytlsmetric.R;

import android.os.Bundle;
import android.widget.ExpandableListView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class structure taken from tutorial at http://www.journaldev.com/9942/android-expandablelistview-example-tutorial
 * last access 27th October 2016
 */

public class HelpActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ExpandableListAdapter expandableListAdapter;
        HelpDataDump helpDataDump = new HelpDataDump(this);

        ExpandableListView generalExpandableListView = (ExpandableListView) findViewById(R.id.generalExpandableListView);

        HashMap<String, List<String>> expandableListDetail = helpDataDump.getDataGeneral();
        List<String> expandableListTitleGeneral = new ArrayList<String>(expandableListDetail.keySet());
        expandableListAdapter = new ExpandableListAdapter(this, expandableListTitleGeneral, expandableListDetail);
        generalExpandableListView.setAdapter(expandableListAdapter);

        ExpandableListView featuresExpandableListView = (ExpandableListView) findViewById(R.id.featuresExpandableListView);
        expandableListDetail = helpDataDump.getDataFeatures();
        List<String> expandableListTitleFeatures = new ArrayList<String>(expandableListDetail.keySet());
        expandableListAdapter = new ExpandableListAdapter(this, expandableListTitleFeatures, expandableListDetail);
        featuresExpandableListView.setAdapter(expandableListAdapter);

        ExpandableListView privacyExpandableListView = (ExpandableListView) findViewById(R.id.privacyExpandableListView);
        expandableListDetail = helpDataDump.getDataPrivacy();
        List<String> expandableListTitlePrivacy = new ArrayList<String>(expandableListDetail.keySet());
        expandableListAdapter = new ExpandableListAdapter(this, expandableListTitlePrivacy, expandableListDetail);
        privacyExpandableListView.setAdapter(expandableListAdapter);

        overridePendingTransition(0, 0);
    }

    protected int getNavigationDrawerID() { return R.id.nav_help; }

}
