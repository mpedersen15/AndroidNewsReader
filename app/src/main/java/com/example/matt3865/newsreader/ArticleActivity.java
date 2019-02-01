package com.example.matt3865.newsreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ArticleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        Intent intent = getIntent();

        String url = intent.getStringExtra("url");

        Log.i("Received URL", url);
        if (!url.matches("")) {


            WebView webView = findViewById(R.id.webView);

            webView.getSettings().setJavaScriptEnabled(true);

            webView.setWebViewClient(new WebViewClient());


            webView.loadUrl(url);

        }
    }
}
