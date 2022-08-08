package edu.byu.cs240.familymap;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        DataCache.getInstance().currEvent = null;

        // set all the toggles to be something from the data cache

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Family Map: Settings");

        // LIFE STORY LINES
        Switch s = findViewById(R.id.lifeStoryLines);
        s.setChecked(DataCache.getInstance().isLifeStoryLines);
        s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                DataCache.getInstance().isLifeStoryLines = true;
            } else {
                DataCache.getInstance().isLifeStoryLines = false;
            }
        });
        // FAMILY TREE LINES
        s = findViewById(R.id.familyTreeLines);
        s.setChecked(DataCache.getInstance().isFamilyTreeLines);
        s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                DataCache.getInstance().isFamilyTreeLines = true;
            } else {
                DataCache.getInstance().isFamilyTreeLines = false;
            }
        });
        // SPOUSE LINES
        s = findViewById(R.id.spouseLines);
        s.setChecked(DataCache.getInstance().isSpouseLines);
        s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                DataCache.getInstance().isSpouseLines = true;
            } else {
                DataCache.getInstance().isSpouseLines = false;
            }
        });
        // FATHER'S SIDE
        s = findViewById(R.id.fathersSide);
        s.setChecked(DataCache.getInstance().isFathersSide);
        s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                DataCache.getInstance().isFathersSide = true;
            } else {
                DataCache.getInstance().isFathersSide = false;
            }
        });
        // MOTHER'S SIDE
        s = findViewById(R.id.mothersSide);
        s.setChecked(DataCache.getInstance().isMothersSide);
        s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                DataCache.getInstance().isMothersSide = true;
            } else {
                DataCache.getInstance().isMothersSide = false;
            }
        });
        // MALE EVENTS
        s = findViewById(R.id.maleEvents);
        s.setChecked(DataCache.getInstance().isMaleEvents);
        s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                DataCache.getInstance().isMaleEvents = true;
            } else {
                DataCache.getInstance().isMaleEvents = false;
            }
        });
        // FEMALE EVENTS
        s = findViewById(R.id.femaleEvents);
        s.setChecked(DataCache.getInstance().isFemaleEvents);
        s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                DataCache.getInstance().isFemaleEvents = true;
            } else {
                DataCache.getInstance().isFemaleEvents = false;
            }
        });

        Button button = findViewById(R.id.logout);
        button.setOnClickListener(v -> {
            System.out.println("We need to logout now");
            DataCache.getInstance().started = false;
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            DataCache.getInstance().clearAll();
            startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("we pressed the back button");
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        return true;
    }
}