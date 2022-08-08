package edu.byu.cs240.familymap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Map;

public class EventActivity extends AppCompatActivity implements MapsFragment.Listener {

    @Override
    public void openPersonActivity() {
        startActivity(new Intent(this, PersonActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentManager fManager = this.getSupportFragmentManager();
        Fragment fragment = fManager.findFragmentById(R.id.fragmentFrameLayout);

        if(fragment == null) {
            fragment = createMapsFragment();
            fManager.beginTransaction().add(R.id.fragmentFrameLayout, fragment).commit();
        } else {
            if(fragment instanceof MapsFragment) {
                ((MapsFragment)fragment).registerListener(this);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        return true;
    }

    private Fragment createMapsFragment() {
        MapsFragment mapsFragment = new MapsFragment();
        ((MapsFragment)mapsFragment).registerListener(this);
        return mapsFragment;
    }
}