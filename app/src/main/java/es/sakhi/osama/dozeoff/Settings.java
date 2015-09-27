package es.sakhi.osama.dozeoff;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Settings extends AppCompatActivity {
    public static final String WHAT_SHOP = "com.example.dozeoff.SHOP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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

    public void sendGasIntent(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        //intent.putExtra(WHAT_SHOP, "gas station");
        SharedPreferences prefs = this.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("shopMode", "gas station");
        editor.commit();
        System.out.println("****** " + prefs.getString("shopMode", "DID NOT WORK"));
        startActivity(intent);
    }

    public void sendTruckStopIntent(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        //intent.putExtra(WHAT_SHOP, "truckstop");
        SharedPreferences prefs = this.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("shopMode", "truck stop");
        editor.commit();
//        prefs.edit().commit();
        startActivity(intent);
    }

    public void sendCoffeeIntent(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        //intent.putExtra(WHAT_SHOP, "coffee");
        SharedPreferences prefs = this.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("shopMode", "coffee");
        editor.commit();
//        prefs.edit().commit();
        startActivity(intent);
    }
}
