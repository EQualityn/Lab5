package bsu.rfe.java.group8.lab5.Kalitin.varB4;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JPanel;

public class GraphicsDisplay extends JPanel {
    private ArrayList<Double[]> graphicsData;
    private ArrayList<Double[]> originalData;
    private int selectedMarker = -1;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    private double scaleX;
    private double scaleY;

    private double[][] viewport = new double[2][2];
    private ArrayList<double[][]> undoHistory = new ArrayList(5);
    private boolean showAxis = true;
    private boolean showMarkers = true;

    private Font axisFont;
    private Font labelsFont;

    private BasicStroke axisStroke;
    private BasicStroke graphicsStroke;
    private BasicStroke markerStroke;
    private BasicStroke selectionStroke;
    private static DecimalFormat formatter=(DecimalFormat)NumberFormat.getInstance();

    private boolean ShowAxis = true;
    private boolean ShowMarkers = true;
    private boolean scaleMode = false;
    private boolean changeMode = false;
    private double[] originalPoint = new double[2];
    private Rectangle2D.Double selectionRect = new Rectangle2D.Double();

    public GraphicsDisplay() {
        this.setBackground(Color.white);
        graphicsStroke = new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[] {30, 10, 20, 10, 10, 10, 20, 10}, 0.0f);
        this.axisStroke = new BasicStroke(2.0F, 0, 0, 10.0F, (float[])null, 0.0F);
        this.markerStroke = new BasicStroke(4.0F, 0, 0, 10.0F, (float[])null, 0.0F);
        this.selectionStroke = new BasicStroke(1.0F, 0, 0, 10.0F, new float[]{10.0F, 10.0F}, 0.0F);
        this.axisFont = new Font("Serif", 1, 36);
        this.labelsFont = new Font("Serif", 0, 20);
        formatter.setMaximumFractionDigits(5);

    }
    public void showGraphics(ArrayList<Double[]> graphicsData)	{
        this.graphicsData = graphicsData;
        this.originalData = new ArrayList(graphicsData.size());
        for (Double[] point : graphicsData) {
            Double[] newPoint = new Double[2];
            newPoint[0] = new Double(point[0].doubleValue());
            newPoint[1] = new Double(point[1].doubleValue());
            this.originalData.add(newPoint);
        }
        this.minX = ((Double[])graphicsData.get(0))[0].doubleValue();
        this.maxX = ((Double[])graphicsData.get(graphicsData.size() - 1))[0].doubleValue();
        this.minY = ((Double[])graphicsData.get(0))[1].doubleValue();
        this.maxY = this.minY;

        for (int i = 1; i < graphicsData.size(); i++) {
            if (((Double[])graphicsData.get(i))[1].doubleValue() < this.minY) {
                this.minY = ((Double[])graphicsData.get(i))[1].doubleValue();
            }
            if (((Double[])graphicsData.get(i))[1].doubleValue() > this.maxY) {
                this.maxY = ((Double[])graphicsData.get(i))[1].doubleValue();
            }
        }
        zoomToRegion(minX, maxY, maxX, minY);
    }
    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }
    public void displayGraphics(ArrayList<Double[]> graphicsData) {
        this.graphicsData = graphicsData;
        this.originalData = new ArrayList(graphicsData.size());
        Iterator var3 = graphicsData.iterator();

        while(var3.hasNext()) {
            Double[] point = (Double[])var3.next();
            Double[] newPoint = new Double[]{new Double(point[0]), new Double(point[1])};
            this.originalData.add(newPoint);
        }

        this.minX = ((Double[])graphicsData.get(0))[0];
        this.maxX = ((Double[])graphicsData.get(graphicsData.size() - 1))[0];
        this.minY = ((Double[])graphicsData.get(0))[1];
        this.maxY = this.minY;

        for(int i = 1; i < graphicsData.size(); ++i) {
            if (((Double[])graphicsData.get(i))[1] < this.minY) {
                this.minY = ((Double[])graphicsData.get(i))[1];
            }

            if (((Double[])graphicsData.get(i))[1] > this.maxY) {
                this.maxY = ((Double[])graphicsData.get(i))[1];
            }
        }

        this.zoomToRegion(this.minX, this.maxY, this.maxX, this.minY);
    }

    public void zoomToRegion(double x1, double y1, double x2, double y2) {
        this.viewport[0][0] = x1;
        this.viewport[0][1] = y1;
        this.viewport[1][0] = x2;
        this.viewport[1][1] = y2;
        this.repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.scaleX = this.getSize().getWidth() / (this.viewport[1][0] - this.viewport[0][0]);
        this.scaleY = this.getSize().getHeight() / (this.viewport[0][1] - this.viewport[1][1]);
        if (this.graphicsData != null && this.graphicsData.size() != 0) {
            Graphics2D canvas = (Graphics2D)g;
            this.paintAxis(canvas);
            this.paintGraphics(canvas);
            this.paintMarkers(canvas);
            this.paintLabels(canvas);
            this.paintSelection(canvas);
        }
    }

    private void paintSelection(Graphics2D canvas) {
        if (this.scaleMode) {
            canvas.setStroke(this.selectionStroke);
            canvas.setColor(Color.BLACK);
            canvas.draw(this.selectionRect);
        }
    }

    protected void paintGraphics (Graphics2D canvas) {
        canvas.setStroke(this.graphicsStroke);
        canvas.setColor(Color.PINK);
        // Линии
        Double currentX = null;
        Double currentY = null;
        for (Double[] point : this.graphicsData)
        {
            if ((point[0].doubleValue() >= this.viewport[0][0]) && (point[1].doubleValue() <= this.viewport[0][1]) &&
                    (point[0].doubleValue() <= this.viewport[1][0]) && (point[1].doubleValue() >= this.viewport[1][1]))
            {
                if ((currentX != null) && (currentY != null)) {
                    canvas.draw(new Line2D.Double(xyToPoint(currentX.doubleValue(), currentY.doubleValue()),
                            xyToPoint(point[0].doubleValue(), point[1].doubleValue())));
                }
                currentX = point[0];
                currentY = point[1];
            }
        }
    }

    private boolean markPoint(double y) {
        int n = (int) y;
        if (n < 0)
            n *= (-1);
        while (n != 0) {
            int q = n - (n / 10) * 10;
            if (q % 2 != 0)
                return false;
            n = n / 10;
        }
        return true;
    }

    private void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(markerStroke);
        canvas.setColor(Color.BLACK);
        for (Double[] point : graphicsData)
        {   // graphicsData[i][0]
            if (markPoint(point[1]))
                canvas.setColor(Color.BLUE);
            else
                canvas.setColor(Color.BLACK);

            GeneralPath path = new GeneralPath();
            Point2D.Double center = xyToPoint(point[0], point[1]);
            path.moveTo(center.x, center.y + 5);
            path.lineTo(center.x + 5, center.y);
            path.lineTo(center.x, center.y - 5);
            path.lineTo(center.x - 5, center.y);
            path.lineTo(center.x, center.y + 5);
            canvas.draw(path);
        }
    }

    private void paintLabels(Graphics2D canvas)
    {
        // Подписи координат и сетки
        canvas.setColor(Color.BLUE);
        canvas.setFont(this.labelsFont);
        FontRenderContext context=canvas.getFontRenderContext();
        double labelYPos;
        double labelXPos;
        if (!(viewport[1][1] >= 0 || viewport[0][1] <= 0))
            labelYPos = 0;
        else labelYPos = viewport[1][1];
        if (!(viewport[0][0] >= 0 || viewport[1][0] <= 0.0D))
            labelXPos=0;
        else labelXPos = viewport[0][0];
        double pos = viewport[0][0];
        double step = (viewport[1][0] - viewport[0][0]) / 10;
        while (pos < viewport[1][0]){
            java.awt.geom.Point2D.Double point = xyToPoint(pos,labelYPos);
            String label = formatter.format(pos);
            Rectangle2D bounds = labelsFont.getStringBounds(label,context);
            canvas.drawString(label, (float)(point.getX() + 5), (float)(point.getY() - bounds.getHeight()));
            pos=pos + step;
        }
        pos = viewport[1][1];
        step = (viewport[0][1] - viewport[1][1]) / 10.0D;
        while (pos < viewport[0][1]){
            Point2D.Double point = xyToPoint(labelXPos,pos);
            String label=formatter.format(pos);
            Rectangle2D bounds = labelsFont.getStringBounds(label,context);
            canvas.drawString(label,(float)(point.getX() + 5),(float)(point.getY() - bounds.getHeight()));
            pos=pos + step;
        }
        if (selectedMarker >= 0)
        {
            Point2D.Double point = xyToPoint(((Double[])graphicsData.get(selectedMarker))[0].doubleValue(),
                    ((Double[])graphicsData.get(selectedMarker))[1].doubleValue());
            String label = "X=" + formatter.format(((Double[])graphicsData.get(selectedMarker))[0]) +
                    ", Y=" + formatter.format(((Double[])graphicsData.get(selectedMarker))[1]);
            Rectangle2D bounds = labelsFont.getStringBounds(label, context);
            canvas.setColor(Color.BLACK);
            canvas.drawString(label, (float)(point.getX() + 5.0D), (float)(point.getY() - bounds.getHeight()));
        }
    }

    protected void paintAxis(Graphics2D canvas){
        // Оси
        canvas.setStroke(this.axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setFont(this.axisFont);
        FontRenderContext context=canvas.getFontRenderContext();
        if (!(viewport[0][0] > 0|| viewport[1][0] < 0)){
            canvas.draw(new Line2D.Double(xyToPoint(0, viewport[0][1]), xyToPoint(0, viewport[1][1])));
            canvas.draw(new Line2D.Double(xyToPoint(-(viewport[1][0] - viewport[0][0]) * 0.0025, viewport[0][1] - (viewport[0][1] - viewport[1][1]) * 0.015),xyToPoint(0,viewport[0][1])));
            canvas.draw(new Line2D.Double(xyToPoint((viewport[1][0] - viewport[0][0]) * 0.0025, viewport[0][1] - (viewport[0][1] - viewport[1][1]) * 0.015), xyToPoint(0, viewport[0][1])));
            Rectangle2D bounds = axisFont.getStringBounds("y",context);
            Point2D.Double labelPos = xyToPoint(0.0, viewport[0][1]);
            canvas.drawString("y",(float)labelPos.x + 10,(float)(labelPos.y + bounds.getHeight() / 2));
        }
        if (!(viewport[1][1] > 0.0D || viewport[0][1] < 0.0D)){
            canvas.draw(new Line2D.Double(xyToPoint(viewport[0][0],0),
                    xyToPoint(viewport[1][0],0)));
            canvas.draw(new Line2D.Double(xyToPoint(viewport[1][0] - (viewport[1][0] - viewport[0][0]) * 0,
                    (viewport[0][1] - viewport[1][1]) * 0.005), xyToPoint(viewport[1][0], 0)));
            canvas.draw(new Line2D.Double(xyToPoint(viewport[1][0] - (viewport[1][0] - viewport[0][0]) * 0.01,
                    -(viewport[0][1] - viewport[1][1]) * 0.005), xyToPoint(viewport[1][0], 0)));
            Rectangle2D bounds = axisFont.getStringBounds("x",context);
            Point2D.Double labelPos = xyToPoint(this.viewport[1][0],0.0D);
            canvas.drawString("x",(float)(labelPos.x - bounds.getWidth() - 10),(float)(labelPos.y - bounds.getHeight() / 2));
        }
    }

    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - viewport[0][0];
        double deltaY = viewport[0][1] - y;
        return new Point2D.Double(deltaX*scaleX, deltaY*scaleY);
    }

    protected double[] translatePointToXY(int x, int y)
    {
        return new double[] { this.viewport[0][0] + x / this.scaleX, this.viewport[0][1] - y / this.scaleY };
    }

    protected int findSelectedPoint(int x, int y)
    {
        if (graphicsData == null) return -1;
        int pos = 0;
        for (Double[] point : graphicsData) {
            Point2D.Double screenPoint = xyToPoint(point[0].doubleValue(), point[1].doubleValue());
            double distance = (screenPoint.getX() - x) * (screenPoint.getX() - x) + (screenPoint.getY() - y) * (screenPoint.getY() - y);
            if (distance < 100) return pos;
            pos++;
        }	    return -1;
    }
    //Возвращает стандартный график



    }


