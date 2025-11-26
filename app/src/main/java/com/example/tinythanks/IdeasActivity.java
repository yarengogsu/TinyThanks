package com.example.tinythanks;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Random;

public class IdeasActivity extends AppCompatActivity {

    private TextView tvPrompt;


    private String[] prompts = {
            "What small detail made you smile today? 😊",
            "When was the last time you laughed out loud? 😂",
            "What was the most beautiful sound you heard today? 🎶",
            "Who are you feeling lucky to have in your life right now? ❤️",
            "What was the most delicious thing you ate today? 🍕",
            "What act of kindness did you do for yourself today? 🌟",
            "Recall a happy memory from last week.",
            "Did you see a color in nature that caught your eye today? 🌿",
            "Did you learn something new today? 💡"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ideas);

        tvPrompt = findViewById(R.id.tvPromptQuestion);
        MaterialButton btnRefresh = findViewById(R.id.btnRefresh);
        MaterialButton btnAnswer = findViewById(R.id.btnAnswer);

        // İlk açılışta rastgele bir soru getir
        getRandomPrompt();

        // Yenile Butonu
        btnRefresh.setOnClickListener(v -> getRandomPrompt());

        // Cevapla Butonu -> Ekleme Sayfasına Git (Soruyu da taşıyoruz)
        btnAnswer.setOnClickListener(v -> {
            Intent intent = new Intent(IdeasActivity.this, AddGratitudeActivity.class);
            intent.putExtra("PROMPT_TEXT", tvPrompt.getText().toString());
            startActivity(intent);
        });

        // --- ORTADAKİ + BUTONU (FAB) ---
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(IdeasActivity.this, AddGratitudeActivity.class));
        });

        // --- ALT MENÜ AYARLARI ---
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        // Bu sayfa açılınca 'Ideas' (Ampul) ikonunu seçili yap
        bottomNav.setSelectedItemId(R.id.nav_ideas);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Ana Sayfaya Git
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                finish(); // Bu sayfayı kapat (Yığılmayı önler)
                return true;
            }
            else if (id == R.id.nav_history) {
                // Geçmiş Sayfasına Git
                startActivity(new Intent(getApplicationContext(), JourneyActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            else if (id == R.id.nav_ideas) {
                // Zaten buradayız
                return true;
            }
            else if (id == R.id.nav_profile) {
                // Profil Sayfasına Git
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void getRandomPrompt() {
        Random random = new Random();
        int index = random.nextInt(prompts.length);
        tvPrompt.setText(prompts[index]);
    }
}