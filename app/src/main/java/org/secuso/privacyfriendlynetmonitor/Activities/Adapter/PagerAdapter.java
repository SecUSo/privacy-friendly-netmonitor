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

package org.secuso.privacyfriendlynetmonitor.Activities.Adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.secuso.privacyfriendlynetmonitor.fragment.Fragment_day;
import org.secuso.privacyfriendlynetmonitor.fragment.Fragment_month;
import org.secuso.privacyfriendlynetmonitor.fragment.Fragment_week;

/**
 * Created by tobias on 04.01.18.
 * Adapter for fragment pager.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    Bundle data;

    /**
     *
     * @param fm
     * @param NumOfTabs
     * @param appSubName
     */
    public PagerAdapter(FragmentManager fm, int NumOfTabs, String appSubName) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        data = new Bundle();
        data.putString("AppName", appSubName);
    }

    /**
     *
     * @param position
     * @return item
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                Fragment_day tab_day = new Fragment_day();
                tab_day.setArguments(data);
                return tab_day;
            case 1:
                Fragment_week tab_week = new Fragment_week();
                tab_week.setArguments(data);
                return tab_week;
            case 2:
                Fragment_month tab_month = new Fragment_month();
                tab_month.setArguments(data);
                return tab_month;
            default:
                return null;
        }
    }

    /**
     *
     * @return number of tabs
     */
    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
