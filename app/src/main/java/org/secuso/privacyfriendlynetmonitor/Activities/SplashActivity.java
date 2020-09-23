/**
 * This file is part of Privacy Friendly Password Generator.
 * Privacy Friendly Password Generator is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or any later version.
 * Privacy Friendly Password Generator is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Privacy Friendly Password Generator. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlynetmonitor.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.secuso.privacyfriendlynetmonitor.Assistant.PrefManager;
import org.secuso.privacyfriendlynetmonitor.R;

/**
 * @author Karola Marky
 * @version 20161022
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent mainIntent = null;
        PrefManager prefManager = new PrefManager(this);
        if(prefManager.isFirstTimeLaunch()) {
            mainIntent = new Intent(this, TutorialActivity.class);
            prefManager.setFirstTimeLaunch(false);
        } else {
            mainIntent = new Intent(this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }


        setContentView(R.layout.activity_splash);
        TextView compatibilityInfo = findViewById(R.id.compatibility_text);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
            compatibilityInfo.setVisibility(View.VISIBLE);
        } else {
            compatibilityInfo.setVisibility(View.INVISIBLE);

            //Intent mainIntent = new Intent(SplashActivity.this, TutorialActivity.class);
            SplashActivity.this.startActivity(mainIntent);
            SplashActivity.this.finish();
        }



    }


}
