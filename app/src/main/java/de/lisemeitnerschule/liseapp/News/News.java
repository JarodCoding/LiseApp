package de.lisemeitnerschule.liseapp.News;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;

import com.bluejamesbond.text.style.JustifiedSpan;

import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import de.lisemeitnerschule.liseapp.Constants;
import de.lisemeitnerschule.liseapp.Network.Session;

/**
 * Created by Pascal on 4.2.15.
 */
public class News {
    public static    List<News> NewsList;
    public String    title;

    public Date date;
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyy");

    public String   author;


    public Drawable  picture;
        public String    pictureName;
            private static       Drawable NullImage         = null; //TODO nullImage
              private static final String NullPictureName   = "NullImage";

    public Spannable teaser;
        public String rawTeaser;

    public Spannable text;
        public String rawText;


    @Override
    public String toString() {
        return title;
    }

    //reading News



        //parsing

            public static final void parse(NewsAdapter adapter,JSONArray unparsedNews){
                ParseTask tmp = new ParseTask(adapter);
                tmp.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, unparsedNews);
            }
            public static final void parse(NewsAdapter adapter,JSONObject object) throws JSONException {
                parse(adapter,object.toJSONArray(object.names()));
            }
            public static final void parseAllNews(NewsAdapter adapter) throws Exception {
                GETTask tmp = new GETTask(adapter);
                tmp.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
            }
    protected static class GETTask extends AsyncTask<String, Integer, List<News>> {
                private final NewsAdapter adapter;
                public GETTask( NewsAdapter adapter) {
                    this.adapter = adapter;
                }
                @Override
                protected List<News> doInBackground(String... params) {
                    JSONArray arr = null;
                    try {
                        JSONObject object = Session.instance.apiRequest("news");
                        arr = object.toJSONArray(object.names());


                    } catch (Exception e) {
                        new Exception("Failed to retrieve News: ",e).printStackTrace();
                    }
                    if(arr == null)return null;
                    return ParseTask.parse(arr,this,adapter);
                }
                @Override
                protected void onPostExecute(List<News> news) {
                    if(news == null||news.size()==0)return;
                    NewsList.clear();
                    for(News current:news){
                        NewsList.add(current);
                        adapter.notifyItemInserted(adapter.getItemCount()-1);
                    }
                }
            }
            protected static class ParseTask extends AsyncTask<JSONArray, Integer, List<News>> {
                    private static String html_backspace = new String(new byte[]{-62,-96});

                    private final NewsAdapter adapter;
                    public ParseTask( NewsAdapter adapter) {
                        this.adapter = adapter;
                    }

                    @Override
                    protected List<News> doInBackground(JSONArray... newsList) {
                        return parse(newsList[0],this,adapter);
                    }
                        protected static List<News> parse(JSONArray newsList,AsyncTask task,NewsAdapter adapter){
                            List<News> res = new ArrayList<News>();
                            try {
                                JSONObject currentRawNews = null;
                                News       currentNews    = null;
                                for(int i = 0;i < newsList.length();i++){
                                    try {
                                        currentRawNews = newsList.getJSONObject(i);
                                        currentNews = new News();
                                        //title
                                        currentNews.title = currentRawNews.getString("title");

                                        //date
                                        Calendar date = Calendar.getInstance();
                                        date.setTimeInMillis(currentRawNews.getLong("datetime") * 1000);
                                        currentNews.date = date.getTime();

                                        //author
                                        currentNews.author = currentRawNews.getString("author");

                                        //image
                                        String image = currentRawNews.getString("image");
                                        downloadImage(image,task,adapter);
                                        currentNews.pictureName = image.substring(image.lastIndexOf("/"));
                                        currentNews.picture = Drawable.createFromStream(new FileInputStream(new File(adapter.Context.getCacheDir(), currentNews.pictureName)), currentNews.pictureName);

                                        //teaser
                                        parseTeaser(currentNews, currentRawNews.getString("teaser"));

                                        //text
                                        parseText(currentNews, currentRawNews.getString("bodytext"),task,adapter);


                                        res.add(currentNews);
                                   //   cache(adapter.Context, currentNews);  TODO: caching restriction
                                    }catch(Exception e){
                                        throw new Exception("Failed to parse News "+(currentNews!=null?currentNews.title:"")+"from "+(currentRawNews!=null?currentRawNews:""),e);
                                    }
                                }
                            } catch (Exception e) {
                                new Exception("Failed to receive News: ",e).printStackTrace();
                            }
                            return res;
                        }
                        protected static void downloadImage(String pictureUrl,AsyncTask task,NewsAdapter adapter) throws Exception {
                            InputStream input = null;
                            OutputStream output = null;
                            HttpURLConnection connection = null;
                            try {
                                URL url = new URL(Constants.URL+pictureUrl);
                                connection = (HttpURLConnection) url.openConnection();
                                connection.connect();

                                // expect HTTP 200 OK, so we don't mistakenly save error report
                                // instead of the file
                                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                                    throw new HttpResponseException(connection.getResponseCode(), "Failed to retrieve image: "+url);
                                }

                                // download the file
                                input = connection.getInputStream();
                                output = new FileOutputStream(new File(adapter.Context.getCacheDir(),url.toString().substring(url.toString().lastIndexOf("/"))));

                                byte data[] = new byte[4096];
                                int count;
                                while ((count = input.read(data)) != -1) {
                                    // allow canceling with back button
                                    if (task!=null&&task.isCancelled()) {
                                        input.close();
                                        throw  new Exception("Failed To Download Image: Cancelled");
                                    }
                                    output.write(data, 0, count);
                                }

                            } catch (Exception e) {
                                throw new Exception("Failed to download Image", e);
                            } finally {
                                try {
                                    if (output != null)
                                        output.close();
                                    if (input != null)
                                        input.close();
                                } catch (IOException ignored) {
                                }

                                if (connection != null)
                                    connection.disconnect();
                            }
                        }
                        protected static void parseTeaser(News target,String source){
                            source = source.replace("(Für weitere Informationen auf den Titel klicken.)", "");
                            source = source.replace("(Für weitere Informationen auf den Titel klicken)", "");
                            source = source.replace(" Für weitere Informationen auf den Titel klicken.", "");
                            source = source.replace(" Für weitere Informationen auf den Titel klicken", "");
                            source = source.replace("(Auf die Überschrift klicken.)", "");
                            source = source.replace("(Auf die Überschrift klicken)", "");
                            source = source.replace("Auf die Überschrift klicken.", "");
                            source = source.replace("Auf die Überschrift klicken", "");

                            target.teaser = new SpannableString(source);
                            target.teaser.setSpan(new JustifiedSpan(), 0, target.teaser.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                            target.rawTeaser = source;
                        }
                        protected static void parseText(News target,String source,AsyncTask task,NewsAdapter adapter) throws Exception {
                            try {
                                Document teaser = Jsoup.parse(source);
                                for (Element img : teaser.getElementsByTag("img")) {
                                    if (target.pictureName == null) {
                                        String temporaryImageUrl = img.attr("src");
                                        target.pictureName = temporaryImageUrl.substring(temporaryImageUrl.lastIndexOf("/"));
                                        downloadImage(temporaryImageUrl,task,adapter);
                                        target.picture = Drawable.createFromStream(adapter.Context.openFileInput(target.pictureName), target.pictureName);
                                    }
                                    img.remove();
                                }
                                for (Element backspace : teaser.getElementsMatchingOwnText(html_backspace)) {
                                    backspace.remove();
                                }
                                target.text = (Spannable) Html.fromHtml(teaser.html());
                                target.text.setSpan(new JustifiedSpan(), 0, target.text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                target.rawTeaser = teaser.html();
                            } catch (Exception e) {
                                throw new Exception("Failed to parse Body from News: " + target.title, e);
                            }
                        }


                    @Override
                    protected void onPostExecute(List<News> news) {
                        for(News current:news){
                            NewsList.add(current);
                            adapter.notifyItemInserted(adapter.getItemCount()-1);
                        }
                    }
                }




    //Caching

            protected static void cache(Context context,News news){
                FileOutputStream outputStream = null;
                File file = null;
                try {
                    file = new File(context.getCacheDir(),news.title.trim()+".news");
                    outputStream = new FileOutputStream(file);
                    outputStream.write((news.title+" "                  + "|").getBytes());
                    outputStream.write((dateFormat.format(news.date) + " " + "|").getBytes());
                    outputStream.write((news.author+" "                 + "|").getBytes());
                    outputStream.write((news.pictureName+" "            + "|").getBytes());
                    outputStream.write((news.rawTeaser+" "              + "|").getBytes());
                    outputStream.write((news.rawText + " ").getBytes());

                    outputStream.close();
                } catch (IOException  e) {
                    new Exception("Caching failed for"+news,e);
                    try{
                        outputStream.close();
                        if(file.exists())file.delete();
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }

                }
            }

            protected static News getCached(Context context,String fileName){
                    FileInputStream inputStream;
                    File file;
                    try {
                        file = new File(context.getCacheDir(),fileName+".news");
                        inputStream = new FileInputStream(file);
                        byte[] buff = new byte[inputStream.available()];
                        inputStream.read(buff,0,inputStream.available());
                        News res = new News();
                        String[] values = new String(buff).split(Pattern.quote("|"));
                        res.title       = values[0];
                        res.date        = dateFormat.parse(values[1]);
                        res.author      = values[3];
                        res.pictureName = values[4];
                        res.picture     = Drawable.createFromStream(new FileInputStream(new File(context.getCacheDir(), res.pictureName)), res.pictureName);
                        res.teaser      = new SpannableString(values[5]);
                        res.text        = (Spannable) Html.fromHtml(values[6]);
                        res.teaser.setSpan(new JustifiedSpan(), 0, res.teaser.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                        res.text  .setSpan(new JustifiedSpan(), 0, res.text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                        inputStream.close();
                        return res;
                    } catch (Exception  e) {
                        e.printStackTrace();
                    }
                return null;
            }

            protected static boolean isCached (Context context,String title,boolean validate){
                FileInputStream inputStream;
                File file = null;

                try {
                    file = new File(context.getCacheDir(),title);
                    if(!file.exists())return false;
                    inputStream = new FileInputStream(file);

                    if(inputStream.available()<4){
                        inputStream.close();
                        file.delete();
                        return false;
                    }


                    if(validate) {
                        byte[] buff = new byte[inputStream.available()];
                        inputStream.read(buff,0,inputStream.available());
                        String[] values = new String(buff).split("|");
                        if(values.length < 6)return false;
                    }


                    inputStream.close();

                    return true;
                } catch (IOException  e) {
                    e.printStackTrace();
                }
                return false;
            }

            protected static boolean clearCache(Context context){

                try {
                   deleteDir(context.getCacheDir());
                    return true;
                } catch (Exception  e) {
                    e.printStackTrace();
                    return false;
                }

            }

                private static void deleteDir(File dir) throws Exception{
                    if (dir.isDirectory()) {
                        for (String childName:dir.list()) {
                            try {
                                deleteDir(new File(dir, childName));
                            }catch (Exception e){
                                throw new Exception("Can't delete "+dir+": Error while deleting child "+dir+" "+childName+": ",e);
                            }
                        }
                    }
                    if(!dir.delete())throw new Exception("Can't delete "+dir);

            }

            public static boolean removeFromCache(Context context,String filename) throws Exception{
                File f = new File(context.getCacheDir(),filename);
                return f.delete();
            }
            public static boolean removeFromCache(Context context,News news) throws Exception{
                File f = new File(context.getCacheDir(),news.title.trim()+".news");
                return f.delete();
            }

    }
