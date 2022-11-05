package org.suyue.TangYuan;

import com.rometools.rome.feed.synd.SyndEntry;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ITHomeItem {
    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public String getImgurl() {
        return imgurl;
    }

    private String title;
    private String link;
    private String description;
    private String imgurl;

    public ITHomeItem(SyndEntry entry) {
        title = entry.getTitle();
        link = entry.getLink();

        String html = entry.getDescription().getValue();
        description = removeTags(html).substring(0, 30);

        int imgIndex = html.indexOf("<img src=\"");

        if (imgIndex >= 0) {
            String s = html.substring(imgIndex+10);
            //System.out.println(s);
            imgurl = s.substring(0,s.indexOf("\""));
        }
    }

    private static String removeTags(String htmlStr) {
        Document doc = Jsoup.parse(htmlStr);
        String text = doc.text();
        // remove extra white space
        StringBuilder builder = new StringBuilder(text);
        int index = 0;
        while(builder.length()>index){
            char tmp = builder.charAt(index);
            if(Character.isSpaceChar(tmp) || Character.isWhitespace(tmp)){
                builder.setCharAt(index, ' ');
            }
            index++;
        }
        text = builder.toString().replaceAll(" +", " ").trim();
        return text;
    }
}
