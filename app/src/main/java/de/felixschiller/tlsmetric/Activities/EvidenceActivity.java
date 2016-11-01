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

package de.felixschiller.tlsmetric.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.felixschiller.tlsmetric.Assistant.Const;
import de.felixschiller.tlsmetric.Assistant.ContextSingleton;
import de.felixschiller.tlsmetric.PacketProcessing.Report;
import de.felixschiller.tlsmetric.PacketProcessing.Evidence;
import de.felixschiller.tlsmetric.PacketProcessing.PackageInformation;
import de.felixschiller.tlsmetric.R;

/**
 * Lists a Report of each detected connection. Most critical if several reports exist.
 */
public class EvidenceActivity extends AppCompatActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evidence);
        ContextSingleton.setContext(this);
        Evidence.newWarnings = 0;

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.evidence_toolbar);
        setSupportActionBar(toolbar);
        //toolbar.setLogo(R.mipmap.icon);
        toolbar.setLogoDescription(R.string.app_name);


        //EvidenceList
        final ListView listview = (ListView) findViewById(android.R.id.list);

        final EvidenceAdapter adapter;
        if(Evidence.mEvidence != null){
            adapter = new EvidenceAdapter(this, copyArrayList(Evidence.getSortedEvidence()));
        } else {
            if(Const.IS_DEBUG) Log.e(Const.LOG_TAG, "Evidence list not existing or empty!");
            adapter = new EvidenceAdapter(this, new ArrayList<Report>());
        }

        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final Report ann = (Report) parent.getItemAtPosition(position);
                view.animate().setDuration(500).alpha((float)0.5)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                if (ann.filter.severity != -1) {
                                    Evidence.setSortedEvidenceDetail(ann.srcPort);
                                    Intent intent = new Intent(ContextSingleton.getContext(), EvidenceDetailActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast toast = Toast.makeText(ContextSingleton.getContext(), "No detail availiable for this connection", Toast.LENGTH_LONG);
                                    toast.show();
                                }
                            }
                        });
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_tlsmetric, menu);
        return true;
    }

    //menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;

            case R.id.action_back:
                Intent intent = new Intent(ContextSingleton.getContext(), MainActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_refresh:
                Evidence.disposeInactiveEvidence();
                Evidence.updateConnections();
                ListView listview = (ListView) findViewById(android.R.id.list);
                EvidenceAdapter adapter = new EvidenceAdapter(ContextSingleton.getContext(),
                        copyArrayList(Evidence.getSortedEvidence()));
                listview.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Customised Adapter class for display of Evidence Reports
    private class EvidenceAdapter extends ArrayAdapter<Report> {

        private Report[] anns;
        private final Context context;

        public EvidenceAdapter(Context context, ArrayList<Report> AnnList) {
            super(context, R.layout.evidence_list_entry, AnnList);
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

            View rowView = inflater.inflate(R.layout.evidence_list_entry, parent, false);

            //if unknown app (-1) try again to get pid by sourcePort;
            Report ann = anns[position];
            if(ann.pid == -1 && ann.uid == -1){
                ann.pid = Evidence.getPidByPort(ann.srcPort);
                ann.uid = Evidence.getUidByPort(ann.srcPort);
                if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "Rescan of pid and uid. srcPort: " +
                        ann.srcPort + " new pid: " + ann.pid
                        + " new uid: " + ann.uid);
            }

            PackageInformation pi = Evidence.getPackageInformation(ann.pid, ann.uid);
            //First Line Text
            TextView firstLine = (TextView) rowView.findViewById(R.id.firstLine);
            String first = pi.packageName;
            firstLine.setText(first);

            //second Line Text
            TextView secondLine = (TextView) rowView.findViewById(R.id.secondLine);
            String second = "Host: " + ann.url;
            secondLine.setText(second);

            //App icon
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            imageView.setImageDrawable(pi.icon);

            //Status icon
            ImageView imageStatusView = (ImageView) rowView.findViewById(R.id.statusIcon);
            int severity = ann.filter.severity;
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
            }

            //Status Text
            TextView statusLine = (TextView) rowView.findViewById(R.id.statusLine);
            String status = "Level :" + severity;
            statusLine.setText(status);
            return rowView;
        }

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public ArrayList<Report> copyArrayList(ArrayList<Report> anns){
        ArrayList<Report> copy = new ArrayList<>();
        for(Report ann: anns){
            copy.add(ann);
        }
        return copy;
    }


}