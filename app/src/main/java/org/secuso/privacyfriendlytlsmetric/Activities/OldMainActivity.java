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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.secuso.privacyfriendlytlsmetric.Assistant.Const;
import org.secuso.privacyfriendlytlsmetric.Assistant.RunStore;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.PassiveService;
import org.secuso.privacyfriendlytlsmetric.R;


/**
 * Activity of the Main Panel. Start and stop everything from here.
 */
public class OldMainActivity extends BaseActivity {

    private boolean mIsServiceBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Fill the ContextStore with activity, then init the Service Handler
        RunStore.setContext(this);
        mIsServiceBind = RunStore.getServiceHandler().isServiceRunning(PassiveService.class);

        final Button startStop = (Button) findViewById(R.id.game_button_start);
        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mIsServiceBind) {
                    startStop.setBackground(getResources().getDrawable(R.drawable.power_working));
                    if(Const.IS_DEBUG) Log.i(Const.LOG_TAG, "Init service start.");
                    //RunStore.getServiceHandler().startPassiveService();
                    RunStore.getServiceHandler().bindPassiveService(getApplicationContext());
                    mIsServiceBind = RunStore.getServiceHandler().isServiceRunning(PassiveService.class);
                    startStop.setBackground(getResources().getDrawable(R.drawable.power_on));
                    // TODO: Implement minimization later on.
                    // minimizeActivity();
                } else {
                    startStop.setBackground(getResources().getDrawable(R.drawable.power_working));
                    if(Const.IS_DEBUG) Log.d(Const.LOG_TAG, "Init service stop.");
                    RunStore.getServiceHandler().stopPassiveService();
                    mIsServiceBind = RunStore.getServiceHandler().isServiceRunning(PassiveService.class);
                    startStop.setBackground(getResources().getDrawable(R.drawable.power_off));
                }
            }
        });

        Button gotoEvidence = (Button) findViewById(R.id.gotoEvidence);
        gotoEvidence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsServiceBind){
                    Intent intent = new Intent(RunStore.getContext(), ReportActivity.class);
                    startActivity(intent);
                } else {
                    Toast toast = Toast.makeText(RunStore.getContext(), R.string.info_service_offline, Toast.LENGTH_LONG);
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
    protected int getNavigationDrawerID() {
        return R.id.nav_main;
    }


    @Override
    public void onDestroy() {
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
