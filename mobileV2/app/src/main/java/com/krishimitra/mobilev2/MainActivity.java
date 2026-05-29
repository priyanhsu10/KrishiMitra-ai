package com.krishimitra.mobilev2;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.krishimitra.mobilev2.data.SessionManager;
import com.krishimitra.mobilev2.databinding.ActivityMainBinding;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(binding.drawerLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();

        // Top-level destinations where the burger icon should be shown instead of back arrow
        Set<Integer> topLevelDestinations = new HashSet<>();
        topLevelDestinations.add(R.id.HomeFragment);
        topLevelDestinations.add(R.id.CropTrackingFragment);
        topLevelDestinations.add(R.id.LanguageFragment);
        topLevelDestinations.add(R.id.LoginFragment);

        appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations)
                .setOpenableLayout(drawer)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Update header data
        View headerView = navigationView.getHeaderView(0);
        TextView tvHeaderMobile = headerView.findViewById(R.id.tv_header_mobile);
        String name = sessionManager.getFarmerName();
        if (name != null) {
            tvHeaderMobile.setText(name);
        }

        // Handle logout
        navigationView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener(item -> {
            sessionManager.clear();
            navController.navigate(R.id.LanguageFragment);
            drawer.closeDrawers();
            return true;
        });

        // Hide toolbar/drawer on specific fragments if needed
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.LanguageFragment || destination.getId() == R.id.LoginFragment || destination.getId() == R.id.OtpFragment) {
                binding.toolbar.setVisibility(View.GONE);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            } else {
                binding.toolbar.setVisibility(View.VISIBLE);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                // Try updating FCM token whenever we are in a main screen
                updateFcmToken();
            }
        });
    }

    private void updateFcmToken() {
        String farmerId = sessionManager.getFarmerId();
        if (farmerId == null) return;

        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    android.util.Log.w("MainActivity", "Fetching FCM registration token failed", task.getException());
                    return;
                }

                String token = task.getResult();
                java.util.Map<String, String> body = new java.util.HashMap<>();
                body.put("fcm_token", token);

                com.krishimitra.mobilev2.data.RetrofitClient.INSTANCE.getFarmerApi().updateFcmToken(farmerId, body)
                    .enqueue(new retrofit2.Callback<Void>() {
                        @Override
                        public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                            if (response.isSuccessful()) {
                                android.util.Log.d("MainActivity", "FCM token updated successfully");
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                            android.util.Log.e("MainActivity", "FCM token update failed", t);
                        }
                    });
            });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
