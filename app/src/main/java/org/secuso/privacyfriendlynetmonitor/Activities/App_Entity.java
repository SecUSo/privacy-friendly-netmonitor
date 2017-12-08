package org.secuso.privacyfriendlynetmonitor.Activities; /**
 * Created by tobias on 08.12.17.
 */
import android.graphics.drawable.Drawable;

public class App_Entity {

    private String name;
    Drawable icon;

    public App_Entity(String name, Drawable icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public Drawable getIcon() {
        return icon;
    }
}
