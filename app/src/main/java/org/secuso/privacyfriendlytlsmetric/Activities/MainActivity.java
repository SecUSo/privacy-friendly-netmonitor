package org.secuso.privacyfriendlytlsmetric.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlytlsmetric.Assistant.Const;
import org.secuso.privacyfriendlytlsmetric.Assistant.ContextStorage;
import org.secuso.privacyfriendlytlsmetric.R;

import static org.secuso.privacyfriendlytlsmetric.R.id.imageView;

public class MainActivity extends BaseActivity {

    private boolean mIsServiceBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Fill the ContextStore with activity, then init the Service Handler
        ContextStorage.setContext(this);
        mIsServiceBind = ContextStorage.getServiceHandler().mIsBoundPassive;

        final Button startStop = (Button) findViewById(R.id.main_button);
        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mIsServiceBind) {
                    if(Const.IS_DEBUG) Log.d(Const.LOG_TAG, getResources().getString(R.string.passive_service_start));
                    ContextStorage.getServiceHandler().startPassiveService();
                    mIsServiceBind = ContextStorage.getServiceHandler().mIsBoundPassive;
                    ImageView imageView = (ImageView) findViewById(R.id.main_image_startstopp);
                    imageView.setImageDrawable(getDrawable(R.drawable.icon_on));
                    TextView textView = (TextView) findViewById(R.id.main_text_startstop);
                    textView.setText(getResources().getString(R.string.main_text_started));
                    startStop.setText(R.string.main_button_text_on);
                    // TODO: Implement minimization later on.
                    // minimizeActivity();
                } else {
                    if(Const.IS_DEBUG) Log.d(Const.LOG_TAG, getResources().getString(R.string.passive_service_stop));
                    ContextStorage.getServiceHandler().stopPassiveService();
                    mIsServiceBind = ContextStorage.getServiceHandler().mIsBoundPassive;
                    ImageView imageView = (ImageView) findViewById(R.id.main_image_startstopp);
                    imageView.setImageDrawable(getDrawable(R.drawable.icon_off));
                    TextView textView = (TextView) findViewById(R.id.main_text_startstop);
                    textView.setText(getResources().getString(R.string.main_text_stopped));
                    startStop.setText(R.string.main_button_text_off);
                }
            }
        });

        // Use the a button to display the welcome screen
        Button b = (Button) findViewById(R.id.button_welcomedialog);
        if(b != null) {
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    WelcomeDialog welcomeDialog = new WelcomeDialog();
                    welcomeDialog.show(getFragmentManager(), "WelcomeDialog");
                }
            });
        }

        overridePendingTransition(0, 0);
    }

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

    public void onClick(View view) {
        switch(view.getId()) {
            // do something with all these buttons?
            default:
        }
    }
}
