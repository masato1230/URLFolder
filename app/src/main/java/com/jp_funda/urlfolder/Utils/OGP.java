package com.jp_funda.urlfolder.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class OGP {
    public static Elements getOgp(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        return document.select("meta[property~=og:*]");
    }

    public static Elements getOgpImage(String url) throws IOException {
        Document document = null;
        try {
            document = Jsoup.connect(url).get();
        } catch (IllegalArgumentException illegalArgumentException) {
            return null;
        }
        return document.getElementsByAttributeValue("property", "og:image");
    }
}
