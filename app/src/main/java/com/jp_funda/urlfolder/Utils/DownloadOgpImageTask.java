package com.jp_funda.urlfolder.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;

public class DownloadOgpImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bitmapImageView;

    public DownloadOgpImageTask(ImageView bitmapImageView) {
        this.bitmapImageView = bitmapImageView;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        Elements elements = null;
        Bitmap bitmap = null;
        try {
            elements = OGP.getOgpImage(urls[0]);
            for (Element element : elements) {
                System.out.println(element.attr("content"));
                String imageUrl = element.attr("content");
                InputStream inputStream = new java.net.URL(imageUrl).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    protected void onPostExecute(Bitmap result) {
        bitmapImageView.setImageBitmap(result);
    }
}
