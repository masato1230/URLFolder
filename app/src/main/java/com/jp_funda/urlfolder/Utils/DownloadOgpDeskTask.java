package com.jp_funda.urlfolder.Utils;

import android.os.AsyncTask;
import android.widget.TextView;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class DownloadOgpDeskTask extends AsyncTask<String, Void, String> {
    TextView ogpDeskView;

    public DownloadOgpDeskTask(TextView ogpDeskView) {
        this.ogpDeskView = ogpDeskView;
    }

    @Override
    protected String doInBackground(String... urls) {
        Elements elements = null;
        String ogpTitle = "";
        try {
            elements = OGP.getOgpDescription(urls[0]);
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
        ogpDeskView.setText(result);
    }
}
