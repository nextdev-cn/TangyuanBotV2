package org.suyue.TangYuan;

import com.rometools.rome.io.FeedException;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.SchedulerTaskKt;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.contact.announcement.Announcements;
import net.mamoe.mirai.contact.file.RemoteFiles;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import net.mamoe.mirai.utils.RemoteFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.suyue.bot.ModConfig;
import org.suyue.bot.SuYueBotMod;
import org.suyue.bot.scheduled.ScheduledTaskExecutor;

import java.io.IOException;
import java.util.*;

public class Main implements SuYueBotMod {
    private long groupId;
    private Group group;

    private boolean isIthome = true;
    private boolean isGoodPrice = false;

    private int discussionCount = 0;
    private int rubbishThreshold = 10;
    private int lastMsg = 0;

    private List<Image> imageList;
    private boolean isAddImage = false;
    private long imgUploaderId = 0;

    private List<String> negativeKeywords;

    public Main() {
        imageList = new LinkedList<Image>();
        negativeKeywords = new ArrayList<String>();
        negativeKeywords.add("退群");
        negativeKeywords.add("笨");
        negativeKeywords.add("你看看你");
        negativeKeywords.add("踢");
        negativeKeywords.add("米米");
        negativeKeywords.add("进厂");
        negativeKeywords.add("闭嘴");
    }

    private ModConfig modConfig = new ModConfig("TangYuan");
    @Override
    public void receiveFriendMessage(FriendMessageEvent event) {

    }

    @Override
    public void receiveGroupMessage(GroupMessageEvent event) {
        MessageChain messageChain = event.getMessage();
        String messageStr = messageChain.contentToString();
        String[] split = messageStr.split(" ");

        boolean isAtBot = messageChain.get(0) instanceof At&&((At) messageChain.get(0)).getTarget()==event.getGroup().getBotAsMember().getId();

        if(event.getGroup().getId()==groupId) {
            //自动计算热烈讨论
            if (event.getTime() - lastMsg <= 60 * 1) discussionCount+=2;
            if (event.getTime() - lastMsg <= 60 * 3) discussionCount++;
            else discussionCount = 0;
            lastMsg = event.getTime();

            //热烈讨论水下群
            if (discussionCount >= rubbishThreshold) {
                event.getGroup().sendMessage(randomRubbishMsg());
                if(imageList!=null&&imageList.size()>0) event.getGroup().sendMessage(randomImgMsg());
                else event.getGroup().sendMessage(randomRubbishEmoji());
                discussionCount = 0;

                Random r = new Random();
                rubbishThreshold = r.nextInt(20)+20;
            }


            //被骂自动认错
            if (messageStr.indexOf("汤圆") >= 0) {
                Iterator<String> iterator = negativeKeywords.iterator();
                while (iterator.hasNext()) {
                    if(messageStr.indexOf(iterator.next())>=0){
                        event.getGroup().sendMessage(randomNegativeMsg());
                        if(imageList!=null&&imageList.size()>0) event.getGroup().sendMessage(randomImgMsg());
                        discussionCount = -40;
                        break;
                    }
                }
            }

            //手动IT之家
            if (messageStr.indexOf("来点IT之家") >= 0) {
                String msg = null;
                try {
                    msg = ITHomeGetter.getSuitItem(ITHomeGetter.ithomeRssUrl,ITHomeGetter.ithomeKeywords()).getTitle()+" "+ITHomeGetter.getSuitItem(ITHomeGetter.ithomeRssUrl,ITHomeGetter.ithomeKeywords()).getLink();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (FeedException e) {
                    throw new RuntimeException(e);
                }
                event.getGroup().sendMessage(new PlainText(msg));
            }

            //米米米米米米米
            if (messageStr.indexOf("小米") >= 0&&discussionCount>=0) {
                event.getGroup().sendMessage(randomMiMsg());
            }

            //上传表情包
            if (isAddImage && event.getSender().getId() == imgUploaderId) {
                if (messageChain.get(1) instanceof Image) {
                    imageList.add((Image) messageChain.get(1));
                } else event.getGroup().sendMessage(new At(event.getSender().getId()).plus("不是图片，汤圆不理你"));
                isAddImage = false;
            }

        }

        //管理汤圆机器人有关
        if (split.length == 2 && split[0].equals("汤圆管理")&&event.getPermission().getLevel()>0) {
            if(split[1].toLowerCase().equals("开启汤圆")){
                groupId = event.getGroup().getId();
                event.getGroup().sendMessage(new At(event.getSender().getId()).plus("本群已启用汤圆机器人"));

                group = event.getGroup();
                Thread t = new ITHomeThread();
                t.start();
                //
            }
            else if(split[1].toLowerCase().equals("关闭汤圆")){
                groupId = 0 ;
                group = null;
                event.getGroup().sendMessage(new At(event.getSender().getId()).plus("本群已关闭汤圆机器人"));
            }
            else if(split[1].toLowerCase().equals("关闭it之家")){
                isIthome = false;
                event.getGroup().sendMessage(new At(event.getSender().getId()).plus("已经关闭it之家推送"));
            }
            else if(split[1].toLowerCase().equals("开启it之家")){
                isIthome = false;
                event.getGroup().sendMessage(new At(event.getSender().getId()).plus("已经开启it之家推送"));
            }
            else if(split[1].toLowerCase().equals("关闭好价推送")){
                isGoodPrice = false;
                event.getGroup().sendMessage(new At(event.getSender().getId()).plus("已经关闭好价推送"));
            }
            else if(split[1].toLowerCase().equals("开启好价推送")){
                isGoodPrice = true;
                event.getGroup().sendMessage(new At(event.getSender().getId()).plus("已经开启好价推送"));
            }
            else if(split[1].toLowerCase().equals("新的表情包")){
                event.getGroup().sendMessage(new At(event.getSender().getId()).plus("请现在发送一个图片，这会进入汤圆的表情包中"));
                isAddImage = true;
                imgUploaderId = event.getSender().getId();
            }
            else{
                event.getGroup().sendMessage(new At(event.getSender().getId()).plus("指令有误，汤圆机器人不听你的"));
            }
        }

    }

    @Override
    public void receiveMessage(MessageEvent event) {

    }

    @Override
    public void unloadMod() {

    }

    //随机灌水垃圾消息
    private Message randomRubbishMsg(){
        Random r = new Random();
        switch(r.nextInt(5)){
            case 0:
                return (new PlainText("牛逼"));
            case 1:
                return (new PlainText("强的"));
            case 2:
                return (new PlainText("厉害"));
            case 3:
                return (new PlainText("可以"));
            case 4:
                return (new PlainText("点赞"));

        }
        return null;
    }

    private Message randomRubbishEmoji(){
        Random r = new Random();
        switch(r.nextInt(2)){
            case 0:
                return (new Face(Face.汪汪).plus(new Face(Face.点赞)));
            case 1:
                return (new Face(Face.斜眼笑).plus(new PlainText("💦💦💦")));

        }
        return null;
    }

    //随机认错
    private Message randomNegativeMsg(){
        Random r = new Random();
        switch(r.nextInt(3)){
            case 0:
                return (new PlainText("我错了"));
            case 1:
                return (new PlainText("以后不干了"));
            case 2:
                return (new PlainText("是我不好"));
        }
        return null;
    }

    //随机表情图库
    private Message randomImgMsg(){
        Random r = new Random();
        return imageList.get(r.nextInt(imageList.size()));
    }

    private Message randomMiMsg(){
        Random r = new Random();
        switch(r.nextInt(5)){
            case 0:
                return (new PlainText("865再战100年").plus(new Face(Face.汪汪)));
            case 1:
                return (new PlainText("小米10再战10年").plus(new Face(Face.斜眼笑)));
            case 2:
                return (new PlainText("固件都不维护了").plus(new PlainText("💦💦💦")));
            case 3:
                return (new PlainText("稳定版不如开发版稳定").plus(new PlainText("💦💦💦")));
            case 4:
                return (new PlainText("MIUI越更新续航越差").plus(new Face(Face.爆筋)).plus(new Face(Face.爆筋)).plus(new Face(Face.爆筋)));
        }
        return null;
    }

    class ITHomeThread extends Thread {
        @Override
        public void run() {
            while(true){
                if(group!=null&&isIthome){
                    try {
                        String msg = ITHomeGetter.getSuitItem(ITHomeGetter.ithomeRssUrl,ITHomeGetter.ithomeKeywords()).getTitle()+" "+ITHomeGetter.getSuitItem(ITHomeGetter.ithomeRssUrl,ITHomeGetter.ithomeKeywords()).getLink();
                        group.sendMessage(new PlainText(msg));
                    } catch (IOException | FeedException e) {
                        throw new RuntimeException(e);
                    }
                }
                Random r = new Random();
                try {
                    sleep(1000*60*(r.nextInt(30)+30));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
