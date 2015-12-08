package com.kivsw.forjoggers;

/**
 * Created by ivan on 03.12.15.
 */
public class LineApproximator implements  iApproximator{

    private int N=0;
    private double SumX=0,  //  Xi
            SumY=0,  //  Yi
            SumXY=0, //   Xi*Yi
            SumSqrX=0; //  Xi*Xi
    private double k,a;

    //----------------------------------------
    public LineApproximator()
    {
        k=0;a=0;
    };

    /**
     * Approximaty some points by a line
     * @param aX
     * @param aY
     * @return
     */
    public boolean approximate(double aX[], double aY[])
    {
        N= Math.min(aX.length,aY.length);
        SumX=0;  //  Xi
        SumY=0; //  Yi
        SumXY=0; //   Xi*Yi
        SumSqrX=0; //  Xi*Xi
        for(int i=0;i<N;i++)
        {
            double x=aX[i],y=aY[i];
            SumX +=x;
            SumSqrX+= x * x;
            SumXY += x * y;
            SumY += y;
        };

        double div = SumX*SumX - N*SumSqrX;
        if(div==0)
        {
            return false;
        }

        k = (SumX*SumY - N*SumXY)  / div;
        a = (SumX*SumXY - SumSqrX*SumY)  / div;

        return true;
    };
    /** add one point to the data
     *
     */
    public boolean addPoint(double x, double y)
    {
        SumX +=x;
        SumSqrX+= x * x;
        SumXY += x * y;
        SumY += y;

        double div = SumX*SumX - N*SumSqrX;
        if(div==0)
        {
            return false;
        }

        k = (SumX*SumY - N*SumXY)  / div;
        a = (SumX*SumXY - SumSqrX*SumY)  / div;

        return true;
    }

    /** return approximated function value
     * @param x
     * @return
     */
    public double function(double x)
    {
        return x*k+a;
    }
}
