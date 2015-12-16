package com.kivsw.forjoggers;

import android.location.Location;

import org.xmlpull.v1.XmlSerializer;

import java.io.FileWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * Created by ivan on 16.12.15.
 */
public class GpxConvertor {

    Track track;
    SimpleDateFormat sdf;
    GpxConvertor(Track track)
    {
        this.track = track;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(new SimpleTimeZone(0, ""));
    }

    boolean saveToFile(String fileName)
    {
        boolean res=false;
        try {
            FileWriter writer = new FileWriter(fileName);
            toGPX(writer);
            writer.close();
            res = true;
        }catch(Exception e)
        {
            return false;
        }

        return res;
    }

    void toGPX(Writer writer) throws Exception
    {
        XmlSerializer serializer = android.util.Xml.newSerializer();

        serializer.setOutput(writer);
        serializer.startDocument("UTF-8", Boolean.TRUE);
        serializer.text("\n");

        serializer.startTag("", "gpx");
        serializer.attribute("", "version", "1.1");
        serializer.attribute("", "creator", BuildConfig.APPLICATION_ID+" "+BuildConfig.VERSION_NAME);
        serializer.attribute("", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        serializer.attribute("", "xmlns", "http://www.topografix.com/GPX/1/1");
        serializer.attribute("", "xsi:schemaLocation", "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd");

        serializer.text("\n");

        // writes the times of the start and the stop
        serializer.startTag("", "extensions");
        serializer.startTag("","forJoggers")
                .attribute("", "timeStart", String.valueOf(track.timeStart))
                .attribute("", "timeStop", String.valueOf(track.timeStop));
        serializer.endTag("", "forJoggers");
        serializer.text("\n");
        serializer.endTag("", "extensions");
        serializer.text("\n");

        // writes track
        serializer.startTag("", "trk");
        serializer.startTag("", "trkseg");
        serializer.text("\n");

        for(Location loc:track.getGeoPoints())
        {
            locationToGPX(serializer,loc);
            serializer.text("\n");
        }

        serializer.endTag("","trkseg");
        serializer.endTag("", "trk");

        serializer.endTag("", "gpx");

        serializer.endDocument();
    }

    String timeToStr(long time)
    {
         //<time>2007-10-02T07:54:30Z</time>

        return sdf.format(new Date(time));
    }

    void locationToGPX(XmlSerializer serializer , Location loc) throws Exception
    {
        serializer.startTag("", "trkpt")
                .attribute("","lon", Double.toString(loc.getLongitude()))
                .attribute("", "lat", Double.toString(loc.getLatitude()));

        serializer.text("\n");
        serializer.startTag("", "time");
        serializer.text(timeToStr(loc.getTime()));
        serializer.endTag("", "time");
        serializer.text("\n");

        if(loc.hasAltitude())
        {
         /*   serializer.startTag("", "fix");
            serializer.text("3d");
            serializer.endTag("", "fix");*/

            serializer.startTag("", "geoidheight");
            serializer.text(String.valueOf((int)loc.getAltitude()));
            serializer.endTag("", "geoidheight");
        }
        else
        {
          /*  serializer.startTag("", "fix");
            serializer.text("2d");
            serializer.endTag("", "fix");*/
        }
        serializer.text("\n");
        serializer.endTag("", "trkpt");
    }
}
