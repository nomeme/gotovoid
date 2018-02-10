package gotovoid.de.gotovoid.domain.model;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import gotovoid.de.gotovoid.database.model.Recording;
import gotovoid.de.gotovoid.database.model.RecordingEntry;
import gotovoid.de.gotovoid.database.model.RecordingWithEntries;

/**
 * Created by DJ on 01/02/18.
 */

public class GPXSerializer {
    private static final String TAG = GPXSerializer.class.getSimpleName();

    public static void serializeRecording(final RecordingWithEntries recording)
            throws IOException {
        Log.d(TAG, "serializeRecording() called with: recording = [" + recording + "]");
        if (recording == null) {
            return;
        }
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        serializer.setOutput(writer);

        serializer.startDocument("utf-8", true);
        serializer.startTag(null, "gpx");
        serializer.attribute(null, "xmlns", "http://www.topografix.com/GPX/1/1");
        serializer.attribute(null, "version", "1.1");
        serializer.attribute(null, "creator", "gotovoid.de");
        serializeMetadata(recording.getRecording(), serializer);
        serializeTrack(recording, serializer);
        serializer.endTag(null, "gpx");
        serializer.flush();
        Log.d(TAG, "serializeRecording: " + writer.toString());
    }

    private static void serializeMetadata(final Recording recording,
                                          final XmlSerializer serializer) throws IOException {
        Log.d(TAG, "serializeMetadata() called with: recording = [" + recording + "], serializer = [" + serializer + "]");
        serializer.startTag(null, "metadata");
        serializeName(recording, serializer);
        serializeTime(recording, serializer);
        serializer.endTag(null, "metadata");
    }


    private static void serializeTrack(final RecordingWithEntries recording,
                                       final XmlSerializer serializer) throws IOException {
        serializer.startTag(null, "trk");
        serializeName(recording.getRecording(), serializer);
        serializeTrackSegment(recording.getEntries(), serializer);
        serializer.endTag(null, "trk");
    }

    private static void serializeTrackSegment(final List<RecordingEntry> entries,
                                              final XmlSerializer serializer) throws IOException {
        serializer.startTag(null, "trkseg");
        for (RecordingEntry entry : entries) {
            serializeTrackPoint(entry, serializer);
        }
        serializer.endTag(null, "trkseg");
    }

    private static void serializeTrackPoint(final RecordingEntry entry,
                                            final XmlSerializer serializer) throws IOException {
        serializer.startTag(null, "trkpnt");
        serializer.attribute(null, "lat", String.valueOf(entry.getLatitude()));
        serializer.attribute(null, "lon", String.valueOf(entry.getLongitude()));
        serializeElevation(entry, serializer);
        serializeTime(entry, serializer);
        serializer.endTag(null, "trkpnt");
    }

    private static void serializeElevation(final RecordingEntry entry,
                                           final XmlSerializer serializer) throws IOException {
        serializer.startTag(null, "ele");
        serializer.text(String.valueOf((float)entry.getAltitude()));
        serializer.endTag(null, "ele");
    }

    private static void serializeName(final Recording recording,
                                      final XmlSerializer serializer) throws IOException {
        serializer.startTag(null, "name");
        serializer.text(recording.getName());
        serializer.endTag(null, "name");
    }

    private static void serializeTime(final Recording recording,
                                      final XmlSerializer serializer) throws IOException {
        serializer.startTag(null, "time");
        serializer.text(formatTime(recording.getTimeStamp()));
        serializer.endTag(null, "time");
    }

    private static void serializeTime(final RecordingEntry entry,
                                      final XmlSerializer serializer) throws IOException {
        serializer.startTag(null, "time");
        serializer.text(formatTime(entry.getTimeStamp()));
        serializer.endTag(null, "time");
    }

    private static String formatTime(final long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault());
        return format.format(new Date(timestamp));
    }
}
