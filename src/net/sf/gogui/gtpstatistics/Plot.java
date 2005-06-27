//----------------------------------------------------------------------------
// $Id$
// $Source$
//----------------------------------------------------------------------------

package net.sf.gogui.gtpstatistics;

import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import javax.imageio.ImageIO;
import net.sf.gogui.utils.Table;

//----------------------------------------------------------------------------

public class Plot
{
    public Plot(int imgWidth, int imgHeight, Color color, int precision)
    {
        m_precision = precision;
        m_color = color;
        m_imgWidth = imgWidth;
        m_imgHeight = imgHeight;
    }

    public void plot(File file, Table table, String columnX, String columnY,
                     String errorColumn)
        throws IOException
    {
        if (m_title == null)
            m_title = columnY;
        int type = BufferedImage.TYPE_INT_RGB;
        BufferedImage image
            = new BufferedImage(m_imgWidth, m_imgHeight, type);
        m_graphics2D = image.createGraphics();
        m_graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                      RenderingHints.VALUE_ANTIALIAS_ON);
        m_metrics = m_graphics2D.getFontMetrics();
        m_left = 4 * m_metrics.getAscent();
        m_top = (int)(m_metrics.getAscent() * 1.7);
        m_right = m_imgWidth - 10;
        m_bottom = m_imgHeight - m_top;
        m_width = m_right - m_left;
        m_height = m_bottom - m_top;
        initScale(table, columnX, columnY);
        drawBackground();
        drawGrid();
        drawData(table, columnX, columnY, errorColumn, m_withBars);
        m_graphics2D.dispose();
        ImageIO.write(image, "png", file);
    }

    public void setPlotStyleBars()
    {
        m_withBars = true;
    }

    public void setSolidLineInterval(double solidLineInterval)
    {
        m_solidLineInterval = solidLineInterval;
        m_useSolidLineInterval = true;
    }

    public void setXTics(double tics)
    {
        m_xTics = tics;
        m_autoXTics = false;
    }

    public void setTitle(String title)
    {
        m_title = title;
    }

    private boolean m_autoXTics = true;

    private boolean m_onlyBoolValues;

    private boolean m_onlyIntValues;

    private boolean m_useSolidLineInterval = false;

    private boolean m_withBars;

    private int m_bottom;

    private int m_height;

    private int m_imgHeight;

    private int m_imgWidth;

    private int m_left;

    private int m_precision;

    private int m_right;

    private int m_top;

    private int m_width;

    private double m_minX;

    private double m_maxX;

    private double m_minY;

    private double m_maxY;

    private double m_solidLineInterval;

    private double m_xRange;

    private double m_xTics;

    private double m_xTicsMin;

    private double m_yRange;

    private double m_yTics;

    private double m_yTicsMin;

    private Color m_color = Color.decode("#ff5454");

    private FontMetrics m_metrics;

    private Graphics2D m_graphics2D;

    private String m_title;

    private void drawBackground()
    {
        m_graphics2D.setColor(Color.decode("#e0e0e0"));
        m_graphics2D.fillRect(0, 0, m_imgWidth, m_imgHeight);
        m_graphics2D.setColor(Color.WHITE);
        m_graphics2D.fillRect(m_left, m_top, m_width, m_height);
        m_graphics2D.setColor(Color.LIGHT_GRAY);
        m_graphics2D.drawRect(m_left, m_top, m_width, m_height);
        m_graphics2D.setColor(Color.BLACK);
        int width = m_metrics.stringWidth(m_title) + 10;
        int height = (int)(m_metrics.getAscent() * 1.4);
        int x = m_left + (m_width - width) / 2;
        int y = (m_top - height) / 2;
        m_graphics2D.setColor(Color.WHITE);
        m_graphics2D.fillRect(x, y, width, height);
        m_graphics2D.setColor(Color.DARK_GRAY);
        m_graphics2D.drawRect(x, y, width, height);
        drawString(m_title, m_left + m_width / 2, m_top / 2);
    }

    private void drawData(Table table, String columnX, String columnY,
                          String errorColumn, boolean withBars)
    {
        m_graphics2D.setColor(m_color);
        Point last = null;
        for (int row = 0; row < table.getNumberRows(); ++row)
        {
            try
            {
                double x = Double.parseDouble(table.get(columnX, row));
                double y = Double.parseDouble(table.get(columnY, row));
                Point point = getPoint(x, y);
                if (withBars || m_onlyBoolValues)
                {
                    Point bottom = getPoint(x, 0);
                    m_graphics2D.drawLine(bottom.x, bottom.y,
                                          point.x, point.y);
                }
                else if (last != null)
                    m_graphics2D.drawLine(last.x, last.y, point.x, point.y);
                if (errorColumn != null)
                {
                    double err
                        = Double.parseDouble(table.get(errorColumn, row));
                    Point top = getPoint(x, y + err);
                    Point bottom = getPoint(x, y - err);
                    m_graphics2D.drawLine(top.x, top.y, bottom.x, bottom.y);
                }
                m_graphics2D.fillRect(point.x - 2, point.y - 2, 5, 5);
                last = point;
            }
            catch (NumberFormatException e)
            {
                last = null;
            }
        }
    }

    private void drawGrid()
    {
        Stroke oldStroke = m_graphics2D.getStroke();
        Stroke dottedStroke
            = new BasicStroke(1f, BasicStroke.CAP_ROUND,
                              BasicStroke.JOIN_ROUND, 1f, new float[] {2f},
                              0f);
        m_graphics2D.setStroke(dottedStroke);
        for (double x = m_xTicsMin; x < m_maxX; x += m_xTics)
        {
            Point bottom = getPoint(x, m_minY);
            Point top = getPoint(x, m_maxY);
            m_graphics2D.setColor(Color.LIGHT_GRAY);
            m_graphics2D.drawLine(top.x, top.y, bottom.x, bottom.y);
        }
        m_graphics2D.setStroke(oldStroke);
        if (m_useSolidLineInterval)
        {
            double min =
                (int)(m_xTicsMin / m_solidLineInterval + 1)
                * m_solidLineInterval;
            for (double x = min; x < m_maxX; x += m_solidLineInterval)
            {
                Point bottom = getPoint(x, m_minY);
                Point top = getPoint(x, m_maxY);
                m_graphics2D.setColor(Color.GRAY);
                m_graphics2D.drawLine(top.x, top.y, bottom.x, bottom.y);
            }
        }
        m_graphics2D.setStroke(dottedStroke);
        for (double y = m_yTicsMin; y < m_maxY; y += m_yTics)
        {
            Point left = getPoint(m_minX, y);
            Point right = getPoint(m_maxX, y);
            m_graphics2D.setColor(Color.LIGHT_GRAY);
            m_graphics2D.drawLine(left.x, left.y, right.x, right.y);
        }
        m_graphics2D.setStroke(oldStroke);
        m_graphics2D.setColor(Color.GRAY);
        if (m_minX <= 0 && m_maxX >= 0)
        {
            Point top = getPoint(0, m_minY);
            Point bottom = getPoint(0, m_maxY);
            m_graphics2D.drawLine(top.x, top.y, bottom.x, bottom.y);
        }
        if (m_minY <= 0 && m_maxY >= 0)
        {
            Point left = getPoint(m_minX, 0);
            Point right = getPoint(m_maxX, 0);
            m_graphics2D.drawLine(left.x, left.y, right.x, right.y);
        }
        m_graphics2D.setColor(Color.BLACK);
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(0);
        format.setGroupingUsed(false);
        for (double x = m_xTicsMin; x < m_maxX; x += 2 * m_xTics)
        {
            Point bottom = getPoint(x, m_minY);
            Point top = getPoint(x, m_maxY);
            drawString(format.format(x), bottom.x,
                       m_bottom + (m_imgHeight - m_bottom) / 2);
        }
        DecimalFormat format2 = new DecimalFormat();
        format2.setMaximumFractionDigits(m_precision);
        format2.setGroupingUsed(false);
        for (double y = m_yTicsMin; y < m_maxY; y += m_yTics)
        {
            Point point = getPoint(m_minX, y);
            String label;
            if (m_onlyIntValues)
                label = format.format(y);
            else
                label = format2.format(y);
            drawStringRightAlign(label, m_left - 5, point.y);
        }
    }

    private void drawString(String string, int x, int y)
    {
        FontMetrics metrics = m_graphics2D.getFontMetrics();
        int width = metrics.stringWidth(string);
        int height = metrics.getAscent();
        m_graphics2D.drawString(string, x - width / 2, y + height / 2);
    }

    private void drawStringRightAlign(String string, int x, int y)
    {
        FontMetrics metrics = m_graphics2D.getFontMetrics();
        int width = metrics.stringWidth(string);
        int height = metrics.getAscent();
        m_graphics2D.drawString(string, x - width, y + height / 2);
    }

    private Point getPoint(double x, double y)
    {
        int intX = (int)(m_left + (x - m_minX) / m_xRange * m_width);
        int intY = (int)(m_bottom - (y - m_minY) / m_yRange * m_height);
        return new Point(intX, intY);
    }

    private double getTics(double range, int numberTicsHint)
    {
        double tics = range / numberTicsHint;
        if (tics < 0.5)
        {
            double result = 0.5;
            while (result / 5 > tics)
            {
                result /= 5;
                if (result / 2 > tics)
                    break;
                result /= 2;
            }
            return result;
        }
        double result = 0.5;
        while (result < tics)
        {
            result *= 2;
            if (result >= tics)
                break;
            result *= 5;
        }
        return result;
    }

    private double getTicsMin(double tics, double min)
    {
        double result = (int)(min / tics) * tics;
        if (result < min)
            result += tics;
        return result;
    }

    private void initScale(Table table, String columnX, String columnY)
    {
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        m_onlyBoolValues = true;
        m_onlyIntValues = true;
        for (int row = 0; row < table.getNumberRows(); ++row)
        {
            String move = table.get(columnX, row);
            String value = table.get(columnY, row);
            if (value != null && ! value.equals("(null)")
                && ! (value.equals("0") || value.equals("1")))
                m_onlyBoolValues = false;
            try
            {
                Integer.parseInt(value);
            }
            catch (NumberFormatException e)
            {
                m_onlyIntValues = false;
            }
            try
            {
                double x = Double.parseDouble(move);
                double y = Double.parseDouble(value);
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
            catch (NumberFormatException e)
            {
            }
        }
        initScaleX(minX, maxX);
        m_minY = minY;
        m_maxY = maxY;
        if (m_onlyBoolValues)
        {
            m_minY = -0.1;
            m_maxY = 1.1;
        }
        if (m_minY == m_maxY)
        {
            m_minY -= 1;
            m_maxY += 1;
            m_yRange = 2;
        }
        else
        {
            m_yRange = m_maxY - m_minY;
            m_minY = m_minY - 0.05 * (m_maxY - m_minY);
            m_maxY = m_maxY + 0.05 * (m_maxY - m_minY);
            m_yRange *= 1.1;
        }
        if (m_minY > 0 && m_minY < 0.3 * m_maxY)
        {
            m_minY = 0;
        }
        m_yRange = m_maxY - m_minY;
        if (m_onlyBoolValues)
        {
            m_yTics = 1;
            m_yTicsMin = 0;
        }
        else
        {
            m_yTics = getTics(m_yRange, 6);
            if (m_onlyIntValues)
                m_yTics = Math.max(1, m_yTics);
            m_yTicsMin = getTicsMin(m_yTics, m_minY);
        }
    }

    private void initScaleX(double min, double max)
    {
        if (min == max)
        {
            m_minX = min - 1;
            m_maxX = min + 1;
        }
        else
        {
            m_minX = min - 0.02 * (max - min);
            m_maxX = max + 0.02 * (max - min);
        }
        m_xRange = m_maxX - m_minX;
        if (m_autoXTics)
            m_xTics = getTics(m_xRange, 6);
        m_xTicsMin = getTicsMin(m_xTics, m_minX);
    }
}

//----------------------------------------------------------------------------
