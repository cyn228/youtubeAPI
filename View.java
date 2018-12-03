
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.awt.*;
import javax.annotation.Resource;
import javax.imageio.ImageIO;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class View extends JFrame {

    private Model model;
    JScrollPane jp;
    JPanel p;

    /**
     * Create a new View.
     */
    public View(Model _model) {
        model = _model;
        p = new JPanel();


        //this = new JFrame("Video Gallery");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        tb.setRollover(true);

        JToggleButton button1 = new JToggleButton("list");
        JToggleButton button2 = new JToggleButton("gird");

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean is_selected = button1.isSelected();
                if (is_selected) {
                    button2.setSelected(false);

                }
                try {
                    changeList();
                    p.repaint();
                } catch (Exception ex) {

                }
            }
        });

        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean is_selected = button2.isSelected();
                if (is_selected) {
                    button1.setSelected(false);
                }
                try {
                    changeGrid();
                    p.repaint();
                } catch (Exception ex) {

                }
            }
        });
        tb.add(button1);
        tb.add(button2);
        tb.addSeparator();
        JTextField searchArea = new JTextField();
        tb.add(searchArea);
        JButton button3 = new JButton("search");
        tb.add(button3);

        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                model.searchVideos(searchArea.getText());
                if (button1.isSelected()) {
                    try {
                        changeList();
                        p.repaint();
                    } catch (Exception ex) {

                    }
                } else if (button2.isSelected()) {
                    try {
                        changeGrid();
                        p.repaint();
                    } catch (Exception ex) {

                    }
                }
            }
        });

        JButton save = new JButton("save");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.save();
            }
        });
        JButton load = new JButton("load");
        load.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(button1.isSelected())load(1);
                else load(2);
                p.repaint();
            }
        });
        tb.addSeparator();
        tb.add(save);
        tb.add(load);

        JPanel rc = ratingControl(null,model.rating_num);

        tb.add(rc);
        add(tb, BorderLayout.BEFORE_FIRST_LINE);

        jp = new JScrollPane(p);
        jp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(jp, BorderLayout.CENTER);
        setSize(800, 600);
        setVisible(true);
    }

    /**
     * Update with data from the model.
     */
    public void update(Object observable) {
        // XXX Fill this in with the logic for updating the view when the model
        // changes.
        System.out.println("Model changed!");
    }

    public void changeList() throws Exception {
        p.removeAll();
        p.revalidate();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        int posY = 0;
        int size = model.videoList.size();
        for (int i = 0; i < size; i++) {
            JPanel video1 = new JPanel();
            video1.setLayout(new BoxLayout(video1, BoxLayout.Y_AXIS));
            video1.setPreferredSize(new Dimension(800, 100));
            Rectangle r = new Rectangle(0, posY, 800, 100);
            video1.setBounds(r);
            String title = model.videoList.get(i).getSnippet().getTitle();
            ResourceId id = model.videoList.get(i).getId();
            DateTime time = model.videoList.get(i).getSnippet().getPublishedAt();
            Thumbnail thumbnail = model.videoList.get(i).getSnippet().getThumbnails().getDefault();
            String url = thumbnail.getUrl();
            Image tbn = ImageIO.read(new URL(url));
            JButton video_pic = new JButton(new ImageIcon(tbn));
            video_pic.setPreferredSize(new Dimension(60, 60));
            video_pic.setLocation(new Point(350, posY));
            posY += 100;
            video_pic.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Desktop d = null;
                    if (Desktop.isDesktopSupported()) {
                        d = Desktop.getDesktop();
                    }
                    if (d != null && d.isSupported(Desktop.Action.BROWSE)) {
                        try {
                            d.browse(new URI("http://youtube.com/watch?v=" + id.getVideoId()));
                        } catch (Exception excep) {

                        }
                    }
                }
            });
            JPanel rc;
            if (model.rating_num == 0) {

                rc = ratingControl(model.videoList.get(i),0);
            } else {
                rc = ratingControl(model.videoList.get(i), model.rating_db.get(model.videoList.get(i)));
                System.out.println(title + " " + model.rating_db.get(model.videoList.get(i)));
            }
            JLabel titleL = new JLabel(title);
            JLabel timeL = new JLabel(model.getTime(time));
            titleL.setPreferredSize(new Dimension(800, 10));
            timeL.setPreferredSize(new Dimension(800, 10));
            video1.add(rc);
            video1.add(video_pic);
            video1.add(titleL);
            video1.add(timeL);
            p.add(video1);

        }

        //jp = new JScrollPane(p);

        //p.repaint();


    }

    public void changeGrid() throws Exception {
        p.removeAll();
        p.revalidate();
        p.setLayout(new GridLayout(5, 5));
        int size = model.videoList.size();
        int posY = 0;
        for (int i = 0; i < size; i++) {
            JPanel video1 = new JPanel();
            video1.setLayout(new BoxLayout(video1, BoxLayout.Y_AXIS));
            video1.setPreferredSize(new Dimension(100, 100));
            //video1.setBounds(new Rectangle(0,posY));
            String title = model.videoList.get(i).getSnippet().getTitle();
            ResourceId id = model.videoList.get(i).getId();
            DateTime time = model.videoList.get(i).getSnippet().getPublishedAt();
            Thumbnail thumbnail = model.videoList.get(i).getSnippet().getThumbnails().getDefault();
            String url = thumbnail.getUrl();
            Image tbn = ImageIO.read(new URL(url));
            JButton video_pic = new JButton(new ImageIcon(tbn));
            video_pic.setPreferredSize(new Dimension(60, 60));

            video_pic.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Desktop d = null;
                    if (Desktop.isDesktopSupported()) {
                        d = Desktop.getDesktop();
                    }
                    if (d != null && d.isSupported(Desktop.Action.BROWSE)) {
                        try {
                            System.out.println(id);
                            d.browse(new URI("http://youtube.com/watch?v=" + id.getVideoId()));
                        } catch (Exception excep) {

                        }
                    }
                }
            });
            JPanel rc;
            if (model.rating_num == 0) {

                rc = ratingControl(model.videoList.get(i),0);
            } else {
                rc = ratingControl(model.videoList.get(i), model.rating_db.get(model.videoList.get(i)));
                System.out.println(title + " " + model.rating_db.get(model.videoList.get(i)));
            }
            JLabel titleL = new JLabel(title);
            JLabel timeL = new JLabel(model.getTime(time));
            titleL.setPreferredSize(new Dimension(100, 10));
            timeL.setPreferredSize(new Dimension(100, 10));
            video1.add(rc);
            video1.add(video_pic);
            video1.add(titleL);
            video1.add(timeL);
            p.add(video1);
        }
        //jp.repaint();
    }

    public JPanel ratingControl(SearchResult sr, int rate) {
        JPanel stars = new JPanel();
        //stars.setPreferredSize(new Dimension(100,20));
        stars.setLayout(new FlowLayout());
        JButton rating1 = new JButton();
        rating1.setPreferredSize(new Dimension(10, 10));
        rating1.setBackground(Color.white);
        JButton rating2 = new JButton();
        rating2.setPreferredSize(new Dimension(10, 10));
        rating2.setBackground(Color.white);
        JButton rating3 = new JButton();
        rating3.setPreferredSize(new Dimension(10, 10));
        rating3.setBackground(Color.white);
        JButton rating4 = new JButton();
        rating4.setPreferredSize(new Dimension(10, 10));
        rating4.setBackground(Color.white);
        JButton rating5 = new JButton();
        rating5.setPreferredSize(new Dimension(10, 10));
        rating5.setBackground(Color.white);

        if (rate == 1) {
            rating1.setBackground(Color.yellow);
            rating2.setBackground(Color.white);
            rating3.setBackground(Color.white);
            rating4.setBackground(Color.white);
            rating5.setBackground(Color.white);
        } else if (rate == 2) {
            rating1.setBackground(Color.yellow);
            rating2.setBackground(Color.yellow);
            rating3.setBackground(Color.white);
            rating4.setBackground(Color.white);
            rating5.setBackground(Color.white);
        } else if (rate == 3) {
            rating1.setBackground(Color.yellow);
            rating2.setBackground(Color.yellow);
            rating3.setBackground(Color.yellow);
            rating4.setBackground(Color.white);
            rating5.setBackground(Color.white);
        } else if (rate == 4) {
            rating1.setBackground(Color.yellow);
            rating2.setBackground(Color.yellow);
            rating3.setBackground(Color.yellow);
            rating4.setBackground(Color.yellow);
            rating5.setBackground(Color.white);
        } else if (rate == 5) {
            rating1.setBackground(Color.yellow);
            rating2.setBackground(Color.yellow);
            rating3.setBackground(Color.yellow);
            rating4.setBackground(Color.yellow);
            rating5.setBackground(Color.yellow);
        }

        rating1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rating1.setBackground(Color.yellow);
                rating2.setBackground(Color.white);
                rating3.setBackground(Color.white);
                rating4.setBackground(Color.white);
                rating5.setBackground(Color.white);
                if (sr != null) {
                    model.rating_db.put(sr, 1);
                } else {
                    model.rating_num = 1;
                }
            }
        });
        rating2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rating1.setBackground(Color.yellow);
                rating2.setBackground(Color.yellow);
                rating3.setBackground(Color.white);
                rating4.setBackground(Color.white);
                rating5.setBackground(Color.white);
                if (sr != null) {
                    model.rating_db.put(sr, 2);
                } else {
                    model.rating_num = 2;
                }
            }
        });
        rating3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rating1.setBackground(Color.yellow);
                rating2.setBackground(Color.yellow);
                rating3.setBackground(Color.yellow);
                rating4.setBackground(Color.white);
                rating5.setBackground(Color.white);
                if (sr != null) {
                    model.rating_db.put(sr, 3);
                } else {
                    model.rating_num = 3;
                }
            }
        });
        rating4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rating1.setBackground(Color.yellow);
                rating2.setBackground(Color.yellow);
                rating3.setBackground(Color.yellow);
                rating4.setBackground(Color.yellow);
                rating5.setBackground(Color.white);
                if (sr != null) {
                    model.rating_db.put(sr, 4);
                } else {
                    model.rating_num = 4;
                }
            }
        });
        rating5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rating1.setBackground(Color.yellow);
                rating2.setBackground(Color.yellow);
                rating3.setBackground(Color.yellow);
                rating4.setBackground(Color.yellow);
                rating5.setBackground(Color.yellow);
                if (sr != null) {
                    model.rating_db.put(sr, 5);
                } else {
                    model.rating_num = 5;
                }
            }
        });
        stars.add(rating1);
        stars.add(rating2);
        stars.add(rating3);
        stars.add(rating4);
        stars.add(rating5);
        return stars;
    }
    public void load(int select) {
        p.removeAll();
        p.revalidate();
        if(select != 1){
            p.setLayout(new GridLayout(5, 5));
        }else{
            p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        }
        JFrame f = new JFrame();
        JFileChooser file = new JFileChooser();
        file.setFileFilter(new FileNameExtensionFilter("txt files", "txt"));
        file.setDialogTitle("load a file");
        if (file.showOpenDialog(f) == JFileChooser.APPROVE_OPTION) {
            File loadedF = file.getSelectedFile();
            try {
               FileReader fr = new FileReader(loadedF);
                int size = fr.read();

                for(int i = 0; i < size; i++){
                    System.out.println("load a file");
                    try {
                        char [] titl = new char[50];
                        fr.read(titl);
                        String title = titl.toString();
                        System.out.println(title);
                        char [] idd = new char[500];
                        fr.read(idd);
                        String id = idd.toString();
                        char [] tim = new char[50];
                        fr.read(tim);
                        String time = tim.toString();
                        char [] u = new char[50];
                        fr.read(u);
                        String url = u.toString();
                        char [] ss = new char[50];
                        fr.read(ss);
                        String r = ss.toString();

                        SearchResult sr = new SearchResult();
                        JPanel video1 = new JPanel();
                        video1.setLayout(new BoxLayout(video1, BoxLayout.Y_AXIS));
                        video1.setPreferredSize(new Dimension(100, 100));
                        Image tbn = ImageIO.read(new URL(url));
                        JButton video_pic = new JButton(new ImageIcon(tbn));
                        video_pic.setPreferredSize(new Dimension(60, 60));

                        video_pic.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                Desktop d = null;
                                if (Desktop.isDesktopSupported()) {
                                    d = Desktop.getDesktop();
                                }
                                if (d != null && d.isSupported(Desktop.Action.BROWSE)) {
                                    try {
                                        System.out.println(id);
                                        d.browse(new URI("http://youtube.com/watch?v=" + id));
                                    } catch (Exception excep) {

                                    }
                                }
                            }
                        });
                        JPanel rc;
                        JPanel stars = new JPanel();
                        //stars.setPreferredSize(new Dimension(100,20));
                        stars.setLayout(new FlowLayout());
                        JButton rating1 = new JButton();
                        rating1.setPreferredSize(new Dimension(10, 10));
                        rating1.setBackground(Color.white);
                        JButton rating2 = new JButton();
                        rating2.setPreferredSize(new Dimension(10, 10));
                        rating2.setBackground(Color.white);
                        JButton rating3 = new JButton();
                        rating3.setPreferredSize(new Dimension(10, 10));
                        rating3.setBackground(Color.white);
                        JButton rating4 = new JButton();
                        rating4.setPreferredSize(new Dimension(10, 10));
                        rating4.setBackground(Color.white);
                        JButton rating5 = new JButton();
                        rating5.setPreferredSize(new Dimension(10, 10));
                        rating5.setBackground(Color.white);
                        int rate = Integer.parseInt(r);
                        if (rate == 1) {
                            rating1.setBackground(Color.yellow);
                            rating2.setBackground(Color.white);
                            rating3.setBackground(Color.white);
                            rating4.setBackground(Color.white);
                            rating5.setBackground(Color.white);
                        } else if (rate == 2) {
                            rating1.setBackground(Color.yellow);
                            rating2.setBackground(Color.yellow);
                            rating3.setBackground(Color.white);
                            rating4.setBackground(Color.white);
                            rating5.setBackground(Color.white);
                        } else if (rate == 3) {
                            rating1.setBackground(Color.yellow);
                            rating2.setBackground(Color.yellow);
                            rating3.setBackground(Color.yellow);
                            rating4.setBackground(Color.white);
                            rating5.setBackground(Color.white);
                        } else if (rate == 4) {
                            rating1.setBackground(Color.yellow);
                            rating2.setBackground(Color.yellow);
                            rating3.setBackground(Color.yellow);
                            rating4.setBackground(Color.yellow);
                            rating5.setBackground(Color.white);
                        } else if (rate == 5) {
                            rating1.setBackground(Color.yellow);
                            rating2.setBackground(Color.yellow);
                            rating3.setBackground(Color.yellow);
                            rating4.setBackground(Color.yellow);
                            rating5.setBackground(Color.yellow);
                        }
                        stars.add(rating1);
                        stars.add(rating2);
                        stars.add(rating3);
                        stars.add(rating4);
                        stars.add(rating5);
                        JLabel titleL = new JLabel(title);
                        JLabel timeL = new JLabel(time);
                        titleL.setPreferredSize(new Dimension(100, 10));
                        timeL.setPreferredSize(new Dimension(100, 10));
                        video1.add(stars);
                        video1.add(video_pic);
                        video1.add(titleL);
                        video1.add(timeL);
                        p.add(video1);
                    }catch (Exception e){

                    }
                }


            } catch (IOException ioe) {
            }
        }
    }
}
