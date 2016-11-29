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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlytlsmetric.Assistant.Const;
import org.secuso.privacyfriendlytlsmetric.Assistant.ContextStorage;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.PassiveService;
import org.secuso.privacyfriendlytlsmetric.R;


/**
 * Activity of the Main Panel. Start and stop everything from here.
 */
public class MainActivity extends AppCompatActivity {

    private boolean mIsServiceBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Fill the ContextStore with activity, then init the Service Handler
        ContextStorage.setContext(this);
        mIsServiceBind = ContextStorage.getServiceHandler().mIsBoundPassive;

        final Button startStop = (Button) findViewById(R.id.startStop);
        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView infoText = (TextView) findViewById(R.id.infoText);
                if(!mIsServiceBind) {
                    startStop.setBackground(getResources().getDrawable(R.drawable.power_working));
                    if(Const.IS_DEBUG) Log.d(Const.LOG_TAG, "Init service start.");
                    ContextStorage.getServiceHandler().startPassiveService();
                    mIsServiceBind = ContextStorage.getServiceHandler().mIsBoundPassive;
                    startStop.setBackground(getResources().getDrawable(R.drawable.power_on));
                    // TODO: Implement minimization later on.
                    // minimizeActivity();
                } else {
                    startStop.setBackground(getResources().getDrawable(R.drawable.power_working));
                    if(Const.IS_DEBUG) Log.d(Const.LOG_TAG, "Init service stop.");
                    ContextStorage.getServiceHandler().stopPassiveService();
                    mIsServiceBind = ContextStorage.getServiceHandler().mIsBoundPassive;
                    startStop.setBackground(getResources().getDrawable(R.drawable.power_off));
                }
            }
        });

        Button gotoEvidence = (Button) findViewById(R.id.gotoEvidence);
        gotoEvidence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsServiceBind){
                    Intent intent = new Intent(ContextStorage.getContext(), EvidenceActivity.class);
                    startActivity(intent);
                } else {
                    Toast toast = Toast.makeText(ContextStorage.getContext(), R.string.info_service_offline, Toast.LENGTH_LONG);
                    toast.show();
                }

            }
        });


        if(mIsServiceBind){
            startStop.setBackground(getResources().getDrawable(R.drawable.power_on));
        } else {
            startStop.setBackground(getResources().getDrawable(R.drawable.power_off));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tlsmetric, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }


    //Call this to minimize the activity
    private void minimizeActivity(){
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

}
