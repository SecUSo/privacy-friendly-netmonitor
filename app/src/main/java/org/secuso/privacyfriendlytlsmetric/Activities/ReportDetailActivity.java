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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import org.secuso.privacyfriendlytlsmetric.Assistant.RunStore;

import org.secuso.privacyfriendlytlsmetric.R;

/**
 * Evidence Detail Panel. List all reports of a connection, invoked by Evidence Panel (ReportActivity)
 */
public class ReportDetailActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);
        RunStore.setContext(this);

        final ListView listview = (ListView) findViewById(android.R.id.list);

        final DetailAdapter adapter;
        adapter = new DetailAdapter(this, R.layout.report_detail_item, new ArrayList<String[]>());
        listview.setAdapter(adapter);
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    //Implementation of List Adapter
    class DetailAdapter extends ArrayAdapter<String[]> {

        public DetailAdapter(Context context, int resource) {

            super(context, resource);
        }

        public DetailAdapter(Context context, int resource, List<String[]> detailList) {

            super(context, resource, detailList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.report_detail_item, null);
            }

            String[] detail = getItem(position);

            TextView type = (TextView) v.findViewById(R.id.report_detail_item_type);
            TextView value = (TextView) v.findViewById(R.id.report_detail_item_value);
            if (detail[0] != null && detail[1] != null) {
                type.setText(detail[0]);
                value.setText(detail[1]);
            } else {
                type.setText("type");
                value.setText("null");
            }

            return v;
        }
    }
}

