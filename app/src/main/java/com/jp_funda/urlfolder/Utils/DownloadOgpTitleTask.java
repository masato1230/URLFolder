package com.jp_funda.urlfolder.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;

public class DownloadOgpTitleTask extends AsyncTask<String, Void, String> {
    TextView ogpTitleView;

    public DownloadOgpTitleTask(TextView ogpTitleView) {
        this.ogpTitleView = ogpTitleView;
    }

    @Override
    protected String doInBackground(String... urls) {
        Elements elements = null;
        String ogpTitle = "";
        try {
            elements = OGP.getOgpTitle(urls[0]);
            for (Element element : elements) {
                System.out.println(element.attr("content"));
                ogpTitle = element.attr("content");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return ogpTitle;
    }

    protected void onPostExecute(String result) {
        ogpTitleView.setText(result);
    }
}
