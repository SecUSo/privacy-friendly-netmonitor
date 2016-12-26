package org.secuso.privacyfriendlytlsmetric.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlytlsmetric.Assistant.Const;
import org.secuso.privacyfriendlytlsmetric.Assistant.RunStore;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.PassiveService;
import org.secuso.privacyfriendlytlsmetric.R;

import static org.secuso.privacyfriendlytlsmetric.R.id.imageView;

public class MainActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RunStore.setContext(this);


        final Button startStop = (Button) findViewById(R.id.main_button);

        if(RunStore.getServiceHandler().isServiceRunning(PassiveService.class)){
            ImageView imageView = (ImageView) findViewById(R.id.main_image_startstopp);
            imageView.setImageDrawable(getDrawable(R.drawable.icon_on));
            TextView textView = (TextView) findViewById(R.id.main_text_startstop);
            textView.setText(R.string.main_text_started);
            startStop.setText(R.string.main_button_text_on);
            setInspectButton();
        }

        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!RunStore.getServiceHandler().isServiceRunning(PassiveService.class)) {
                    if(Const.IS_DEBUG) Log.d(Const.LOG_TAG, getResources().getString(R.string.passive_service_start));
                    RunStore.getServiceHandler().startPassiveService();
                    ImageView imageView = (ImageView) findViewById(R.id.main_image_startstopp);
                    imageView.setImageDrawable(getDrawable(R.drawable.icon_on));
                    TextView textView = (TextView) findViewById(R.id.main_text_startstop);
                    textView.setText(getResources().getString(R.string.main_text_started));
                    startStop.setText(R.string.main_button_text_on);
                    // TODO: Implement minimization later on.
                    // minimizeActivity();
                } else {
                    if(Const.IS_DEBUG) Log.d(Const.LOG_TAG, getResources().getString(R.string.passive_service_stop));
                    RunStore.getServiceHandler().stopPassiveService();
                    ImageView imageView = (ImageView) findViewById(R.id.main_image_startstopp);
                    imageView.setImageDrawable(getDrawable(R.drawable.icon_off));
                    TextView textView = (TextView) findViewById(R.id.main_text_startstop);
                    textView.setText(getResources().getString(R.string.main_text_stopped));
                    startStop.setText(R.string.main_button_text_off);
                }
                setInspectButton();
            }
        });



        // on click functionality for inspect button
        Button inspect = (Button) findViewById(R.id.button_inspect);
        inspect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (RunStore.getServiceHandler().isServiceRunning(PassiveService.class)){
                    Intent intent = new Intent(getApplicationContext(), ReportActivity.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        TaskStackBuilder builder = TaskStackBuilder.create(getApplicationContext());
                        builder.addNextIntentWithParentStack(intent);
                        builder.startActivities();
                    } else {
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
        overridePendingTransition(0, 0);
    }

    //TODO: start welcomediag
    //WelcomeDialog welcomeDialog = new WelcomeDialog();
    //welcomeDialog.show(getFragmentManager(), "WelcomeDialog");

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_main;
    }

    public static class WelcomeDialog extends DialogFragment {

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            LayoutInflater i = getActivity().getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(i.inflate(R.layout.welcome_dialog, null));
            builder.setIcon(R.mipmap.icon);
            builder.setTitle(getActivity().getString(R.string.welcome));
            builder.setPositiveButton(getActivity().getString(R.string.okay), null);
            builder.setNegativeButton(getActivity().getString(R.string.viewhelp), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((MainActivity)getActivity()).goToNavigationItem(R.id.nav_help);
                }
            });

            return builder.create();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //Kill the service if main activity gets destroyed
        /*
        if(RunStore.getServiceHandler().isServiceRunning(PassiveService.class)) {
            RunStore.getServiceHandler().stopPassiveService();
        }*/
    }

    // set the inspect button based on service status
    private void setInspectButton(){
        Button inspect = (Button) findViewById(R.id.button_inspect);
        if(!RunStore.getServiceHandler().isServiceRunning(PassiveService.class)) {
            inspect.setBackground(getDrawable(R.drawable.button_disabled));
            TextView textView = (TextView) findViewById(R.id.main_text_inspect_info);
            textView.setText(getString(R.string.main_text_desc_inspect_off));
        } else {
            inspect.setBackground(getDrawable(R.drawable.button_fullwidth));
            TextView textView = (TextView) findViewById(R.id.main_text_inspect_info);
            textView.setText(getString(R.string.main_text_desc_inspect_on));
        }
    }

}
