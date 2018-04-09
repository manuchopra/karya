package de.fhdw.ergoholics.brainphaser.activities.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Node;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.fhdw.ergoholics.brainphaser.ProjectKarya;
import de.fhdw.ergoholics.brainphaser.BrainPhaserComponent;
import de.fhdw.ergoholics.brainphaser.R;
import de.fhdw.ergoholics.brainphaser.activities.BrainPhaserActivity;
import de.fhdw.ergoholics.brainphaser.activities.createuser.CreateUserActivity;
import de.fhdw.ergoholics.brainphaser.database.AnswerDataSource;
import de.fhdw.ergoholics.brainphaser.database.CategoryDataSource;
import de.fhdw.ergoholics.brainphaser.database.ChallengeDataSource;
import de.fhdw.ergoholics.brainphaser.logic.UserManager;
import de.fhdw.ergoholics.brainphaser.logic.fileimport.FileImport;
import de.fhdw.ergoholics.brainphaser.logic.fileimport.bpc.BPCObjects;
import de.fhdw.ergoholics.brainphaser.logic.fileimport.bpc.BPCRead;
import de.fhdw.ergoholics.brainphaser.logic.fileimport.bpc.BPCWrite;
import de.fhdw.ergoholics.brainphaser.logic.fileimport.exceptions.ElementAmountException;
import de.fhdw.ergoholics.brainphaser.model.Answer;
import de.fhdw.ergoholics.brainphaser.model.Category;
import de.fhdw.ergoholics.brainphaser.model.Challenge;
import android.Manifest;


/**
 *
 * The activity redirects to user creation on first launch. On later launches it loads last selected
 * user and redirects to the main activity.
 */
public class ProxyActivity extends BrainPhaserActivity {
    @Inject
    UserManager mUserManager;
    //Attributes
    @Inject
    CategoryDataSource mCategoryDataSource;
    @Inject
    ChallengeDataSource mChallengeDataSource;
    @Inject
    AnswerDataSource mAnswerDataSource;
    static final Integer WRITE_EXST = 0x3;
    static final Integer READ_EXST = 0x4;

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

    ProjectKarya application = (ProjectKarya) getApplication();

    public void onCreate(Bundle savedInstanceState) {
        askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE,READ_EXST);
        askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXST);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_proxy);

        if (mUserManager.logInLastUser()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_SHOW_LOGGEDIN_SNACKBAR, true);

            startActivity(intent);
            finish();
        } else {
            // Import challenges if the database does not include any
            if (mChallengeDataSource.getAll().size() == 0) {

                try {
                    //should happen in a Async task
                    AsyncImport myImport = new AsyncImport();
                    myImport.execute();
//                    FileImport.importBPC(is, application);

                } catch (Exception e) {
                    throw new RuntimeException("An unexpected error has occured when trying to add " +
                            "example challenges!");
                }
            }

            startActivity(new Intent(Intent.ACTION_INSERT, Uri.EMPTY, getApplicationContext(), CreateUserActivity.class));
            finish();
        }
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(ProxyActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(ProxyActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(ProxyActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(ProxyActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }


    public class AsyncImport extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... strings) {
            InputStream is = getResources().openRawResource(R.raw.challenges);
            try {
//                FileImport.importBPC(is,application);
                //Get root element
                Node categoriesNode = BPCRead.getCategoriesNode(is);

                //Get the root's child nodes
                Node childCategories = categoriesNode.getFirstChild();

                //Create lists for saving the categories, challenges and answers
                List<Category> categoryList = new ArrayList<>();
                List<Challenge> challengeList = new ArrayList<>();
                List<Answer> answerList = new ArrayList<>();
//                Log.d("status","so far so good1");

                //All categories, challenges and answers are read first
                //Popov
                //So if there is any syntax error in the file, nothing will be imported
                long i = 0;
                long nextChallengeId = 0;
                while (childCategories != null) {
                    if (childCategories.getNodeType() == Node.ELEMENT_NODE) {
                        nextChallengeId = BPCObjects.readCategory(childCategories, i, nextChallengeId,
                                categoryList, challengeList, answerList);
                        i++;
                    }
//                    Log.d("status","so far so good2");


                    childCategories = childCategories.getNextSibling();
                }

//                Log.d("status","out of the while loop baby");

                if (i == 0) throw new ElementAmountException("<category>", ">0", "0");
//                Log.d("status","writing got me buggy");

                //No syntax errors were found, so the read information is being written
                //FIX THIS WRITING PROCESS
//                BPCWrite writer = new BPCWrite(application);
//                writer.writeAll(categoryList, challengeList, answerList);

                for (Category category : categoryList) {
//                    Log.d("status","check2"); //passes this one

                    long oldCategoryId = category.getId();
                    category.setId(null);
                    long categoryId = mCategoryDataSource.create(category);

                    for (int j = 0; j < challengeList.size(); j++) {
                        Challenge challenge = challengeList.get(j);
//                        Log.d("status","check1");

                        if (challenge != null && challenge.getCategoryId() == oldCategoryId) {
                            challenge.setCategoryId(categoryId);

                            //writeChallenge method
                            long oldChallengeId = challenge.getId();
                            challenge.setId(null);
                            long challengeId = mChallengeDataSource.create(challenge);

                            for (Answer answer : answerList) {
                                if (answer.getChallengeId() == oldChallengeId) {
                                    answer.setChallengeId(challengeId);
                                    //writeAnswer method
                                    answer.setId(null);
                                    mAnswerDataSource.create(answer);
                                    //end of write Answer method

                                    answer.setChallengeId(-1);
                                }
                            }
//                            Log.d("status","escaped 2");

                            //end of writeChallenge Method

                            challengeList.set(j, null);
                        }
                    }
//                    Log.d("status","escaped 1");
                }

//                Log.d("status","weird");

            } catch (Exception e) {
                throw new RuntimeException("An unexpected error has occured when trying to add " +
                        "example challenges!");
            }
            return "all jobs imported!";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // display a progress dialog for good user experience
            progressDialog = new ProgressDialog(ProxyActivity.this);
            progressDialog.setMessage("Loading all the available jobs!");
//            progressDialog.setCancelable(false);
//            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {

//            Log.d("data", "did it work?");
            // dismiss the progress dialog after receiving data from API
            progressDialog.dismiss();
        }
        }

}
