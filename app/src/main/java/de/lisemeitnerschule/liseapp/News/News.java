package de.lisemeitnerschule.liseapp.News;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;

import org.apache.http.client.HttpResponseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.lisemeitnerschule.liseapp.R;
import de.lisemeitnerschule.liseapp.Utils.GermanTextUtil;

/**
 * Created by Pascal on 4.2.15.
 */
public class News {
    public String    title;

    public Date date;
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyy");

    public Drawable  picture;
        public String    pictureName;
        public int       pictureWidth;
        public int       pictureHeigth;
            private static       Drawable NullImage         = null; //TODO nullImage
              private static final String NullPictureName   = "NullImage";
              private static final int    NullPictureWidth  = 00;
              private static final int    NullPictureHeigth = 00;

    public Spannable teaser;
        public String    HTMLteaser;

    @Override
    public String toString() {
        return title;
    }

    //reading News



        //parsing

            public static final void parse(NewsAdapter adapter,Element... unparsedNews){
                ParseTask tmp = new ParseTask(adapter);
                tmp.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, unparsedNews);
            }

            private static class ParseTask extends AsyncTask<Element, Integer, List<News>> {
                    private static String html_backspace = new String(new byte[]{-62,-96});

                    NewsAdapter adapter;
                    public ParseTask( NewsAdapter adapter) {
                        this.adapter = adapter;
                    }

                    protected void downloadImage(String pictureUrl) throws Exception {
                        InputStream input = null;
                        OutputStream output = null;
                        HttpURLConnection connection = null;
                        try {
                            URL url = new URL("http://www.lise-meitner-schule.de/"+pictureUrl);
                            connection = (HttpURLConnection) url.openConnection();
                            connection.connect();

                            // expect HTTP 200 OK, so we don't mistakenly save error report
                            // instead of the file
                            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                                throw new HttpResponseException(connection.getResponseCode(), "Failed to retrieve image");
                            }

                            // download the file
                            input = connection.getInputStream();
                            String[] URLParts = url.toString().split("/");
                            output = adapter.Context.openFileOutput(URLParts[URLParts.length - 1], Context.MODE_PRIVATE);

                            byte data[] = new byte[4096];
                            int count;
                            while ((count = input.read(data)) != -1) {
                                // allow canceling with back button
                                if (isCancelled()) {
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
                    @Override
                    protected List<News> doInBackground(Element... parameters) {
                        List<News> res = new ArrayList<News>();
                        for(Element element:parameters) {
                            if (isCancelled())return res;


                            try {

                                News current = new News();


                                 // parse the Header
                                 try {
                                       Element header = element.child(0).child(0);
                                       current.date = dateFormat.parse(header.child(0).ownText());
                                       current.title = header.child(1).ownText();
                                    } catch (Exception e) {
                                        throw new Exception("failed to parse Header! aborting", e);
                                    }
                                try{
                                    News tmp;
                                    if((tmp = getCached(adapter.Context,current.title+".news"))!=null){
                                        current = tmp;
                                        continue;
                                    }
                                }catch (Exception e){

                                }

                                    //parse The Image
                                    try {
                                        String pictureUrl = element.child(1).child(0).child(0).attr("src");
                                        downloadImage(pictureUrl);
                                        String[] URLParts     = pictureUrl.split("/");
                                        current.pictureName   = URLParts[URLParts.length-1];
                                        current.pictureWidth  = Integer.valueOf(element.child(1).child(0).child(0).attr("width"));
                                        current.pictureHeigth =  Integer.valueOf(element.child(1).child(0).child(0).attr("height"));
                                        current.picture = Drawable.createFromStream(adapter.Context.openFileInput(current.pictureName),current.pictureName);
                                    } catch (Exception e) {
                                        if(e.getMessage().equals("Failed To Download Image: Cancelled"))continue;
                                        new Exception("failed to load Image for: " + current.title,e).printStackTrace();

                                    }


                                    //parse Teaser
                                    try {
                                        Element teaser = element.child(2);
                                        for (Element img : teaser.getElementsByTag("img")) {
                                            if(current.pictureName != null) {
                                                try {
                                                    String pictureUrl = img.attr("src");
                                                    downloadImage(pictureUrl);
                                                    String[] URLParts = pictureUrl.split("/");
                                                    current.pictureName = URLParts[URLParts.length - 1];
                                                    current.pictureWidth = Integer.valueOf(img.attr("width"));
                                                    current.pictureHeigth = Integer.valueOf(img.attr("height"));
                                                    current.picture = Drawable.createFromStream(adapter.Context.openFileInput(current.pictureName), current.pictureName);

                                                } catch (Exception e) {
                                                    if (e.getMessage().equals("Failed To Download Image: Cancelled"))
                                                        continue;
                                                    new Exception("failed to load Image for: " + current.title, e).printStackTrace();

                                                }
                                            }
                                            img.remove();
                                        }
                                        for(Element backspace: teaser.getElementsMatchingOwnText(html_backspace)){
                                            backspace.remove();
                                        }
                                        String html = teaser.html();
                                        html = html.replace(GermanTextUtil.encodeHTML("(Für weitere Informationen auf den Titel klicken.)"),"");
                                        html = html.replace(GermanTextUtil.encodeHTML("(Für weitere Informationen auf den Titel klicken)" ),"");
                                        html = html.replace(GermanTextUtil.encodeHTML(" Für weitere Informationen auf den Titel klicken"  ),"");

                                        current.teaser = (Spannable)Html.fromHtml(html);
                                        current.HTMLteaser = html;
                                        cache(adapter.Context,current);
                                    }catch (Exception e){
                                        new Exception("Failed to parse Teaser: "+element.nodeName(),e).printStackTrace();
                                    }
                                cache(adapter.Context,current);
                                    res.add(current);
                            }catch (Exception e){
                                new Exception("Failed to parse News: "+element,e).printStackTrace();
                            }
                        }
                        return res;
                    }

                    @Override
                    protected void onPostExecute(List<News> news) {
                        for(News current:news){
                            adapter.NewsList.add(current);
                            adapter.notifyItemInserted(adapter.getItemCount()-1);
                        }
                    }
                }



        //Caching

            protected static void cache(Context context,News news){
                FileOutputStream outputStream = null;
                try {
                    outputStream = context.openFileOutput(news.title.trim()+".news",Context.MODE_PRIVATE);
                    outputStream.write((news.title                  + ":").getBytes());
                    outputStream.write((dateFormat.format(news.date)+ ":").getBytes());
                    outputStream.write((news.pictureName            + ":").getBytes());
                    outputStream.write((news.pictureWidth + ":").getBytes());
                    outputStream.write((news.pictureHeigth          + ":").getBytes());
                    outputStream.write(news.HTMLteaser                    .getBytes());
                    outputStream.close();
                } catch (IOException  e) {
                    new Exception("Caching failed for"+news,e);
                    try{
                        outputStream.close();
                        context.deleteFile(news.title.trim()+".news");
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }

                }
            }

            protected static News    getCached(Context context,String fileName){
                    FileInputStream inputStream;
                    try {
                        inputStream = context.openFileInput(fileName);
                        byte[] buff = new byte[inputStream.available()];
                        inputStream.read(buff,0,inputStream.available());
                        News res = new News();
                        String[] values = new String(buff).split(":");
                        res.title       = values[0];
                        res.pictureName = values[1];
                        res.teaser      = (Spannable)Html.fromHtml(values[2]);
                        inputStream.close();
                        return res;
                    } catch (IOException  e) {
                        e.printStackTrace();
                    }
                    return null;
            }
            protected static News clearCache(Context context,String fileName){
                FileInputStream inputStream;
                try {
                    inputStream = context.openFileInput(fileName);
                    byte[] buff = new byte[inputStream.available()];
                    inputStream.read(buff,0,inputStream.available());
                    News res = new News();
                    String[] values = new String(buff).split(":");
                    res.title       = values[0];
                    res.pictureName = values[1];
                    res.teaser      = (Spannable)Html.fromHtml(values[2]);
                    inputStream.close();
                    return res;
                } catch (IOException  e) {
                    e.printStackTrace();
                }
                return null;
            }


    protected static boolean isCached (Context context,String fileName,boolean validate){
                    FileInputStream inputStream;

                    try {

                        inputStream = context.openFileInput(fileName);

                        if(inputStream.available()<2){
                            inputStream.close();
                            return false;
                        }


                        if(validate) {
                            byte[] buff = new byte[inputStream.available()];
                            inputStream.read(buff,0,inputStream.available());
                            String[] values = new String(buff).split(":");
                            if(values.length < 6)return false;
                        }


                        inputStream.close();

                        return true;
                    } catch (IOException  e) {
                        e.printStackTrace();
                    }
                    return false;
                }



        //Tesing

            public static void parsingTest(NewsAdapter adapter){
                parse(adapter,getUnparesDummyNews(adapter.Context));
            }

                public static Element getUnparesDummyNews(Context context){
                    InputStream inputStream;
                    try {
                        inputStream = context.getResources().openRawResource(R.raw.dummy);
                        Document doc = Jsoup.parse(inputStream,"UTF-8","lisemeitnerschule.de");
                        return doc.getElementsByClass("news-list-item").first();
                    } catch (IOException  e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                public static News getDummyNews(Context context)
                {
                    News res = new News();
                    res.title        = "Title";
                    res.pictureName  = "android_vs_ios";
                    res.picture      = context.getResources().getDrawable(R.drawable.android_vs_ios);
                    res.teaser       = new SpannableString("teaser");
                    return res;

                }
    }
