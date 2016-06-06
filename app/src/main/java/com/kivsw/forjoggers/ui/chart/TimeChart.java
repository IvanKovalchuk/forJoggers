package com.kivsw.forjoggers.ui.chart;

import android.graphics.Canvas;
import android.graphics.Paint;

import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by ivan on 6/6/16.
 */
public class TimeChart extends  org.achartengine.chart.TimeChart
{
    public TimeChart(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer) {
        super(dataset, renderer);
    }

    protected void drawXLabels(List<Double> xLabels, Double[] xTextLabelLocations, Canvas canvas, Paint paint, int left, int top, int bottom, double xPixelsPerUnit, double minX, double maxX) {
        int length = xLabels.size();
        if(length > 0) {
            boolean showXLabels = this.mRenderer.isShowXLabels();
            boolean showGridY = this.mRenderer.isShowGridY();
            if(showGridY) {
                this.mGridPaint.setStyle(Paint.Style.STROKE);
                this.mGridPaint.setStrokeWidth(this.mRenderer.getGridLineWidth());
            }

            boolean showTickMarks = this.mRenderer.isShowTickMarks();
            DateFormat format = this.getDateFormat(((Double)xLabels.get(0)).doubleValue(), ((Double)xLabels.get(length - 1)).doubleValue());

            for(int i = 0; i < length; ++i) {
                long label = Math.round(((Double)xLabels.get(i)).doubleValue());
                float xLabel = (float)((double)left + xPixelsPerUnit * ((double)label - minX));
                if(showXLabels) {
                    paint.setColor(this.mRenderer.getXLabelsColor());
                    if(showTickMarks) {
                        canvas.drawLine(xLabel, (float)bottom, xLabel, (float)bottom + this.mRenderer.getLabelsTextSize() / 3.0F, paint);
                    }

                    this.drawText(canvas, format.format(new Date(label)), xLabel, (float)bottom + this.mRenderer.getLabelsTextSize() * 4.0F / 3.0F + this.mRenderer.getXLabelsPadding(), paint, this.mRenderer.getXLabelsAngle());
                }

                if(showGridY) {
                    this.mGridPaint.setColor(this.mRenderer.getGridColor(0));
                    canvas.drawLine(xLabel, (float)bottom, xLabel, (float)top, this.mGridPaint);
                }
            }
        }

        this.drawXTextLabels(xTextLabelLocations, canvas, paint, true, left, top, bottom, xPixelsPerUnit, minX, maxX);
    }

    private DateFormat getDateFormat(double start, double end) {

            try {
                SimpleDateFormat format1 = new SimpleDateFormat("HH:mm:ss");
                format1.setTimeZone(TimeZone.getTimeZone("UTC"));

                return format1;
            } catch (Exception var8) {
                ;
            }
        return SimpleDateFormat.getTimeInstance(2);

    }

}
