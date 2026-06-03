package vn.edu.vaa.classmanagerdemo.storage;

import android.content.Context;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.models.Student;

public class XmlStudentParser {
    public static List<Student> parseSample(Context context) throws Exception {
        List<Student> list = new ArrayList<>();
        InputStream is = context.getResources().openRawResource(R.raw.students_sample);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(is, "UTF-8");

        Student current = null;
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tag = parser.getName();
                if ("student".equals(tag)) {
                    current = new Student();
                } else if (current != null) {
                    switch (tag) {
                        case "id": current.setId(Integer.parseInt(parser.nextText())); break;
                        case "name": current.setName(parser.nextText()); break;
                        case "className": current.setClassName(parser.nextText()); break;
                        case "email": current.setEmail(parser.nextText()); break;
                        case "phone": current.setPhone(parser.nextText()); break;
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if ("student".equals(parser.getName()) && current != null) {
                    list.add(current);
                    current = null;
                }
            }
            eventType = parser.next();
        }
        is.close();
        return list;
    }
}
