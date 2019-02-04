import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Main extends JPanel implements Runnable{

    public static void main(String[] args){
        int waveLength = -1;
        int wavesWide = -1;
        int wavesHigh = -1;

        boolean valid = true;
        if(args.length < 3){
            valid = false;
        } else {

            waveLength = Integer.parseInt(args[0]);
            wavesWide = Integer.parseInt(args[1]);
            wavesHigh = Integer.parseInt(args[2]);

            if(waveLength <= 1 || wavesHigh < 1 || wavesWide < 1){
                valid = false;
            }
        }

        if(!valid){
            waveLength = 8;
            wavesWide = 10;
            wavesHigh = 10;
        }

        JFrame frame = new JFrame(" Perlin Noise ");

        Main p = new Main(waveLength, wavesWide, wavesHigh);
        frame.add(p);

        frame.setSize(2700, 1600);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height*19/40-frame.getSize().height/2);

        //frame.setUndecorated(true);
        frame.setVisible(true);


        p.run();
    }

    private final int BORDER = 100;

    private int i = 0;
    private Random r = new Random();
    private double x = 0, y;

    private double waveLength;

    private double startOfWaveHeight;
    private double endOfWaveHeight;

    private double wavesWide;
    private double wavesHigh;

    private ArrayList<Integer> xPositionsOfStartArray = new ArrayList<>();
    private ArrayList<ArrayList<Pair>> EndOfWaveHeightArrays = new ArrayList<>();

    // contains arrays of (x, y) coordinates. The first element in each array has z = 0 and the next has z = 1 etc.
    private ArrayList<ArrayList<Pair>> Interpolation3D = new ArrayList<>();

    public Main(int waveLength, int width, int height){
        this.waveLength = waveLength;
        wavesWide = width;
        wavesHigh = height;
        performInterpolation3D();
    }

    private void performInterpolation3D(){

        // Ensure all needed arrays are empty.
        Interpolation3D.clear();
        EndOfWaveHeightArrays.clear();
        xPositionsOfStartArray.clear();

        // Fill a 2D array to act as starting 2D wave.
        ArrayList<Pair> startOfWaveHeightArray = getInterpolatedArray();

        // Fill Array with a number of 2D arrays to act as ending 2D waves.
        for(int j = 0; j <= wavesHigh; j++){
            EndOfWaveHeightArrays.add(getInterpolatedArray());
        }

        // The starting and ending arrays will be interpolated.

        // Record x positions of (x, y) points in starting array.
        for(Pair p: startOfWaveHeightArray){
            xPositionsOfStartArray.add((int)p.x);
        }

        for(int xPos: xPositionsOfStartArray) {
            Interpolation3D.add(new ArrayList<>());
            x = 0;
            int index = 0;

            // 2D interpolation between the below points.
            startOfWaveHeight = startOfWaveHeightArray.get(xPositionsOfStartArray.indexOf(xPos)).getY();
            endOfWaveHeight = EndOfWaveHeightArrays.get(index).get(xPositionsOfStartArray.indexOf(xPos)).getY();

            while (x < wavesHigh * waveLength) {
                if (x % waveLength == 0 && x !=0) {
                    startOfWaveHeight = endOfWaveHeight;
                    index++;
                    endOfWaveHeight = EndOfWaveHeightArrays.get(index).get(xPositionsOfStartArray.indexOf(xPos)).getY();
                    y = startOfWaveHeight;
                } else {
                    y = interpolate(startOfWaveHeight, endOfWaveHeight, x);
                }

                x++;
                Interpolation3D.get(xPositionsOfStartArray.indexOf(xPos)).add(new Pair(xPos,y));
            }
        }

    }

    private ArrayList<Pair> getInterpolatedArray(){
        ArrayList<Pair> array = new ArrayList<>();

        // random points to be interpolated.
        // startOfWaveHeight and endOfWaveHeight are heights startOfWaveHeight wavelength apart from each other.
        startOfWaveHeight = r.nextDouble();
        endOfWaveHeight = r.nextDouble();

        // number of Pairs in array.
        x = 0;

        while(x < wavesWide * waveLength){

            //
            if(x % waveLength == 0){
                startOfWaveHeight = endOfWaveHeight;
                endOfWaveHeight = r.nextDouble();
                y = startOfWaveHeight;
            }
            else{
                y = interpolate(startOfWaveHeight, endOfWaveHeight, x);
            }
            x++;
            array.add(new Pair(x, y));
        }
        return array;
    }

    private double interpolate(double pa, double pb, double x){
        // px = how many parts way through the wave x is.
        double px = (x % waveLength) / waveLength;

        // ft = px in radians.
        double ft = px*Math.PI;

        // invert cos on [0, pi] and divide by two to keep y values in [0, 1].
        double f = (1-Math.cos(ft))*0.5;

        // We find the height of the interpolated point by taking parts of pa and pb and multiplying them by the
        // value f's complement and f respectively.
        return(pa*(1-f)+pb*f);
    }

    @Override
    public void run() {
        for(;;){
            repaint();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void paint(Graphics g){


        g.setColor(new Color(30,30,30));
        g.fillRect(0,0,3100,1600);



        i+=1;
        if(i > Interpolation3D.get(0).size()-1){
            System.out.println("Finished");
            performInterpolation3D();
            i = 0;
            return;
        }

        for(int k = 0; k <= i; k++) {
            g.setColor(Color.BLACK);

            //g.fillRect(2000, 0, 1100,1600);
            for (ArrayList<Pair> ap : Interpolation3D) {
                g.setColor(new Color(80,210,220));
                if (ap.get(k).y < 0.6) {
                    g.setColor(new Color(30,110,35));
                }
                if (Math.abs(ap.get(k).y - 0.6)<0.02) {
                    g.setColor(new Color(200,175,75));
                }
                if (ap.get(k).y < 0.25) {
                    g.setColor(new Color(0,50,10));
                }
                if (ap.get(k).y < 0.20) {
                    g.setColor(new Color(100,100,100));
                }
                if (ap.get(k).y < 0.07) {
                    g.setColor(new Color(255,255,255));
                }
                if (ap.get(k).y > 0.8) {
                    g.setColor(new Color(27,69,118));
                }


                // displays y cross-section

                if(i*(int)(1300/(wavesWide*waveLength)) > 100){
                    g.fillOval((int) (1500+ ap.get(i).x * 1000 / (wavesWide*waveLength)),
                            (int) ((ap.get(i).y -0.4)*(200 - (wavesWide*waveLength))) + 100, 10, 10);
                }


                g.fillOval((int) (1500+ ap.get(i).x * 1000 / (wavesWide*waveLength)),
                        (int) ((ap.get(i).y -0.4)*(200 - (wavesWide*waveLength))) + 100 + i*(int)(1300/(wavesWide*waveLength)), 10, 10);

                int border = 1;
                // displays z cross-section
                g.fillRect(
                        BORDER + (Interpolation3D.indexOf(ap)*(int)(1300/(wavesWide*waveLength))) + border,
                        (BORDER + k*(int)(1300/ (wavesHigh*waveLength))) + border,
                        (int)(1300/(wavesWide*waveLength)) - 2*border,
                        (int)(1300/ (wavesHigh*waveLength)) - 2*border);

                g.setColor(Color.WHITE);
                g.drawLine(1500,(int)(0.58*200) + i*(int)(1300/(wavesWide*waveLength)), 2500, (int)
                        (0.58*200) + i*(int)(1300/(wavesWide*waveLength)));

                if(i*(int)(1300/(wavesWide*waveLength)) > 100){
                    g.drawLine(1500,(int)(0.58*200), 2500, (int)
                            (0.58*200));
                }
            }
        }

    }
}

