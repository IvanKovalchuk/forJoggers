package com.kivsw.forjoggers.model.track;

import android.location.Location;

import com.kivsw.forjoggers.BuildConfig;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.SimpleTimeZone;

/** This class save/load a track to/from gpx file
 *  http://www.topografix.com/gpx_manual.asp
 *
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

    public boolean saveToFile(String fileName)
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

    public void toGPX(Writer writer) throws Exception
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
                 .attribute("", "timeStop", String.valueOf(track.timeStop))
                 .attribute("", "activityType", String.valueOf(track.getActivityType()));
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

    //-----------------------------------------------------------------------------------

    public boolean loadFromFile(String fileName)
    {
        boolean res=false;
        try {
            FileReader reader = new FileReader(fileName);
            fromGPX(reader);
            reader.close();
            res = true;
        }catch(Exception e)
        {
            return false;
        }

        return res;
    }

    String[] trackPath={"gpx","trk","trkseg"},
             extentionPath={"gpx", "extensions","forJoggers"};

    ArrayList<String> xmlCurrentPath =new ArrayList<String>(),
              xmlTrackPath=new ArrayList(Arrays.asList(trackPath)),
              xmlExtentionPath=new ArrayList(Arrays.asList(extentionPath));



    public void fromGPX(Reader reader) throws Exception
    {
        track.clear();
        XmlPullParser xpp = org.xmlpull.v1.XmlPullParserFactory.newInstance().newPullParser();// android.util.Xml.newPullParser();
        xmlCurrentPath.clear();

        xpp.setInput(reader);
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch(eventType)
            {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case  XmlPullParser.START_TAG:
                    xmlCurrentPath.add(xpp.getName());

                    if(isTrackSegment())
                        parseTrackSegment(xpp);
                    else if(isExtentionForJoggers())
                    {
                        try {
                          track.timeStart = Long.parseLong(xpp.getAttributeValue("", "timeStart"));
                        }catch(Exception e){}
                        try {
                          track.timeStop = Long.parseLong(xpp.getAttributeValue("", "timeStop"));
                        }catch(Exception e){}
                        try {
                            int a = Integer.parseInt(xpp.getAttributeValue("", "activityType"));
                            track.setActivityType(a);
                        }catch(Exception e){}
                    }

                    break;
                case  XmlPullParser.END_TAG:
                    if(xmlCurrentPath.get(xmlCurrentPath.size()-1).equals(xpp.getName()))
                        xmlCurrentPath.remove(xmlCurrentPath.size()-1);
                    break;
                case XmlPullParser.TEXT:
                    xpp.getText();
                    break;
            }

            eventType = xpp.next();
        }

        xmlCurrentPath.clear();

    }

    private boolean isTrackSegment()
    {
        boolean res=false;
        if(xmlTrackPath.size() <= xmlCurrentPath.size()) {
            res = true;
            for (int i= 0; i < xmlTrackPath.size(); i++) {
                res = res && (xmlTrackPath.get(i).equals(xmlCurrentPath.get(i)));
            };
        }
        return res;

    }
    private boolean isExtentionForJoggers()
    {
        boolean res=false;
        if(xmlExtentionPath.size() <= xmlCurrentPath.size()) {
            res = true;
            for (int i= 0; i < xmlExtentionPath.size(); i++) {
                res = res && (xmlExtentionPath.get(i).equals(xmlCurrentPath.get(i)));
            };
        }
        return res;
    }

    void parseTrackSegment(XmlPullParser xpp) throws Exception
    {
        int eventType;
        Location loc=null;
        String currentTag=null;

        eventType = xpp.next();
        while (isTrackSegment()) {
            switch(eventType)
            {
                case  XmlPullParser.START_TAG:
                    xmlCurrentPath.add(xpp.getName());
                    if(xpp.getName().equals("trkpt"))
                    {
                        loc = new Location("");
                        try {
                            loc.setLongitude(Double.parseDouble(xpp.getAttributeValue("", "lon")));
                        }catch(Exception e) {};
                        try {
                            loc.setLatitude( Double.parseDouble(xpp.getAttributeValue("", "lat")));
                        }catch(Exception e) {};
                    };
                    break;


                case  XmlPullParser.END_TAG:
                    xmlCurrentPath.remove(xmlCurrentPath.size()-1);

                    if(xpp.getName().equals("trkpt"))
                    {
                        //track.addPoint(loc);
                        track.mGeoPoints.add(loc);
                        loc=null;
                    };
                    break;

                case XmlPullParser.TEXT:
                    currentTag = xmlCurrentPath.get(xmlCurrentPath.size()-1);
                    if(currentTag.equals("time")) {
                        loc.setTime(parseTime(xpp.getText()));
                    }
                    else if(currentTag.equals("ele")  ||  currentTag.equals("geoidheight"))
                    {
                        loc.setAltitude( Double.parseDouble(xpp.getText()));
                    }
                    break;
            }

            eventType = xpp.next();
        }
    }

    long parseTime(String timestamp) throws Exception
    {
        return sdf.parse(timestamp).getTime();
    }
}


