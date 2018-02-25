package de.gotovoid.domain.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.gotovoid.database.model.Recording;
import de.gotovoid.database.model.RecordingEntry;
import de.gotovoid.database.model.RecordingWithEntries;

/**
 * Parses a gpx file into {@link Recording} and a {@link List} of {@link RecordingEntry}s
 * Which will be returned as {@link RecordingWithEntries}.
 * <p>
 * Uses the {@link XmlPullParser} to parse the gpx file.
 * <p>
 * Created by DJ on 04/01/18.
 */
public class GPXParser {
    private static final String TAG = GPXParser.class.getSimpleName();

    /**
     * Parses the {@link InputStream} of a gpx file to create a
     * {@link RecordingWithEntries} of the type {@link Recording.Type#HIKE}.
     * Will return null if the file can not be parsed.
     *
     * @param stream the {@link InputStream} to be used for parsing
     * @return the {@link RecordingWithEntries}.
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Nullable
    public static RecordingWithEntries parseRecording(@NonNull final InputStream stream)
            throws IOException, XmlPullParserException {
        Log.d(TAG, "parseRecording() called with: stream = [" + stream + "]");
        return parseStream(stream, Recording.Type.HIKE);
    }

    /**
     * Parses the {@link InputStream} of a gpx file to create a
     * {@link RecordingWithEntries} of the given {@link Recording.Type}.
     * Will return null if the file can not be parsed.
     *
     * @param stream the {@link InputStream} to be used for parsing
     * @param type   the {@link Recording.Type}
     * @return a new {@link RecordingWithEntries} of the given {@link Recording.Type}
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Nullable
    private static RecordingWithEntries parseStream(final InputStream stream,
                                                    final Recording.Type type)
            throws IOException, XmlPullParserException {
        RecordingWithEntries recording = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(stream, null);
            parser.nextTag();
            recording = readGPXFile(parser, type);
        } finally {
            stream.close();
        }
        return recording;
    }

    /**
     * Uses the {@link XmlPullParser} to create a new {@link RecordingWithEntries}
     * of the given {@link Recording.Type}.
     * Will return null if parsing was not successful.
     *
     * @param parser {@link XmlPullParser} to use for parsing
     * @param type   the {@link Recording.Type} of the recording
     * @return new {@link RecordingWithEntries} if parsing was successful
     * @throws IOException
     * @throws XmlPullParserException
     */
    @NonNull
    private static RecordingWithEntries readGPXFile(@NonNull final XmlPullParser parser,
                                                    @NonNull final Recording.Type type)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "gpx");
        Recording recording = null;
        List<RecordingEntry> entries = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String tagName = parser.getName();
            if (tagName.equals("metadata")) {
                // The metadata tag provides the necessary information for the Recording object.
                recording = readMetadata(parser, type);
            } else if (tagName.equals("trk")) {
                // the trk tags contain the waypoints for the RecordingEntry objects.
                entries.addAll(parseTrack(parser));
            } else {
                // Ignore the other tags.
                skip(parser);
            }
        }
        return new RecordingWithEntries(recording, entries);
    }

    /**
     * Read the metadata of the gpx file using the {@link XmlPullParser} provided.
     * Returns a new {@link Recording} object containing the metadata.
     * Fills with current time if parsing was not successful.
     *
     * @param parser parser to be used
     * @param type   {@link Recording.Type}
     * @return new {@link Recording} object
     * @throws IOException
     * @throws XmlPullParserException
     */
    @NonNull
    private static Recording readMetadata(@NonNull final XmlPullParser parser,
                                          @NonNull final Recording.Type type)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "metadata");
        String name = null;
        Date date = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String tag = parser.getName();

            if (tag.equals("name")) {
                name = parseTrackName(parser);
            } else if (tag.equals("time")) {
                date = parseTime(parser);
            } else {
                skip(parser);
            }
        }
        if (date == null) {
            date = new Date(System.currentTimeMillis());
        }
        Recording recording = new Recording(name,
                type,
                false,
                date.getTime());
        return recording;
    }

    /**
     * Parses the gpx file using the given {@link XmlPullParser} to extract a {@link List} of
     * {@link RecordingEntry}s.
     *
     * @param parser {@link XmlPullParser} to use for parsing
     * @return {@link List} of parsed {@link RecordingEntry}s
     * @throws IOException
     * @throws XmlPullParserException
     */
    @NonNull
    private static List<RecordingEntry> parseTrack(@NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "trk");
        final List<RecordingEntry> entries = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String tagName = parser.getName();
            if (tagName.equals("trkseg")) {
                entries.addAll(parseTrackSegment(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    /**
     * Parses individual track segments using the given {@link XmlPullParser} to extract a
     * {@link List} of {@link RecordingEntry}s for the track segment.
     *
     * @param parser {@link XmlPullParser} to be used for parsing
     * @return {@link List} of {@link RecordingEntry}s for the track segment.
     * @throws IOException
     * @throws XmlPullParserException
     */
    @NonNull
    private static List<RecordingEntry> parseTrackSegment(@NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "trkseg");
        List<RecordingEntry> tracks = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String tagName = parser.getName();
            if (tagName.equals("trkpt")) {
                tracks.add(parseTrackPoint(parser));
            } else {
                skip(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, null, "trkseg");
        return tracks;
    }

    /**
     * Uses the given {@link XmlPullParser} to parse the content of a name tag thus
     * returning the track name.
     * Will return an empty string as name if parsing fails.
     *
     * @param parser {@link XmlPullParser} to be used for parsing
     * @return track name
     * @throws IOException
     * @throws XmlPullParserException
     */
    @NonNull
    private static String parseTrackName(@NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "name");
        String trackName = parseText(parser);
        parser.require(XmlPullParser.END_TAG, null, "name");
        return trackName;
    }

    /**
     * Uses the given {@link XmlPullParser} to parse the content of a time tag thus returning the
     * time as {@link Date} the recording was created.
     * Will return null if parsing fails.
     *
     * @param parser {@link XmlPullParser} to be used for parsing
     * @return the creation time
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Nullable
    private static Date parseTime(@NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "time");
        String dateString = parseText(parser);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault());
        Date result = null;
        try {
            result = format.parse(dateString);
        } catch (final ParseException exception) {
            Log.e(TAG, "parseTime: ", exception);
        }
        parser.require(XmlPullParser.END_TAG, null, "time");
        return result;
    }

    /**
     * Uses the given {@link XmlPullParser} to parse the content of a track point tag and
     * returns a new {@link RecordingEntry} containing the parsed data.
     *
     * @param parser {@link XmlPullParser} to be used for parsing
     * @return {@link RecordingEntry} containing the parsed data
     * @throws IOException
     * @throws XmlPullParserException
     */
    @NonNull
    private static RecordingEntry parseTrackPoint(@NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "trkpt");
        // The latitude and logitude are stored in attributes of the 'trkpt' tag.
        final double latitude = Double.valueOf(parser.getAttributeValue(null, "lat"));
        final double longitude = Double.valueOf(parser.getAttributeValue(null, "lon"));
        int elevation = 0;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String tag = parser.getName();
            if (tag.equals("ele")) {
                // The altitude or elevation is stored in a separate tag
                elevation = parseElevation(parser);
            } else {
                skip(parser);
            }
        }
        return new RecordingEntry(longitude, latitude, elevation);
    }

    /**
     * Uses the given {@link XmlPullParser} to parse a elevation tag of a gpx file.
     *
     * @param parser the {@link XmlPullParser} to be used for parsing
     * @return the elevation
     * @throws IOException
     * @throws XmlPullParserException
     */
    private static int parseElevation(@NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "ele");
        final int elevation = Double.valueOf(parseText(parser)).intValue();
        parser.require(XmlPullParser.END_TAG, null, "ele");
        return elevation;
    }

    /**
     * Uses the given {@link XmlPullParser} to extract text between an opening and closing tag.
     *
     * @param parser {@link XmlPullParser} to be used for parsing
     * @return the parsed text
     * @throws IOException
     * @throws XmlPullParserException
     */
    @NonNull
    private static String parseText(@NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    /**
     * Skips the current tag.
     *
     * @param parser {@link XmlPullParser} to be used for parsing
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static void skip(@NonNull final XmlPullParser parser)
            throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalArgumentException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
                default:
                    // Do nothing
            }
        }
    }
}
