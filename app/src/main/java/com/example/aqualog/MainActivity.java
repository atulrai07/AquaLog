package com.example.aqualog;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private int currentItemId = R.id.nav_home; // Default to Home

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Load HomeFragment as default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), false, true);
        }

        // Bottom navigation item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.navigation_history) {
                selectedFragment = new HistoryFragment();
            }

            if (selectedFragment != null) {
                boolean forward = itemId > currentItemId; // Decide direction
                loadFragment(selectedFragment, true, forward);
                currentItemId = itemId;
                return true;
            }
            return false;
        });
    }

    private void loadFragment(@NonNull Fragment fragment, boolean withAnimation, boolean forward) {
        if (withAnimation) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            forward ? R.anim.slide_in_right : R.anim.slide_in_left,
                            forward ? R.anim.slide_out_left : R.anim.slide_out_right
                    )
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
