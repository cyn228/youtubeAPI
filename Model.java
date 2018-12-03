import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Iterator;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Model  extends Observable{
    /** The observers that are watching this model for changes. */
    private List<Observer> observers;

    private static long MAX_NUM_RESULTS = 25;

    private String apiKey;
    private YouTube youtube;

    public ArrayList <SearchResult> videoList = new ArrayList<SearchResult>(25);
    public  Map <SearchResult,Integer> rating_db = new HashMap<SearchResult, Integer>() ;
    // good idea to dis-allow default constructor usage because an API key is strictly necessary
    public int rating_num = 0;
    public int save_size = 0;
    private Model() {}
    /**
     * Create a new model.
     */
    public Model(String apiKey) {
        this.observers = new ArrayList<Observer>();
        setChanged();
        this.apiKey = apiKey;

        try {
            YouTube.Builder builder = new YouTube.Builder(
                    new NetHttpTransport(), new JacksonFactory(),new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest httpRequest) throws IOException {}
            });
            builder.setApplicationName("a4-youtube");
            youtube = builder.build();

        } catch (Exception e) {
            e.printStackTrace();
            this.throwModelInitializationException();
        }
    }


    /**
     * Add an observer to be notified when this model changes.
     */
    public void addObserver(Observer observer) {
        this.observers.add(observer);
    }

    /**
     * Remove an observer from this model.
     */
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    /**
     * Notify all observers that the model has changed.
     */
    public void notifyObservers() {
        for (Observer observer: this.observers) {
            observer.update(this);
        }
    }
    public void searchVideos(String query) {

        if (youtube == null) {
            this.throwModelInitializationException();
        }

        try {
            videoList.clear();
            YouTube.Search.List search = youtube.search().list("id,snippet");

            search.setKey(this.apiKey);
            search.setQ(query);
            search.setType("video");
            search.setSafeSearch("strict");
            search.setMaxResults(this.MAX_NUM_RESULTS);

            SearchListResponse searchResponse = search.execute();
            List<SearchResult> resultsList = searchResponse.getItems();

            if (resultsList != null) {
                Iterator<SearchResult> resultsIterator = resultsList.iterator();
                int count = 0;
                while (resultsIterator.hasNext()&& count < 25) {
                    count++;
                    //System.out.println("--------------------------------------------------");

                    SearchResult searchResult = resultsIterator.next();
                    if(rating_num == 0){
                        videoList.add(searchResult);
                    }else{

                        if(rating_db.get(searchResult) != null){
                            int rnum = rating_db.get(searchResult);
                            if(rating_num <= rnum){
                                videoList.add(searchResult);
                            }
                        }
                    }
                    ResourceId rId              = searchResult.getId();
                    SearchResultSnippet snippet = searchResult.getSnippet();

                    //System.out.println("Title: "+snippet.getTitle());
                    //System.out.println("video ID: "+rId.getVideoId());
                    //System.out.println("Raw data:"+searchResult.toString());

                    //System.out.println("--------------------------------------------------");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            this.throwModelInitializationException();
        }

    }

    // helper calls for error reporting and debugging
    private void throwModelInitializationException() {

        throw new RuntimeException("Couldn't initialize the YouTube object. You may want to check your API Key.");

    }

    public void save() {
                save_size = videoList.size();
                JFrame f = new JFrame();
                JFileChooser file = new JFileChooser();
                file.setFileFilter(new FileNameExtensionFilter("txt files","txt"));
                file.setDialogTitle("save the current file");
                if(file.showSaveDialog(f) == JFileChooser.APPROVE_OPTION){
                    File savedF = file.getSelectedFile();
                    try{
                       FileWriter fw = new FileWriter(savedF);
                        fw.write(Integer.toString(videoList.size())+'\n');
                        for(int i =0; i < videoList.size();i++){

                            SearchResult sr = videoList.get(i);
                            String title = sr.getSnippet().getTitle();
                            String id = sr.getId().getVideoId();
                            DateTime time = sr.getSnippet().getPublishedAt();
                            Thumbnail thumbnail = sr.getSnippet().getThumbnails().getDefault();
                            String url = thumbnail.getUrl();
                            String s;
                            if(rating_db.get(sr)!=null) {
                                s = Integer.toString(rating_db.get(sr));
                            }else{
                                s = Integer.toString(0);
                            }
                            fw.write(title+'\n');
                            fw.write(id+'\n');
                            fw.write(getTime(time)+'\n');
                            fw.write(url+'\n');
                            fw.write(s+'\n');

                        }
                        fw.flush();
                        fw.close();


                        System.out.println("save a file");
                    }catch(IOException ioe){
                    }

            }

    }

    public String getTime(DateTime t){
        String s;
        long currentTime = (new Date()).getTime();
        long publishTime = t.getValue();
        long p = TimeUnit.MILLISECONDS.toMinutes(currentTime - publishTime);
        int period = (int)p;
        if(period < 60){
            s = period + " minutes ago";
        }else if(period > 60 && period < 1440){
            s = (period / 60) + " hours ago";
        }else if(period > 1440 && period < 43200){
            s = (period/1440) + " days ago";
        }else if(period > 43200 && period < 525600){
            s = (period/43200) + " months ago";
        }else{
            s = (period/525600) + " years ago";
        }
        return s;
    }

}
