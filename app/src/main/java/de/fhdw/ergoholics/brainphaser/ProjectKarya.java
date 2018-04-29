package de.fhdw.ergoholics.brainphaser;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.widget.Toast;

//import com.newtronlabs.easypermissions.EasyPermissions;
//import com.newtronlabs.easypermissions.listener.IPermissionsListener;

import net.danlew.android.joda.JodaTimeAndroid;

import java.lang.reflect.Proxy;
import java.util.Set;

import javax.inject.Singleton;

import dagger.Component;
import de.fhdw.ergoholics.brainphaser.activities.main.ProxyActivity;
import de.fhdw.ergoholics.brainphaser.database.DatabaseModule;

/**
 * Created by funkv on 17.02.2016.
 *
 * Custom Application class for hooking into App creation
 */
public class ProjectKarya extends Application {
    public static String PACKAGE_NAME;
    public static BrainPhaserComponent component;
    static final Integer WRITE_EXST = 0x3;
    static final Integer READ_EXST = 0x4;


    /**
     * Creates the Production app Component
     */
    public BrainPhaserComponent createComponent() {
        return DaggerProjectKarya_ApplicationComponent.builder()
            .appModule(new AppModule(this))
            .databaseModule(new DatabaseModule(getApplicationContext(), "/mnt/sdcard/storage3.db"))
            .build();
    }

    /**
     * Returns the Component for use with Dependency Injection for this
     * Application.
     * @return compoenent to use for DI
     */
    public BrainPhaserComponent getComponent( ) {
        return component;
    }

    /**
     * initializes the DaoManager with a writeable database
     */
    @Override
    public void onCreate() {
        super.onCreate();
//        ProxyActivity newActivity = new ProxyActivity();

        JodaTimeAndroid.init(this);
        if (ContextCompat.checkSelfPermission(ProjectKarya.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(ProjectKarya.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                component = createComponent();
            }
        }
//        } else {
//            newActivity.askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE,READ_EXST);
//            newActivity.askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXST);
//
//            component = createComponent();
//        }

        PACKAGE_NAME = getApplicationContext().getPackageName();
    }


    /**
     * Defines the Component to use in the Production Application.
     * The component is a bridge between Modules and Injects.
     * It creates instances of all the types defined.
     */
    @Singleton
    @Component(modules = {AppModule.class, DatabaseModule.class})
    public interface ApplicationComponent extends BrainPhaserComponent {
    }

    /**
     * Licensed under CC BY-SA (c) 2012 Muhammad Nabeel Arif
     * http://stackoverflow.com/questions/4605527/converting-pixels-to-dp
     *
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into dp
     * @return A float value to represent dp equivalent to px value
     */
    public float convertPixelsToDp(float px){
        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }
}