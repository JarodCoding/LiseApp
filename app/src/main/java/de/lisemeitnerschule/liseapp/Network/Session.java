package de.lisemeitnerschule.liseapp.Network;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import de.lisemeitnerschule.liseapp.News.News;
import de.lisemeitnerschule.liseapp.News.NewsAdapter;


public class Session {
	Connection connection ;
    Document   currentPage;
	
	public Session(String username,String password) {
		try {
			 connection = Jsoup.connect("http://lise-meitner-schule.de/start");
			 Document currentPage = connection.get();
			 Element loginData = currentPage.getElementsByClass("felogin-hidden").first();
            currentPage = connection
					  .data("user", username)
					  .data("pass", password)
					  .data("btn-submit:", "Anmelden")
					  .data("logintype",loginData.getElementsByAttributeValue("name", "logintype").first().attr("value"))
					  .data("pid",loginData.getElementsByAttributeValue("name", "pid").first().attr("value"))
					  .data("redirect_url",loginData.getElementsByAttributeValue("name", "redirect_url").first().attr("value"))
					  .data("tx_felogin_pi1[noredirect]",loginData.getElementsByAttributeValue("name", "tx_felogin_pi1[noredirect]").first().attr("value"))
					  .data("n",loginData.getElementsByAttributeValue("name", "n").first().attr("value"))
					  .data("e",loginData.getElementsByAttributeValue("name", "e").first().attr("value"))
					  .userAgent("Mozilla")
					  .post();
			  System.err.println(currentPage.title());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
    public Document navigateTo(String site) throws IOException {
        return connection.get();
    }
    public void parseNewsFromCurrentPage(NewsAdapter adapter){
        News.parse(adapter,(Element[])currentPage.getElementsByClass("news-list-item").toArray());
    }
}
