/*
 * STDE - State Transition Diagram Editor
 * 
 * 2011, 2012 Jan Montag, Andreas Schwenk
 * 
 * Component:   AdditionalMath
 * Class:       AdditionalMath
 * Created:     2011-11-09
 */
package AdditionalMath;

import java.awt.Point;
import java.awt.Polygon;

/**
 * provides mathematic-functions (possibly?) not provided in the java-SDK
 * @author Andreas Schwenk
 */
public class AdditionalMath
{
    // *** ATTRIBUTES ***

    // *** METHODS ***
    /**
     * calculates the binomial coefficient of n over k
     * 
     * @param n
     * @param k
     * @return binomial coefficient
     * @author Andreas Schwenk
     */
    public static double binomialCoeff(double n, double k)
    {
        /* http://en.wikipedia.org/wiki/Binomial_coefficient */
        /* multiplicative form:   product from i=1 to k ((n-(k-i))/(i)) */
        double result = 1.0;
        for(double i=1; i<=k; i++)
            result *= n - (k-i);
        for(double i=1; i<=k; i++)
            result /= i;
        return result;
        /* note: usage of >two< "for-loops" avoids numerical instability.
         This is acceptable since k won't be very large */
    }

    /**
     * creates a Bezier-curve given an array of control points and a number of
     * slices. "Polygon" is used as return-type, but the result is a none-closed
     * line. 
     * The "curve" will be an approximation of short line segments.
     * note: algorithm has been derived from:
     *        http://en.wikipedia.org/wiki/Bezier_Curve
     * 
     * @param controlPoints control-points for the bezier curve
     * @param slices number of lines of the resulting poygon
     * @return polygon
     * @author Andreas Schwenk
     */
    public static Polygon createBezierCurve(Point controlPoints[], int slices)
    {
        // array of x and y coordinates 
        int[] xvalues = new int[slices];
        int[] yvalues = new int[slices];

        int n = controlPoints.length - 1;
        double delta_t = 1.0 / (double)(slices-1);

        int i,k;
        double t=0, x, y;

        // for all line-segments
        for(k=0; k<slices; k++)
        {
            x = y = 0.0;
            for(i=0; i<=n; i++)
            {
                x += AdditionalMath.binomialCoeff(n,i)
                        * Math.pow(1.0-t,n-i) * Math.pow(t,i) * (double)controlPoints[i].x;
                y += AdditionalMath.binomialCoeff(n,i)
                        * Math.pow(1.0-t,n-i) * Math.pow(t,i) * (double)controlPoints[i].y;
            }
            x += 0.5; // rounding x
            y += 0.5; // rounding y
            xvalues[k] = (int)x;
            yvalues[k] = (int)y;
            t += delta_t;
        }
        // build a polygon with the arrays of x and y coordinates
        return new Polygon(xvalues, yvalues, slices);
    }
    
    /**
     * calculates the angle between two given two-dimensional points (vertices)
     * 
     * @param p1 first vertex
     * @param p2 second vertex
     * @return angle in radiant
     * @author Andreas Schwenk
     */
    public static double getAngle(Point p1, Point p2)
    {
        double deltaX, deltaY, angle;
        // calculate delta of x and y
        deltaX = p1.x - p2.x;
        deltaY = p1.y - p2.y;
        // distinguish by quadrants
        if(deltaX == 0)
        {
            if(deltaY > 0)
                angle = Math.PI/2.0;
            else
                angle = -Math.PI/2.0;
        }
        // calculate angle by arcus-tangens
        angle = Math.atan(deltaY/deltaX);
        if(deltaX<0)
        {
            if(deltaY>0)
                angle += Math.PI;
            else
                angle -= Math.PI;
        }
        // force angle to be in range [0, 2*PI]
        if(angle < 0)
            angle += 2.0 * Math.PI;
        // return angle
        return angle;
    }

}
