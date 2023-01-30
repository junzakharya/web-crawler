package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

public class Crawler {
    public HashSet<String> urlHash;
    public int MAX_DEPTh =2;

    public  static Connection connection = null;
    public Crawler(){
        // initializing urlHash
        urlHash = new HashSet<>();
        connection = DatabaseConnection.getConnection();
    }
    // to save web pages data into database
    public void getPageTextAndLinks (String url,int depth){
        // if urlHash does not contain url
        if(!urlHash.contains(url)){
            //add url to hashset
            if(urlHash.add(url)){
                System.out.println(url);
            }
            try {
                // connecting web page and get webpage as documents object
                Document document = Jsoup.connect(url).timeout(5000).get();
                //get text inside that document/webpage
                String text = document.text().length()>1000?document.text().substring(0,999): document.text();
                String title = document.title();
                //prepare an insertion command
                PreparedStatement preparedStatement =connection.prepareStatement("insert into pages values(?,?,?)");
                //at first "?"
                preparedStatement.setString(1,title);
                //At second "?"
                preparedStatement.setString(2,url);
                //at third "?"
                preparedStatement.setString(3,text);
                //execute prepared command
                preparedStatement.executeUpdate();
                depth++;
                // limiting depth
                if(depth == 2){
                    return;
                }
                //get all links tags that are present in webpage
                Elements availableLinksOnPage = document.select("a[href]");
                // for every link call this function recursively
                for(Element currentLink :availableLinksOnPage){
                    getPageTextAndLinks(currentLink.attr("abs:href"),depth);

                }
            }
            catch(IOException | SQLException ioException){
                ioException.printStackTrace();
            }

        }
    }
    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        crawler.getPageTextAndLinks("https://www.javatpoint.com",0);
    }
}