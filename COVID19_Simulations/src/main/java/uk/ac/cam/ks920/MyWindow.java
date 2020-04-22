package uk.ac.cam.ks920;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class MyWindow {

    private JFrame frame;
    private COVIDPopulationPainter APainter;
    private COVIDCommunitiesPainter CPainter;
    private PlottingPainter PPainter;
    private PainterPanel painterPanel;
    public final double metresPerPixel;

    public MyWindow(COVID19_Population population, COVID19_Plotter plotter, int AnimationSideLength) {
        frame = new JFrame();
        int frameWidth = 1400;
        int frameHeight = 850;
        int PlotWidth = (int)((frameWidth - AnimationSideLength) * 0.9);
        int PlotHeight = (int)Math.min(PlotWidth * 0.75, frameHeight * 0.8);
        int APanelPadding = 20;
        metresPerPixel = (double)population.sideLength / AnimationSideLength;

        APainter = new COVIDPopulationPainter(frameWidth - AnimationSideLength - APanelPadding,
                (frameHeight - AnimationSideLength) / 2, AnimationSideLength, AnimationSideLength, population);

        CPainter = new COVIDCommunitiesPainter(frameWidth - AnimationSideLength - APanelPadding,
                (frameHeight - AnimationSideLength) / 2, AnimationSideLength, AnimationSideLength, population);

        PPainter = new PlottingPainter((frameWidth - AnimationSideLength - PlotWidth) / 2,
                (frameHeight - PlotHeight) / 2, PlotWidth, PlotHeight, plotter);

        painterPanel = new PainterPanel(APainter, PPainter);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("COVID-19 Simulation");
        frame.setVisible(true);
        frame.setSize(frameWidth, frameHeight);
        frame.add(painterPanel);

    }

    public void PaintAllCommunities(Color c) {
        CPainter.SetToShowAll(c);
        painterPanel.addPainter(CPainter);
        Update();
    }

    public void PaintCommunities(Color c, int... comNumbers) {
        CPainter.SetToShow(c, comNumbers);
        painterPanel.addPainter(CPainter);
        Update();
    }

    public void StopShowingCommunities() {
        painterPanel.removePainter(CPainter);
    }

    public void Update() {
        frame.repaint();
    }

    class PainterPanel extends JPanel {

        ArrayList<MyPainter> Painters;

        public PainterPanel(ArrayList<MyPainter> Painters) {
            this.Painters = Painters;
        }

        public PainterPanel(MyPainter... Painters) {
            this.Painters = new ArrayList<MyPainter>();
            this.Painters.addAll(Arrays.asList(Painters));
        }

        public void addPainter(MyPainter painter) {
            Painters.add(painter);
        }

        public void removePainter(MyPainter painter) {
            Painters.remove(painter);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            for (MyPainter p : Painters) {
                p.Paint(g);
            }
        }
    }

    abstract class MyPainter {
        int x, y, width, height;

        public MyPainter(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public abstract void Paint(Graphics g);
    }

    class COVIDCommunitiesPainter extends MyPainter {

        COVID19_Population pop;
        private ArrayList<Integer> comsToShow;
        private Color circleColor = Color.WHITE;

        COVIDCommunitiesPainter(int x, int y, int width, int height, COVID19_Population pop) {
            super(x, y, width, height);
            this.pop = pop;
            comsToShow = new ArrayList<Integer>();
        }

        public void SetToShow(Color CircleColor, int... comNumbers) {
            comsToShow = new ArrayList<Integer>();
            for (int i : comNumbers) {
                comsToShow.add(i);
            }
            circleColor = CircleColor;
        }

        public void SetToShowAll(Color CircleColor) {
            comsToShow = new ArrayList<Integer>();
            for (int i = 0; i < pop.numCommunities; i++)
                comsToShow.add(i);
            circleColor = CircleColor;
        }

        public void Paint(Graphics g) {
            g.setColor(circleColor);
            ArrayList<COVID19_Population.MyCommunity> communities = pop.GetCommunities();
            for (int comNumber : comsToShow) {
                COVID19_Population.MyCommunity currentCom = communities.get(comNumber);
                g.drawOval(x + (int)((currentCom.x - currentCom.radius)/metresPerPixel),
                        y + (int)((currentCom.y - currentCom.radius)/metresPerPixel),
                        (int)(2 * currentCom.radius / metresPerPixel),
                        (int)(2 * currentCom.radius / metresPerPixel));
            }
        }
    }

    class COVIDPopulationPainter extends MyPainter {

        COVID19_Population pop;
        private int memberwidth, memberheight;

        public COVIDPopulationPainter(int x, int y, int width, int height, COVID19_Population pop) {
            super(x, y, width, height);
            this.pop = pop;
            memberwidth = 5;
            memberheight = memberwidth;
        }

        public void Paint(Graphics g) {
            g.setColor(new Color(19, 19, 109));
            g.fillRect(x - memberwidth/2, y - memberheight/2, width + memberwidth, height + memberheight);

            for (Member m : pop.CommunityMembers) {
                if (m.GetTC() >= 0) //Infected
                    g.setColor(new Color(255, 0, 0));
                else if (m.IsHealed())
                    g.setColor(new Color(0, 255, 0));
                else if (m.IsDeceased())
                    g.setColor(new Color(255, 240, 0));
                else
                    g.setColor(new Color(255, 255, 255));

                g.drawOval(x + (int)(m.GetX()/metresPerPixel) - memberwidth / 2,
                        y + (int)(m.GetY()/metresPerPixel) - memberheight / 2,
                        memberwidth,
                        memberheight);
            }
        }
    }

    class PlottingPainter extends MyPainter {

        public COVID19_Plotter plotter;
        int PopCap;

        public PlottingPainter(int x, int y, int width, int height, COVID19_Plotter plotter) {
            super(x, y, width, height);
            this.plotter = plotter;
            PopCap = plotter.GetPopulation().Capacity;
        }

        public void Paint(Graphics g) {
            g.setColor(new Color(126, 169, 255));
            g.fillRect(x, y, width, height);

            //Retrieve Data
            ArrayList<int[]> data = plotter.GetData();

            //Plot Bar-Chart
            int numBars = data.size();
            int prevxcoord = x;
            int prevwidth = 0;
            for (int i = 0; i < data.size(); i++) {
                int xcoord = prevxcoord + prevwidth;
                int thiswidth = (i + 1) * width / numBars + x - xcoord;
                //Infected
                int TopOfInfBar = y + (int)((1 - ((double)data.get(i)[1]/PopCap)) * height);
                g.setColor(Color.RED);
                g.fillRect(xcoord, TopOfInfBar, thiswidth, height * data.get(i)[1]/PopCap);

                //Unaffected
                int TopOfUBar = TopOfInfBar - (height * data.get(i)[0])/PopCap;
                g.setColor(Color.DARK_GRAY);
                g.fillRect(xcoord, TopOfUBar, thiswidth,(height * data.get(i)[0])/PopCap);

                //Healed
                int TopOfHBar = TopOfUBar - (height * data.get(i)[2])/PopCap;
                g.setColor(new Color(76, 200, 83));
                g.fillRect(xcoord, TopOfHBar, thiswidth, (height * data.get(i)[2])/PopCap);

                //Deceased
                g.setColor(new Color(255, 240, 0));
                g.fillRect(xcoord, y, thiswidth, TopOfHBar - y);

                prevxcoord = xcoord;
                prevwidth = thiswidth;
            }

            //Axes
            g.setColor(Color.BLACK);
            g.drawLine(x, y, x, y + height);
            g.drawLine(x, y + height, x + width, y + height);
        }
    }
}
