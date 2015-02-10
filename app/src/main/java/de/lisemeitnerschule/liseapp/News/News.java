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
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

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
            private static       Drawable NullImage         = null; //TODO nullImage
              private static final String NullPictureName   = "NullImage";

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
                            URL url = new URL("http://lise-meitner-schule.de/uploads/tx_news/"+pictureUrl);
                            connection = (HttpURLConnection) url.openConnection();
                            connection.connect();

                            // expect HTTP 200 OK, so we don't mistakenly save error report
                            // instead of the file
                            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                                throw new HttpResponseException(connection.getResponseCode(), "Failed to retrieve image: "+url);
                            }

                            // download the file
                            input = connection.getInputStream();
                            String[] URLParts = url.toString().split("/");
                            output = new FileOutputStream(new File(adapter.Context.getCacheDir(),URLParts[URLParts.length - 1]));

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
                                    if((tmp = getCached(adapter.Context,current.title))!=null){
                                        current = tmp;
                                        res.add(tmp);
                                        continue;
                                    }
                                }catch (Exception e){

                                }

                                    //parse The Image
                                    try {
                                        String temporaryImageUrl    = element.child(1).child(0).child(0).attr("src");
                                        String temporaryImageName   = temporaryImageUrl.substring(temporaryImageUrl.lastIndexOf("/"));
                                        if(!temporaryImageUrl.startsWith("/upload/")) {
                                            String extension = temporaryImageName.substring(temporaryImageName.lastIndexOf(".")+1);
                                            temporaryImageName = temporaryImageName.substring(0,temporaryImageName.length()-extension.length());
                                            String[] underLineSaperated = temporaryImageName.split("_");
                                            StringBuilder pictureNameBuilder = new StringBuilder();
                                            int i = 1;
                                            for(String tmp:underLineSaperated){

                                                if(i == underLineSaperated.length){
                                                    pictureNameBuilder.setCharAt(pictureNameBuilder.length()-1,'.');
                                                    continue;
                                                }
                                                if(i == 1){
                                                    i++;
                                                    continue;
                                                }
                                                pictureNameBuilder.append(tmp+"_");
                                                i++;
                                            }
                                            pictureNameBuilder.append(extension);
                                            current.pictureName = pictureNameBuilder.toString();
                                        }else{
                                            current.pictureName = temporaryImageName;
                                        }
                                        downloadImage(current.pictureName);
                                        current.picture = Drawable.createFromStream(new FileInputStream(new File(adapter.Context.getCacheDir(),current.pictureName)),current.pictureName);
                                    } catch (Exception e) {
                                        if(e.getMessage().equals("Failed To Download Image: Cancelled"))continue;
                                        new Exception("failed to load Image for: " + current.title,e).printStackTrace();

                                    }


                                    //parse Teaser
                                    try {
                                        Element teaser = element.child(2);
                                        for (Element img : teaser.getElementsByTag("img")) {
                                            if(current.pictureName == null) {
                                                try {
                                                    String pictureUrl = img.attr("src");
                                                    downloadImage(pictureUrl);
                                                    String[] URLParts = pictureUrl.split("/");
                                                    current.pictureName = URLParts[URLParts.length - 1];
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
                File file = null;
                try {
                    file = new File(context.getCacheDir(),news.title.trim()+".news");
                    outputStream = new FileOutputStream(file);
                    outputStream.write((news.title                  + "|").getBytes());
                    outputStream.write((dateFormat.format(news.date)+ "|").getBytes());
                    outputStream.write((news.pictureName            + "|").getBytes());
                    outputStream.write(news.HTMLteaser                    .getBytes());
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
                        res.date       = dateFormat.parse(values[1]);
                        res.pictureName = values[2];
                        res.picture = Drawable.createFromStream(new FileInputStream(new File(context.getCacheDir(), res.pictureName)), res.pictureName);
                        res.teaser      = (Spannable)Html.fromHtml(values[3]);
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
                String[] values = new String(buff).split(";");
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






        //Testing

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
