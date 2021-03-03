package com.example.petoibittlebluetoothcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TitleActivity extends AppCompatActivity {

    private TextView madeBy;
    private Button pairedDevicesButton;
    private Button guideButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_title);

        madeBy = findViewById(R.id.idMadeBy);
        pairedDevicesButton = findViewById(R.id.idPairButton);
        guideButton = findViewById(R.id.idGuideButton);

        // Create footer Github profile hyperlink
        String content = "<a href=https://github.com/EnriqueMoran> Made by @EnriqueMoran";
        Spannable s = (Spannable) Html.fromHtml(content);
        for (URLSpan u: s.getSpans(0, s.length(), URLSpan.class)) {
            s.setSpan(new UnderlineSpan() {
                public void updateDrawState(TextPaint tp) {
                    tp.setUnderlineText(false);
                }
            }, s.getSpanStart(u), s.getSpanEnd(u), 0);
        }
        madeBy.setText(s);
        madeBy.setMovementMethod(LinkMovementMethod.getInstance());

        pairedDevicesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(TitleActivity.this, PairedDevices.class);
                startActivityForResult(intent, 1);
            }
        });

        guideButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(TitleActivity.this, InstructionsActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }
}