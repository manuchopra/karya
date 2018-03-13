package de.fhdw.ergoholics.brainphaser.activities.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.nononsenseapps.filepicker.FilePickerActivity;

import javax.inject.Inject;

import de.fhdw.ergoholics.brainphaser.ProjectKarya;
import de.fhdw.ergoholics.brainphaser.BrainPhaserComponent;
import de.fhdw.ergoholics.brainphaser.BuildConfig;
import de.fhdw.ergoholics.brainphaser.R;
import de.fhdw.ergoholics.brainphaser.activities.BrainPhaserActivity;
import de.fhdw.ergoholics.brainphaser.activities.aboutscreen.AboutActivity;
import de.fhdw.ergoholics.brainphaser.activities.fileimport.ImportChallengeActivity;
import de.fhdw.ergoholics.brainphaser.activities.selectuser.UserSelectionActivity;
import de.fhdw.ergoholics.brainphaser.activities.usersettings.SettingsActivity;
import de.fhdw.ergoholics.brainphaser.logic.UserManager;

/**
 *
 * The activity redirects to user creation on first launch, or loads last selected user if it has
 * been launched before.
 */
public class MainActivity extends BrainPhaserActivity {
    public static String EXTRA_SHOW_LOGGEDIN_SNACKBAR = "SHOW_SNACKBAR";
    private final static int CODE_FILEPICKER = 0;
    static final Integer WRITE_EXST = 0x3;
    static final Integer READ_EXST = 0x4;

    @Inject UserManager mUserManager;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void injectComponent(BrainPhaserComponent component) {
        component.inject(this);
    }

    /**
     * This method is called when the activity is created
     *
     * @param savedInstanceState handed over to super constructor
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
//        askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE,READ_EXST);
//        askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXST);
//
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mainactivity);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);

        // Set as Actionbar
        setSupportActionBar(toolbar);

    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * This method is called for creating action menu.
     * Adds the import BPC developer button in debug mode
     *
     * @param menu the menu to be created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        if (BuildConfig.DEBUG) {
            MenuItem item = menu.add("Import BPC");
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * This method handles the action menu item selections.
     *
     * @param item the item which has been selected
     * @return if the item was found in the menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_switch_user:
                startActivity(new Intent(getApplicationContext(), UserSelectionActivity.class));
                return true;
            case R.id.action_about:
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            case 0: // Only in debug mode: File Picker
                Intent i = new Intent(getApplicationContext(), FilePickerActivity.class);
                startActivityForResult(i, CODE_FILEPICKER);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method is called when the activity was called for a result.
     * Used for the filepicker.
     *
     * @param requestCode the request code
     * @param resultCode the result code
     * @param data the intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass the intent containing the file URI on to the Import Challenge method
        if (requestCode == CODE_FILEPICKER) {
            if (data != null) {
                data.setClass(getApplicationContext(), ImportChallengeActivity.class);
                startActivity(data);
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * This method is called hen the activity is started.
     * Shows a snackbar containing the current username when the app is started.
     */
    @Override
    protected void onStart() {
        super.onStart();



        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        // If EXTRA_SHOW_LOGGEDIN_SNACKBAR is passed,
        // show a little snackbar that shows the currently logged in user's name
        if (intent.getBooleanExtra(EXTRA_SHOW_LOGGEDIN_SNACKBAR, false)) {
            View rootView = findViewById(R.id.main_content);
            String text = String.format(getResources().getString(R.string.logged_in_as), mUserManager.getCurrentUser().getName());
            final Snackbar snackbar = Snackbar
                .make(rootView, text, Snackbar.LENGTH_LONG)
                .setAction(R.string.switch_user_short, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), UserSelectionActivity.class);
                        startActivity(intent);
                    }
                });

            // Delay the snackbar a quater second for a smoother experience
            rootView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    snackbar.show();
                }
            }, 250);

            // Update the intent so it doesn't show again on back navigation and thus only when explicitly requested
            intent.putExtra(EXTRA_SHOW_LOGGEDIN_SNACKBAR, false);
        }
    }
}