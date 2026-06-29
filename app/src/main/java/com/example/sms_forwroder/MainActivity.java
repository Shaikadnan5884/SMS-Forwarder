package com.example.sms_forwroder;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final String PREFS_NAME = "SmsForwarderPrefs";

    private EditText etForwardNumber, etSenderId, etBotToken, etChatId;
    private SwitchMaterial switchForwardAll, switchTelegram, switchSms;
    private View tilSenderId, layoutTelegramConfig, layoutSmsConfig;
    private TextView statusText;
    private ImageView statusIcon;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        loadSettings();

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            updateStatus(true);
        }
    }

    private void initViews() {
        etForwardNumber = findViewById(R.id.etForwardNumber);
        etSenderId = findViewById(R.id.etSenderId);
        etBotToken = findViewById(R.id.etBotToken);
        etChatId = findViewById(R.id.etChatId);
        
        switchForwardAll = findViewById(R.id.switchForwardAll);
        switchTelegram = findViewById(R.id.switchTelegram);
        switchSms = findViewById(R.id.switchSms);
        
        tilSenderId = findViewById(R.id.tilSenderId);
        layoutTelegramConfig = findViewById(R.id.layoutTelegramConfig);
        layoutSmsConfig = findViewById(R.id.layoutSmsConfig);
        
        statusText = findViewById(R.id.statusText);
        statusIcon = findViewById(R.id.statusIcon);

        switchForwardAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tilSenderId.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });

        switchTelegram.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutTelegramConfig.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        switchSms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutSmsConfig.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        findViewById(R.id.btnSaveSettings).setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        String number = prefs.getString("forward_number", "");
        String sender = prefs.getString("sender_id", "CP-GCETCP-S");
        boolean forwardAll = prefs.getBoolean("forward_all", false);
        
        boolean telegramEnabled = prefs.getBoolean("telegram_enabled", false);
        String botToken = prefs.getString("bot_token", "");
        String chatId = prefs.getString("chat_id", "");
        
        boolean smsEnabled = prefs.getBoolean("sms_enabled", false);

        etForwardNumber.setText(number);
        etSenderId.setText(sender);
        switchForwardAll.setChecked(forwardAll);
        tilSenderId.setVisibility(forwardAll ? View.GONE : View.VISIBLE);
        
        switchTelegram.setChecked(telegramEnabled);
        layoutTelegramConfig.setVisibility(telegramEnabled ? View.VISIBLE : View.GONE);
        etBotToken.setText(botToken);
        etChatId.setText(chatId);
        
        switchSms.setChecked(smsEnabled);
        layoutSmsConfig.setVisibility(smsEnabled ? View.VISIBLE : View.GONE);
    }

    private void saveSettings() {
        String number = etForwardNumber.getText().toString().trim();
        String sender = etSenderId.getText().toString().trim();
        boolean forwardAll = switchForwardAll.isChecked();
        
        boolean telegramEnabled = switchTelegram.isChecked();
        String botToken = etBotToken.getText().toString().trim();
        String chatId = etChatId.getText().toString().trim();
        
        boolean smsEnabled = switchSms.isChecked();

        if (telegramEnabled && (botToken.isEmpty() || chatId.isEmpty())) {
            Toast.makeText(this, "Please provide Telegram Bot Token and Chat ID", Toast.LENGTH_SHORT).show();
            return;
        }

        if (smsEnabled && number.isEmpty()) {
            Toast.makeText(this, "Please provide a forward phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        prefs.edit()
                .putString("forward_number", number)
                .putString("sender_id", sender)
                .putBoolean("forward_all", forwardAll)
                .putBoolean("telegram_enabled", telegramEnabled)
                .putString("bot_token", botToken)
                .putString("chat_id", chatId)
                .putBoolean("sms_enabled", smsEnabled)
                .apply();

        Toast.makeText(this, "All settings applied!", Toast.LENGTH_SHORT).show();
    }

    private void updateStatus(boolean granted) {
        if (granted) {
            statusText.setText("Active & Monitoring");
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            statusIcon.setImageResource(android.R.drawable.presence_online);
        } else {
            statusText.setText("Permission Required");
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            statusIcon.setImageResource(android.R.drawable.presence_busy);
        }
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            updateStatus(allGranted);
            if (allGranted) {
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions Denied. The app will not work.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
