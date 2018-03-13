package de.fhdw.ergoholics.brainphaser.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import android.Manifest;

import de.fhdw.ergoholics.brainphaser.ProjectKarya;
import de.fhdw.ergoholics.brainphaser.BrainPhaserComponent;
import de.fhdw.ergoholics.brainphaser.activities.main.MainActivity;

/**
 * Created by funkv on 06.03.2016.
 * <p/>
 * Base Activity class to be used by all activitites in the project.
 * Subclasses need to implement injectComponent to use the Depency Injector.
 * See: https://blog.gouline.net/2015/05/04/dagger-2-even-sharper-less-square/
 */
public abstract class BrainPhaserActivity extends AppCompatActivity {
    /**
     * OnCreate injects components
     *
     * @param savedInstanceState Ignored
     */

    static final Integer WRITE_EXST = 0x3;
    static final Integer READ_EXST = 0x4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE,READ_EXST);
//        askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXST);

        injectComponent(((ProjectKarya) getApplication()).getComponent());
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(BrainPhaserActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(BrainPhaserActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(BrainPhaserActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(BrainPhaserActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Called to inject dependencies. Calls component.inject(this) as
     * uniform implementation in all Activities.
     *
     * @param component the component supplied by the Application to resolve dependencies
     */
    protected abstract void injectComponent(BrainPhaserComponent component);
}
