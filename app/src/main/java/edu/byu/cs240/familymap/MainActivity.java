package edu.byu.cs240.familymap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.Person;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements LoginFragment.Listener, MapsFragment.Listener {
    static Menu menu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("YEET");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fManager = this.getSupportFragmentManager();
        Fragment fragment = fManager.findFragmentById(R.id.fragmentFrameLayout);
        if(fragment == null) {
            fragment = createLoginFragment();
            fManager.beginTransaction().add(R.id.fragmentFrameLayout, fragment).commit();
        } else {
            if(fragment instanceof LoginFragment) {
                ((LoginFragment)fragment).registerListener(this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(DataCache.getInstance().started) {
            openMap();
        }
    }

    private Fragment createLoginFragment() {
        LoginFragment loginFragment = new LoginFragment();
        loginFragment.registerListener(this);
        return loginFragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        System.out.println("we are in creating menu options thing");
        this.menu = menu;
        if(DataCache.getInstance().started) {
            inflateOptionsMenu(this.menu);
        }
        return true;
    }

    public boolean inflateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        System.out.println(inflater.toString());
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void openMap() {
        System.out.println("we want to get to the map frag now");
        inflateOptionsMenu(menu);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment fragment = new MapsFragment();
        ((MapsFragment)fragment).registerListener(this);
        fragmentManager.beginTransaction().replace(R.id.fragmentFrameLayout, fragment).commit();
        DataCache.getInstance().started = true;
    }


    @Override
    public void openPersonActivity() {
        System.out.println("we want to get to the person activity now");
        startActivity(new Intent(this, PersonActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("SOMETHING WAS PRESSED AND SO AM I");
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.search:
                System.out.println("open search please");
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            case R.id.settings:
                System.out.println("open settings please");
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}