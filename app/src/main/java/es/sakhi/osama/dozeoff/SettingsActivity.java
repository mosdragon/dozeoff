package es.sakhi.osama.dozeoff;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;


public class SettingsActivity extends AppCompatActivity {

    private Button saveConfigsButton;
    private Spinner choicesSpinner;
    private CheckBox rerouteBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getActionBar().setTitle("Configure Preferences");

        saveConfigsButton = (Button) findViewById(R.id.saveConfigs);
        choicesSpinner = (Spinner) findViewById(R.id.choices);

//        TODO: Figure out how to create an adapter for this spinner to let it show "gas station" and "coffee shop"
//        choices.setAdapter(n);
        rerouteBox = (CheckBox) findViewById(R.id.reroute);

        saveConfigsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
                finish();
            }
        });
    }

    private void save() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("configured", true);
        boolean reroute = rerouteBox.isChecked();
        editor.putBoolean("reroute", reroute);
        if (reroute) {
            String choice = "gas station";
            editor.putString("choice", choice);
        }

        editor.commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
