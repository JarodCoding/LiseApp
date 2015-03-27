package de.lisemeitnerschule.liseapp.Internal.News;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.lisemeitnerschule.liseapp.Internal.InternalContract;
import de.lisemeitnerschule.liseapp.Network.Session;
import de.lisemeitnerschule.liseapp.Utilitys;

/**
 * Created by Pascal on 23.3.15.
 */
public class NewsSyncAdapter extends AbstractThreadedSyncAdapter {
    private final AccountManager accountmanager;
    public NewsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        accountmanager = AccountManager.get(context);
        if(LastUpdatedFile == null) {
           LastUpdatedFile = new File(getContext().getCacheDir(),"LastUpdated");
           if(!LastUpdatedFile.exists())
               try {
                   LastUpdatedFile.createNewFile();
               } catch (IOException e1) {
                   //Should never happen
                   e1.printStackTrace();
               }

        }
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        try {
            String authToken = accountmanager.blockingGetAuthToken(account,"",true);
            Session session = Session.instance(account.name,authToken);

            JSONArray news = requestNews(session, getLastUpdated());
            JSONObject current;
            for(int i = 0;i < news.length();i++){
                current = news.getJSONObject(i);
                if(parse(current,getContext())){
                    ContentValues values = new ContentValues();
                        values.put(InternalContract.News._ID        ,current.getInt    ("uid"      ));
                        values.put(InternalContract.News.Title      ,current.getString ("title"    ));
                        values.put(InternalContract.News.Teaser     ,current.getString ("teaser"   ));
                        values.put(InternalContract.News.Text       ,current.getString ("bodytext" ));
                        values.put(InternalContract.News.Endtime    ,current.getLong   ("endtime"  ));
                        values.put(InternalContract.News.Author     ,current.getString ("author"   ));
                        values.put(InternalContract.News.Image      ,current.getString ("image"    ));
                        values.put(InternalContract.News.Date       ,current.getLong   ("datetime" ));
                        values.put(InternalContract.News.Categorys  ,current.getString ("categorys"));
                        values.put(InternalContract.News.User       ,account.name                   );

                    provider.insert(InternalContract.News.CONTENT_URI,values);
                }else{
                    provider.delete(Uri.withAppendedPath(InternalContract.News.CONTENT_URI,current.getString("uid")),"1=1",new String[]{});
                }
            }
            provider.delete(InternalContract.News.CONTENT_URI,"InternalContract.News.Endtime <= ?",new String[]{""+System.currentTimeMillis()/1000});
        } catch (Exception e){

        }
        //TODO: DETECT CHANGES VS NEW;
        //TODO: PUSH NOTIFICATIONS   ;

    }
    private static Long lastUpdated    = 0L;
    private static File LastUpdatedFile;

    //retriving


        //Last Updated
            public long getLastUpdated(){
                if(lastUpdated > 0L)return lastUpdated;
                try {
                    FileInputStream  inputStream = new FileInputStream(LastUpdatedFile);
                    lastUpdated = new DataInputStream(inputStream).readLong();
                    inputStream.close();
                } catch (FileNotFoundException e1) {
                    //should never happen
                    e1.printStackTrace();
                } catch (IOException e1) {
                    //should never happen

                    e1.printStackTrace();
                }
                return 0L;
            }

            public void updateLastUpdated(){
                lastUpdated = Utilitys.generateTimeStamp();
                try {
                    FileOutputStream outputStream = new FileOutputStream(LastUpdatedFile);
                    new DataOutputStream(outputStream).writeLong(lastUpdated);
                    outputStream.close();
                } catch (FileNotFoundException e1) {
                    //should never happen
                    e1.printStackTrace();
                } catch (IOException e1) {
                    //should never happen

                    e1.printStackTrace();
                }
            }


        //Network
            public JSONArray requestNews(Session session,long timestamp){
                JSONArray arr = null;
                try {
                    JSONObject object = session.apiRequest("news",new String[]{String.valueOf(timestamp)});
                    arr = object.toJSONArray(object.names());

                } catch (Exception e) {
                    new Exception("Failed to retrieve News: ",e).printStackTrace();
                }
                return arr;
            }


        //parsing
            private static String html_backspace = new String(new byte[]{-62,-96});

            protected static boolean parse(JSONObject rawNews,Context context) throws Exception {

                        try {
                            //if only the uid is send then this is meant to remove the news
                            if(rawNews.length()==1){
                                return false;
                            }
                            if(rawNews.getString("image")!=null)
                                rawNews.put("image", downloadImage(rawNews.getString("image"), context));

                            //teaser
                            parseTeaser(rawNews.getString("teaser"));

                            //text
                            parseText(rawNews,context);

                        }catch(Exception e){
                            throw new Exception("Failed to parse News from: "+rawNews,e);
                        }
                return true;

            }
                static class ImageCachingTarget implements Target{
                    private final Context context;
                    private final String filename;
                    public ImageCachingTarget(Context context,String filename){
                        this.context  = context;
                        this.filename = filename;
                    }
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        try {
                            File file = new File(context.getCacheDir(),"images/"+filename);
                            if(file.exists())file.delete();
                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 6, ostream);
                            ostream.close();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                }
            protected static String downloadImage(String pictureUrl,Context context) throws Exception {
                Picasso picassoInstance = Picasso.with(context);
                RequestCreator requestCreator = picassoInstance.load(pictureUrl);
                String fileName = "images/"+pictureUrl.substring(pictureUrl.lastIndexOf("/"));
                requestCreator.into(new ImageCachingTarget(context,fileName));
                return fileName;

            }
            protected static String parseTeaser(String source){
                source = source.replace("(Für weitere Informationen auf den Titel klicken.)", "");
                source = source.replace("(Für weitere Informationen auf den Titel klicken)", "");
                source = source.replace(" Für weitere Informationen auf den Titel klicken.", "");
                source = source.replace(" Für weitere Informationen auf den Titel klicken", "");
                source = source.replace("(Auf die Überschrift klicken.)", "");
                source = source.replace("(Auf die Überschrift klicken)", "");
                source = source.replace("Auf die Überschrift klicken.", "");
                source = source.replace("Auf die Überschrift klicken", "");
                return source;
            }
            protected static void parseText(JSONObject rawNews,Context context) throws Exception {
                try {
                    Document body = Jsoup.parse(rawNews.getString("bodytext"));
                    for (Element img : body.getElementsByTag("img")) {
                        if (!rawNews.has("image")) {
                            String temporaryImageUrl = img.attr("src");
                            rawNews.put("image",downloadImage(temporaryImageUrl,context));
                        }
                        img.remove();
                    }
                    for (Element backspace : body.getElementsMatchingOwnText(html_backspace)) {
                        backspace.remove();
                    }

                    rawNews.put("bodytext", body.html());
                } catch (Exception e) {
                    throw new Exception("Failed to parse Body from News: " + rawNews, e);
                }
            }
    }
