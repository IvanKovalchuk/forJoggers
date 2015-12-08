package com.kivsw.forjoggers;

/**
 * Created by ivan on 08.12.15.
 */
public interface iApproximator {
    /**
     * Approximaty some points by a line
     * @param aX
     * @param aY
     * @return
     */
    boolean approximate(double aX[], double aY[]);
    /** return approximated function value
     * @param x
     * @return
     */
    double function(double x);
}
