package ca.doophie.spritesheet_testapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import ca.doophie.spritesheets.Test;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Test().go();
    }
}
