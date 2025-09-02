package com.example.uppgift1_oliver_kalthoff;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements android.hardware.SensorEventListener {

    // UI
    private ImageView imgMushroom;
    private Spinner spinnerSensitivity;
    private Button btnReset;
    private ToggleButton toggleLight;
    private TextView tvValues;

    // Sensorer
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor light;

    // Logik
    private float threshold = Float.MAX_VALUE; // Threshold för accelerometer sätts av spinner val
    private float currentRotation = 0f; // Antal grader bilden är roterad
    private float lastLux = 0f; // Senast uppmätta värde av lux för att visa i UI
    private long lastToastMs = 0L; // Timer mellan toasts, ca 1 sec
    private boolean lightEnabled = true; // Ljussensorn startar som aktiv


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Koppla UI
        spinnerSensitivity = findViewById(R.id.spinnerSensitivity);
        btnReset = findViewById(R.id.btnReset);
        imgMushroom = findViewById(R.id.mushroom);
        tvValues = findViewById(R.id.tvValues);
        toggleLight = findViewById(R.id.toggleLight);

        // Starttext i sensorrutan
        tvValues.setText("ax=0 ay=0 az=0 lux=0");

        // Toggleknapp för att slå av/på ljussensorn
        toggleLight.setChecked(true);
        lightEnabled = true;
        toggleLight.setOnCheckedChangeListener((btn, isChecked) -> lightEnabled = isChecked);

        // Spinner - hämtar stringarray från resources string.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.sensitivity_levels,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSensitivity.setAdapter(adapter);

        // Startar med sensitivity på index 0 = default = sensor avstängd
        spinnerSensitivity.setSelection(0);

        // När användare väljer sensitivity sätts threshold
        spinnerSensitivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1: // Low
                        threshold = 5.0f;
                        break;
                    case 2: // Medium
                        threshold = 8.0f;
                        break;
                    case 3: // High
                        threshold = 12.0f;
                        break;
                    default: // Sensor avstängd tills annat val är gjort
                        threshold = Float.MAX_VALUE;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Reset-knapp för rotation av bilden
        btnReset.setOnClickListener(v -> {
            currentRotation = 0f;
            imgMushroom.setRotation(0f);
        });
    }


    // Registrerar sensorer när appen körs
    @Override
    protected void onResume() {
        super.onResume();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Hämtar referenser till sensorer om tillgängliga
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // Registrerar lyssnare
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        if (light != null) {
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    // Avregisrerar sensor vid pausad körning
    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    // Automatiskt implementerad av SensorEventListener
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // Sensoractions för accelerometer + light senor
    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();

        if (type == Sensor.TYPE_ACCELEROMETER) {
            // ax, ay, az = acceleration i m/s2 i x/y/z-axis
            float ax = event.values[0];
            float ay = event.values[1];
            float az = event.values[2];


            // Rå magnitud (inkusive gravitation
            double mag = Math.sqrt(ax * ax + ay * ay + az * az);

            // Skillnad från jordens gravidation.
            double delta = Math.abs(mag - SensorManager.GRAVITY_EARTH);

            // Visar sensordata i gui
            tvValues.setText(String.format("ax=%.1f ay=%.1f az=%.1f lux=%.1f", ax, ay, az, lastLux));

            // Debounce som gör att toast väntar en sekund innan nästa
            long now = System.currentTimeMillis();
            if (delta > threshold && now - lastToastMs > 1000) {
                lastToastMs = now;

                // Toast till användaren vid skakning/acceleration över threshold
                Toast.makeText(this, "Shake!", Toast.LENGTH_SHORT).show();

                // Roterar bilden 30 grader åt gången (när treshold överskrids).
                // Fortsätter efter ett varv.
                currentRotation = (currentRotation + 30f) % 360f;
                imgMushroom.setRotation(currentRotation);
            }
        }

        if (type == Sensor.TYPE_LIGHT && lightEnabled) {
            // Ljus i lux. Högre = ljusare
            lastLux = event.values[0];

            // If sats som sätter färg på toggle knappen efter värdet på lux.
            if (lastLux < 20f) {
                toggleLight.setBackgroundColor(Color.DKGRAY);
                toggleLight.setTextColor(Color.WHITE);
            } else if (lastLux < 100f) {
                toggleLight.setBackgroundColor(Color.GRAY);
                toggleLight.setTextColor(Color.WHITE);
            } else {
                toggleLight.setBackgroundColor(Color.YELLOW);
                toggleLight.setTextColor(Color.BLACK);
            }
        }
    }
}