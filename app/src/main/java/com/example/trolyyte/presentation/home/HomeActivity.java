package com.example.trolyyte.presentation.home;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.trolyyte.R;
import com.example.trolyyte.presentation.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.trolyyte.presentation.calendar.CalendarFragment;
public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // File này chỉ chứa FragmentContainer và BottomNav (đã hướng dẫn ở trả lời trước)

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Mở tab Home mặc định
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        // BẮT SỰ KIỆN CLICK MENU TẠI ĐÂY
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            }
            else if(id == R.id.nav_reminders) {
                selectedFragment = new CalendarFragment();
            }
            else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}