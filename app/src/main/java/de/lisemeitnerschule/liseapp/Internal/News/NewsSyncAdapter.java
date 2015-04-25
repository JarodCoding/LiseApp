package de.lisemeitnerschule.liseapp.Internal.News;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.lisemeitnerschule.liseapp.Internal.InternalContract;
import de.lisemeitnerschule.liseapp.LiseApp;
import de.lisemeitnerschule.liseapp.Network.Security.Authenticator;
import de.lisemeitnerschule.liseapp.Network.Session;
import de.lisemeitnerschule.liseapp.R;

/**
 * Created by Pascal on 23.3.15.
 */
public class NewsSyncAdapter extends AbstractThreadedSyncAdapter {
    public NewsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        File file = new File(context.getCacheDir(),"/images/");
        if(!file.exists())file.mkdir();

    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        sync(getContext(), account, provider);

    }
    public static int sync(Context context,Account account, ContentProviderClient provider){
        int res = 0;
        try {
            String authToken = AccountManager.get(context).blockingGetAuthToken(account,"",true);
            Session session = Session.instance(account.name,authToken);

            JSONArray news = requestNews(session, getLastUpdated(context));
            if(news != null) {
                JSONObject current;
                for (int i = 0; i < news.length(); i++) {
                    current = news.getJSONObject(i);
                    if (parse(current, context)) {
                        ContentValues values = new ContentValues();
                        values.put(InternalContract.News._ID, current.getInt("uid"));
                        values.put(InternalContract.News.Title, current.getString("title"));
                        values.put(InternalContract.News.Teaser, current.getString("teaser"));
                        values.put(InternalContract.News.Text, current.getString("bodytext"));
                        values.put(InternalContract.News.Endtime, current.getLong("endtime"));
                        values.put(InternalContract.News.Author, current.getString("author"));
                        values.put(InternalContract.News.Image, current.getString("image"));
                        values.put(InternalContract.News.Date, current.getLong("datetime"));
                        values.put(InternalContract.News.Categorys, current.getString("categories"));
                        values.put(InternalContract.News.User, account.name);

                        provider.insert(InternalContract.News.CONTENT_URI, values);
                        res++;
                        notify(values, context);

                    } else {
                        res++;
                        //delete images
                        Cursor obsoleteNews = provider.query(Uri.withAppendedPath(InternalContract.News.CONTENT_URI, current.getString("uid")), new String[]{InternalContract.News.Image},null,null,null);
                        final File image = new File(context.getCacheDir(), "/images/"+obsoleteNews.getString(0));
                        image.delete();
                        provider.delete(Uri.withAppendedPath(InternalContract.News.CONTENT_URI, current.getString("uid")), null, null);
                    }
                }
                updateLastUpdated(context);
            }


            //delete obsolete News (older than endtime)

            //delete images
            Cursor obsoleteNews = provider.query(InternalContract.News.CONTENT_URI, new String[]{InternalContract.News.Image},InternalContract.News.Endtime + " <= ?", new String[]{"" + System.currentTimeMillis() / 1000},InternalContract.News.SORT_ORDER_DEFAULT);
            for(int i = 0;i < obsoleteNews.getCount();i++){
                obsoleteNews.moveToPosition(i);
                final File image = new File(context.getCacheDir(), "/images/"+obsoleteNews.getString(0));
                image.delete();
            }

            //remove database entries
            provider.delete(InternalContract.News.CONTENT_URI, InternalContract.News.Endtime + " <= ?", new String[]{"" + System.currentTimeMillis() / 1000});

        } catch (Exception e){
            e.printStackTrace();
        }
        return res;
        //TODO: DETECT CHANGES VS NEW;
    }
    //Notifications
    public static void notify(final ContentValues values, final Context context){
        NotificationCompat.Builder build = new NotificationCompat.Builder(context);
            //info
                //image
                    Bitmap   icon = null;
                    try {
                        try {
                            icon = new AsyncTask<Void, Void, Bitmap>() {
                                @Override
                                protected Bitmap doInBackground(Void... params) {
                                    try {
                                        return Picasso.with(context).load(new File(context.getCacheDir(),"/images/"+values.getAsString(InternalContract.News.Image)))
                                                .resize(200, 200)
                                                .placeholder(R.drawable.ic_drawer)
                                                .error(R.drawable.ic_drawer)
                                                .get();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }
                            }.execute().get(1500, TimeUnit.MILLISECONDS);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(icon != null) {
                        build.setLargeIcon(icon);
                    }else{
                        build.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_drawer));
                    }
                //title
                    build.setContentTitle(values.getAsString(InternalContract.News.Title));
                //Teaser
                    build.setContentText(values.getAsString(InternalContract.News.Teaser));

            //Behavior
                //Click
                Intent resultIntent = new Intent(context, News_Detail_Page.class);

                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                context,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                build.setContentIntent(resultPendingIntent);
        NotificationManager notficaitonManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notficaitonManager.notify(1, build.build());
    }
    //retriving

        //Last Updated
            public static long getLastUpdated(Context context){
                SharedPreferences settings = context.getSharedPreferences("LiseNetworkData", 0);
                return settings.getLong("NewsLastUpdate",0);
            }

            public static void updateLastUpdated(Context context){
                SharedPreferences settings = context.getSharedPreferences("LiseNetworkData", 0);
                settings.edit().putLong("NewsLastUpdate",System.currentTimeMillis()).commit();
            }


        //Network
            public static JSONArray requestNews(Session session,long timestamp){
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
            protected static String downloadImage(String pictureUrl,Context context) throws Exception {
                URL url = new URL(LiseApp.URL+pictureUrl);

                InputStream input = null;
                FileOutputStream output = null;

                try {
                    String fileName = pictureUrl.substring(pictureUrl.lastIndexOf("/"));

                    input = url.openConnection().getInputStream();
                    File file = new File(context.getCacheDir(),"/images/"+fileName);
                    if(file.exists())file.delete();
                    file.createNewFile();
                    output =  new FileOutputStream(file);
                    int read;
                    byte[] data = new byte[1024];
                    while ((read = input.read(data)) != -1)
                        output.write(data, 0, read);

                    return fileName;

                } finally {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                }

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
    //manual Sync

        public static void syncManually(Context context){
            Account[] accounts = AccountManager.get(context).getAccountsByType(Authenticator.accountType);
            //spread the out on multiple tasks for better core utilisation
            for(Account current:accounts){
                new ManualSyncAsyncTask(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, current);
            }
        }

        public static void syncManually(Context context,Account account){
            new ManualSyncAsyncTask(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,account);
        }
        public static void syncManually(Context context,SwipeRefreshLayout updater){
            Account[] accounts = AccountManager.get(context).getAccountsByType(Authenticator.accountType);
            //spread the out on multiple tasks for better core utilisation
            for(Account current:accounts){
                new ManualSyncAsyncTask(context,updater).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, current);
            }
        }

        public static void syncManually(Context context,Account account,SwipeRefreshLayout updater){
            new ManualSyncAsyncTask(context,updater).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,account);
        }

    }
class ManualSyncAsyncTask extends AsyncTask<Account,Integer,Integer>{
    private Context context;
    private SwipeRefreshLayout updater;
    public ManualSyncAsyncTask(Context context){
        this.context = context;
    }
    public ManualSyncAsyncTask(Context context,SwipeRefreshLayout updater){
        this.context = context;
        this.updater = updater;
    }


    @Override
    protected Integer doInBackground(Account... params) {
        int res = 0;
        for(Account current:params){
            res += NewsSyncAdapter.sync(context, current, context.getContentResolver().acquireContentProviderClient(InternalContract.CONTENT_URI));
        }
        return res;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if(updater!=null)updater.setRefreshing(false);
        super.onPostExecute(integer);
    }
}