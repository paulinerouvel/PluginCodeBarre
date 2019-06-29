import com.asprise.ocr.Ocr;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import org.json.JSONObject;

import static org.bytedeco.javacpp.opencv_core.IplImage;
import static org.bytedeco.javacpp.opencv_core.cvFlip;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;





public class CodeReaderPlugin implements Runnable  {


    final int INTERVAL = 100;///you may use interval
    CanvasFrame canvas = new CanvasFrame("Web Cam");
    static String namePhoto ="";
    static IplImage imgToSave = null;


    public CodeReaderPlugin() {

        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }


    //methode de runnable
    public void run() {

        FrameGrabber grabber = new VideoInputFrameGrabber(0); // 1 for next camera
        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        IplImage img;
        int i = 0;

        try {
            grabber.start();

           while (true) {
                Frame frame = grabber.grab();

                img = converter.convert(frame);

                //the grabbed frame will be flipped, re-flip to make it right
                cvFlip(img, img, 1);// l-r = 90_degrees_steps_anti_clockwise


                canvas.showImage(converter.convert(img));

                namePhoto = String.valueOf(i);
                imgToSave = img;

                i++;

                Thread.sleep(INTERVAL);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void launch() {
        CodeReaderPlugin gs = new CodeReaderPlugin();
        Thread th = new Thread(gs);
        th.start();


        JFrame page = new JFrame("Fenetre");
        Button b = new Button();



        b.setBounds(15,15,15,15);
        b.setLabel("Prendre une photo");
        b.setVisible(true);


        page.setBounds(50,50,500,500);
        page.add(b);
        page.setVisible(true);

        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                Date today = new Date();
                int hash = today.hashCode();
                cvSaveImage( "src/main/resources/capture" + (hash) + ".jpg", imgToSave);
                System.out.println("Image prise");


                Ocr.setUp(); // one time setup
                Ocr ocr = new Ocr(); // create a new OCR engine
                ocr.startEngine("eng", Ocr.SPEED_FASTEST); // English
                String s = ocr.recognize(new File[] {new File("src/main/resources/capture" + (hash) + ".jpg")},
                        Ocr.RECOGNIZE_TYPE_BARCODE, Ocr.OUTPUT_FORMAT_PLAINTEXT); // PLAINTEXT | XML | PDF | RTF
                System.out.println("Result: " + s);
                ocr.stopEngine();


                File f = new File("src/main/resources/capture" + (hash) + ".jpg");
                f.deleteOnExit();

                s = "[[EAN-13: 8002270014901]]";

                String[] tabS = s.split(" ");

                String code = tabS[1].substring(0, 13);




                String urlStr = "https://fr.openfoodfacts.org/api/v0/produit/" + code + ".json";

                try{
                    URL url = new URL(urlStr);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.connect();

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();



                    JSONObject productInfo = new JSONObject(response.toString());
                    JSONObject product = productInfo.getJSONObject("product");
                    System.out.println(product.getString("brands"));
                    System.out.println(product.getString("generic_name_fr"));
                    System.out.println(product.getString("image_small_url"));



                }
                catch (Exception exec){
                    System.out.println(exec);
                }
            }
        });

    }





}