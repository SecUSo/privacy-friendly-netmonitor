/*
    TLSMetric
    - Copyright (2015, 2016) Felix Tsala Schiller

    ###################################################################

    This file is part of TLSMetric.

    TLSMetric is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TLSMetric is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TLSMetric.  If not, see <http://www.gnu.org/licenses/>.

    Diese Datei ist Teil von TLSMetric.

    TLSMetric ist Freie Software: Sie können es unter den Bedingungen
    der GNU General Public License, wie von der Free Software Foundation,
    Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
    veröffentlichten Version, weiterverbreiten und/oder modifizieren.

    TLSMetric wird in der Hoffnung, dass es nützlich sein wird, aber
    OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
    Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Siehe die GNU General Public License für weitere Details.

    Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
    Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlytlsmetric.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import org.secuso.privacyfriendlytlsmetric.Assistant.Const;
import org.secuso.privacyfriendlytlsmetric.Assistant.RunStore;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Report;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Evidence;
import org.secuso.privacyfriendlytlsmetric.R;

/**
 * Evidence Detail Panel. List all reports of a connection, invoked by Evidence Panel (ReportActivity)
 */
public class EvidenceDetailActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_content);
        RunStore.setContext(this);


        //EvidenceList
        final ListView listview = (ListView) findViewById(android.R.id.list);

        final DetailAdapter adapter;
        if(Evidence.mEvidence != null){
            adapter = new DetailAdapter(this, copyArrayList(Evidence.mEvidenceDetail));
        } else {
            if(Const.IS_DEBUG) Log.e(Const.LOG_TAG, "Evidence list not existing or empty!");
            adapter = new DetailAdapter(this, new ArrayList<Report>());
            Toast.makeText(EvidenceDetailActivity.this, "No connections availiable.", Toast.LENGTH_SHORT).show();
        }

        listview.setAdapter(adapter);
    }

/*
       //old menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;

            case R.id.action_back:
                    Intent intent = new Intent(RunStore.getContext(), ReportActivity.class);
                    startActivity(intent);
                return true;

            case R.id.action_refresh:
                Evidence.disposeInactiveEvidence();
                Evidence.updateConnections();
                ListView listview = (ListView) findViewById(android.R.id.list);
                DetailAdapter adapter = (DetailAdapter)listview.getAdapter();
                adapter.notifyDataSetChanged();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
*/



    private class DetailAdapter extends ArrayAdapter<Report> {

        private final Report[] anns;
        private final Context context;

        public DetailAdapter(Context context, ArrayList<Report> AnnList) {
            super(context, R.layout.evidence_detail_entry, AnnList);
            this.context = context;
            this.anns = new Report[AnnList.size()];
            for(int i = 0; i < AnnList.size(); i++){
                this.anns[i] = AnnList.get(i);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.evidence_detail_entry, parent, false);

            //TODO: Implement IPC
           /* PkgInfo pi = Evidence.getPackageInformation(anns[position].pid, anns[position].uid);
            //First Line Text
            TextView firstLine = (TextView) rowView.findViewById(R.id.firstLine);
            String first = pi.appName;
            firstLine.setText(first);

            //Detail Text Field
            TextView detail = (TextView) rowView.findViewById(R.id.detail);
            String detailText = generateDetail(anns[position]);
            detail.setText(detailText);

            //App icon
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            imageView.setImageDrawable(pi.icon);

            //Status icon
            ImageView imageStatusView = (ImageView) rowView.findViewById(R.id.statusIcon);
            int severity = anns[position].filter.severity;
            if(severity == 3){
                imageStatusView.setImageResource(R.mipmap.icon_warn_red);
            } else if (severity == 2){
                imageStatusView.setImageResource(R.mipmap.icon_warn_orange);
            } else if (severity == 1) {
                imageStatusView.setImageResource(R.mipmap.icon_warn_orange);
            } else if (severity == 0){
                imageStatusView.setImageResource(R.mipmap.icon_ok);
            } else if (severity == -1) {
                imageStatusView.setImageResource(R.mipmap.icon_quest);
            }*/

            return rowView;
        }

    }

    //Generate detail information based ob the report.
    private String generateDetail(Report ann) {
        String detail = ann.filter.description;
        detail += " \n Severity: " + ann.filter.severity ;
        switch (ann.filter.severity){
            case -1:
                detail += " - connection information.";
                break;
            case 0:
                detail += " - secure connection.";
                break;
            case 1:
                detail += " - minor warning.";
                break;
            case 2:
                detail += " - major warning.";
                break;
            case 3:
                detail += " - unencrypted connection.";
                break;
            default:
                break;
        }
        detail += " \n Protocol: " + ann.filter.protocol;
        detail += " \n Time: " + ann.timestamp.toString();
        detail += " \n Target Host IP: " + ann.getRemoteAdd().getHostAddress();
        detail += " \n Target Hostname: " + ann.getRemoteAdd().getHostName();
        detail += " \n Source Port: " + ann.getLocalPort();
        detail += " \n Destination Port: " + ann.getLocalPort();
        if(ann.getPid() == -1){
            detail += " \n App process ID: UNKNOWN";
        } else {
            detail += " \n App process ID (PID) : " + ann.getPid();
        }
        if(ann.getUid() == -1){
            detail += " \n App user ID (UID) : UNKNOWN";
        } else {
            detail += " \n App process ID: " + ann.getUid();
        }
        return detail;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    //Poor excuse for a copy constructor
    //TODO: Make real copy constructors
    public ArrayList<Report> copyArrayList(ArrayList<Report> anns){
        ArrayList<Report> copy = new ArrayList<>();
        for(Report ann: anns){
            copy.add(ann);
        }
        return copy;
    }


}