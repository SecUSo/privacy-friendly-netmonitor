/**
 * This file is part of Privacy Friendly Password Generator.
 Privacy Friendly Password Generator is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.
 Privacy Friendly Password Generator is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with Privacy Friendly Password Generator. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlynetmonitor.Assistant;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Class structure taken from tutorial at http://www.androidhive.info/2016/05/android-build-intro-slider-app/
 * @author Karola Marky
 * @version 20170112
 */
public class PrefManager {
    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor;

    // shared pref mode
    private int PRIVATE_MODE = 0;

    // Shared preferences file name

    public PrefManager(Context context) {
        pref = context.getSharedPreferences(Const.PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public static void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(Const.IS_FIRST_START, isFirstTime);
        editor.commit();
    }

    public static boolean isFirstTimeLaunch() { return pref.getBoolean(Const.IS_FIRST_START, true); }

}