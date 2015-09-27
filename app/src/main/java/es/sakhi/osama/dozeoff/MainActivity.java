package es.sakhi.osama.dozeoff;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class MainActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String shopName = prefs.getString("shopMode", "");
        System.out.println("#### " + shopName);
        if (shopName.equals("")) {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent goToSettings = new Intent(this, Settings.class);
            startActivity(goToSettings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void toggleDrive(View view) {
        Button start = (Button) findViewById(R.id.button);
        if (start.getText() == "START DRIVING") {
            start.setText("STOP DRIVING");
        } else {
            start.setText("START DRIVING");
        }
    }

    public void openGoogleService(View view) {
        //Intent intent = getIntent();
        //String myShop = intent.getStringExtra(Settings.WHAT_SHOP);
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String myShop = prefs.getString("shopMode", "gas station");
        Uri anyAddress = Uri.parse("google.navigation:q=" + Uri.encode(myShop) + "&mode=d");
        Intent mapI = new Intent(Intent.ACTION_VIEW, anyAddress);
        mapI.setPackage("com.google.android.apps.maps");
        if (mapI.resolveActivity(getPackageManager()) != null) {
            startActivity(mapI);
        }

    }
}
