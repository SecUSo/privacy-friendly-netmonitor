/*
    Privacy Friendly Net Monitor (Net Monitor)
    - Copyright (2015 - 2017) Felix Tsala Schiller

    ###################################################################

    This file is part of Net Monitor.

    Net Monitor is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Net Monitor is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Net Monitor.  If not, see <http://www.gnu.org/licenses/>.

    Diese Datei ist Teil von Net Monitor.

    Net Monitor ist Freie Software: Sie können es unter den Bedingungen
    der GNU General Public License, wie von der Free Software Foundation,
    Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
    veröffentlichten Version, weiterverbreiten und/oder modifizieren.

    Net Monitor wird in der Hoffnung, dass es nützlich sein wird, aber
    OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
    Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Siehe die GNU General Public License für weitere Details.

    Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
    Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.

    ###################################################################

    This app has been created in affiliation with SecUSo-Department of Technische Universität
    Darmstadt.

    The design is based on the Privacy Friendly Example App template by Karola Marky, Christopher
    Beckmann and Markus Hau (https://github.com/SecUSo/privacy-friendly-app-example).

    Privacy Friendly Net Monitor is based on TLSMetric by Felix Tsala Schiller
    https://bitbucket.org/schillef/tlsmetric/overview.

 */
package org.secuso.privacyfriendlynetmonitor.Activities;

import android.content.Context;

import org.secuso.privacyfriendlynetmonitor.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Activity for displaying help content.
 *
 * Class structure taken from tutorial at http://www.journaldev.com/9942/android-expandablelistview-example-tutorial
 * last access 27th October 2016
 */

class HelpDataDump {

    private Context context;

    HelpDataDump(Context context) {
        this.context = context;
    }

    HashMap<String, List<String>> getDataGeneral() {
        HashMap<String, List<String>> expandableListDetail = new LinkedHashMap<>();

        List<String> general = new ArrayList<>();
        general.add(context.getResources().getString(R.string.help_whatis_answer));
        expandableListDetail.put(context.getResources().getString(R.string.help_whatis), general);

        List<String> features1 = new ArrayList<>();
        features1.add(context.getResources().getString(R.string.help_feature_one_answer));
        expandableListDetail.put(context.getResources().getString(R.string.help_feature_one), features1);

        List<String> features2 = new ArrayList<>();
        features2.add(context.getResources().getString(R.string.help_feature_two_answer));
        expandableListDetail.put(context.getResources().getString(R.string.help_feature_two), features2);

        List<String> features3 = new ArrayList<>();
        features3.add(context.getResources().getString(R.string.help_feature_three_answer));
        expandableListDetail.put(context.getResources().getString(R.string.help_feature_three), features3);

        List<String> features4 = new ArrayList<>();
        features4.add(context.getResources().getString(R.string.help_feature_four_answer));
        expandableListDetail.put(context.getResources().getString(R.string.help_feature_four), features4);

        List<String> features5 = new ArrayList<>();
        features5.add(context.getResources().getString(R.string.help_feature_five_answer));
        expandableListDetail.put(context.getResources().getString(R.string.help_feature_five), features5);

        List<String> privacy = new ArrayList<>();
        privacy.add(context.getResources().getString(R.string.help_privacy_answer));
        expandableListDetail.put(context.getResources().getString(R.string.help_privacy), privacy);

        List<String> permissions = new ArrayList<>();
        permissions.add(context.getResources().getString(R.string.help_permission_answer));
        expandableListDetail.put(context.getResources().getString(R.string.help_permission), permissions);

        return expandableListDetail;
    }
}
