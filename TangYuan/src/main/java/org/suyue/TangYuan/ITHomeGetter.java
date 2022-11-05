package org.suyue.TangYuan;

import com.alibaba.fastjson.JSONArray;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import net.mamoe.mirai.message.data.PlainText;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ITHomeGetter {
    public static String ithomeRssUrl = "https://www.ithome.com/rss/";
    public static List<String> ithomeKeywords(){
        List<String> keywords = new ArrayList<String>();
        keywords.add("小米");
        keywords.add("MIUI");
        keywords.add("苹果");
        keywords.add("Apple");
        keywords.add("IOS");
        keywords.add("MacOS");
        keywords.add("腾讯");
        keywords.add("字节");
        keywords.add("阿里");
        keywords.add("百度");
        keywords.add("联通");
        keywords.add("电信");
        keywords.add("移动");
        keywords.add("高通");
        keywords.add("联发科");
        return  keywords;
    };

    public static void main(String[] args) throws IOException, FeedException {
        System.out.println("用于测试it之家的测试用例");

        System.out.println(getSuitItem(ithomeRssUrl,ithomeKeywords()).getTitle());

        Thread t = new ITHomeThread();
        t.start();
    }

    public static List<ITHomeItem> getItems(String url,int count) throws IOException, FeedException {
        SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(url)));
        List<SyndEntry> entries = feed.getEntries().subList(0,count);
        List<ITHomeItem> items = new ArrayList<>();

        for(int i=0;i<entries.size();i++)items.add(new ITHomeItem(entries.get(i)));
        return items;
    }

    public static ITHomeItem getSuitItem(String url,List<String> keywords) throws IOException, FeedException {
        //优先输出标题有关键词的新闻，不然随机抽
        SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(url)));
        List<SyndEntry> entries = feed.getEntries().subList(0,10);
        List<ITHomeItem> items = new ArrayList<>();

        for(int i=0;i<entries.size();i++){
            ITHomeItem item = new ITHomeItem(entries.get(i));
            for(int j=0;j<keywords.size();j++){
                if(item.getTitle().indexOf(keywords.get(j))>=0) return item;
            }
            items.add(item);
        }

        Random r = new Random();
        return items.get(r.nextInt(items.size()));
    }

    public static class ITHomeThread extends Thread {
        @Override
        public void run() {
            while(true){
                if(true){
                    try {
                        String msg = ITHomeGetter.getSuitItem(ITHomeGetter.ithomeRssUrl,ITHomeGetter.ithomeKeywords()).getTitle()+" "+ITHomeGetter.getSuitItem(ITHomeGetter.ithomeRssUrl,ITHomeGetter.ithomeKeywords()).getLink();
                        System.out.println(msg);
                    } catch (IOException | FeedException e) {
                        throw new RuntimeException(e);
                    }
                }
                Random r = new Random();
                try {
                    sleep(1000*60*(r.nextInt(1)+1));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
