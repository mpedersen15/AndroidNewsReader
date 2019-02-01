package com.example.matt3865.newsreader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ListView articleListView;

    ArrayList<String> articleTitles;
    ArrayList<String> articleUrls;

    ArrayAdapter<String> arrayAdapter;

    SQLiteDatabase myDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        articleListView = findViewById(R.id.articleListView);

        articleTitles = new ArrayList<>();
        articleUrls = new ArrayList<>();

        // TODO: read data from local DB, fetch data if not present (or outdated?)
        myDB = this.openOrCreateDatabase("articles", MODE_PRIVATE, null);
        myDB.execSQL("CREATE TABLE IF NOT EXISTS articles (title VARCHAR, url VARCHAR)");



//        articleTitles.add("Google");
//
//        articleUrls.add("https://google.com");

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, articleTitles);

        articleListView.setAdapter(arrayAdapter);

        articleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("Item clicked", articleTitles.get(position) + " - " + articleUrls.get(position));
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);

                intent.putExtra("url", articleUrls.get(position));

                startActivity(intent);
            }
        });

        readDatabase();

        if (articleTitles.size() == 0 || articleUrls.size() == 0) {
            fetchStories();
        } else {
            Log.i("Loaded Stories", "Loaded " + articleTitles.size() + " stories from DB!");
        }
    }

    private void readDatabase() {
        Cursor c = myDB.rawQuery("SELECT * FROM articles", null);

        int titleIndex = c.getColumnIndex("title");

        int urlIndex = c.getColumnIndex("url");

        Log.i("cursor count", "" + c.getCount());
        while(c.moveToNext()) {
            articleTitles.add(c.getString(titleIndex));
            articleUrls.add(c.getString(urlIndex));
//            c.moveToNext();
        }
    }

    private void fetchStories () {
        DownloadTask task = new DownloadTask();
        JSONObject result;

        articleTitles.clear();
        articleUrls.clear();

        try {

            result = task.execute("https://newsapi.org/v2/top-headlines?sources=hacker-news&apiKey=defb3074e3fd43deb26364fcfe09a212").get();

            Log.i("News Results", result.getJSONArray("articles").toString());

            JSONArray articles = result.getJSONArray("articles");


            for (int i = 0; i < articles.length(); i++) {
                JSONObject article = articles.getJSONObject(i);
                articleTitles.add(article.getString("title"));
                articleUrls.add(article.getString("url"));
            }


            arrayAdapter.notifyDataSetChanged();

            // TODO: update local DB with this data
            saveDataToDB();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void saveDataToDB() {
        myDB.execSQL("DELETE FROM articles");

        String valuesString = "";

        for (int i = 0 ; i < articleTitles.size(); i++) {
            valuesString += "(\"" + articleTitles.get(i) + "\",\"" + articleUrls.get(i) + "\")";

            if (i != articleTitles.size() - 1) {
                valuesString += ",";
            }
        }

        Log.i("Values string", valuesString);

        myDB.execSQL("INSERT INTO articles (title, url) VALUES " + valuesString);
    }

    public class DownloadTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {

                url = new URL(urls[0]);

                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return new JSONObject(result);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }
    }
}
