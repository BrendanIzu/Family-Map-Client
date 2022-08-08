package edu.byu.cs240.familymap;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import model.Event;
import model.Person;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMarkerClickListener {
    private static final int MAX_YEAR = 1000000000;

    private Listener listener;
    private View view;
    private GoogleMap map;
    private DataCache dataCache = DataCache.getInstance();
    List<Polyline> lines = new ArrayList<>();
    Map<String, LatLng> locations = new HashMap<>();

    public interface Listener {
        void openPersonActivity();
    }
    public void registerListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(layoutInflater, container, savedInstanceState);
        view = layoutInflater.inflate(R.layout.fragment_maps, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        
        if(dataCache.currEvent != null) {
            updateEventText(dataCache.currEvent);
        }

        TextView eventTextView = view.findViewById(R.id.eventText);
        eventTextView.setOnClickListener(v -> {
            if(dataCache.currPerson == null) {
                System.out.println("ERROR: please select marker to view an event");
            } else {
                listener.openPersonActivity();
            }
        });
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLoadedCallback(this);
        map.setOnMarkerClickListener(this);
        locations.clear();

        Event[] events = dataCache.eventsResult.getData();

        if(dataCache.currEvent != null) {
            updateEventText(dataCache.currEvent);
            LatLng latLng = new LatLng(dataCache.currEvent.getLatitude(), dataCache.currEvent.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }

        int colorPosition = 0;
        float googleColor;

        for(Event e:events) {
            // 1. we have not seen an event of this type yet
            String test = e.getEvent_type().toLowerCase(Locale.ROOT);
            if (!dataCache.eventColors.containsKey(test)) {
                // make sure the color position isn't too high
                if (colorPosition >= 7) colorPosition = 0;

                // enter <String eventType, Integer color> into the eventColors map, store current color, and cycle to the next color
                dataCache.eventColors.put(e.getEvent_type(), colorPosition);
                googleColor = DataCache.colors[colorPosition];
                colorPosition++;

            } else { // 2. if we have not not seen it that means we have seen it
                googleColor = DataCache.colors[dataCache.eventColors.get(test)];
            }

            // add it to the map
            LatLng latLng = new LatLng(e.getLatitude(), e.getLongitude());
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(latLng).title("TITLE")
                    .icon(BitmapDescriptorFactory.defaultMarker(googleColor)));
            assert marker != null; marker.setTag(e);

            boolean removed = false;
            if(!dataCache.isFathersSide) {
                if(dataCache.fatherPersons.containsKey(e.getPerson_id())) {
                    marker.remove();
                    removed = true;
                }
            }
            if(!dataCache.isMothersSide) {
                if(dataCache.motherPersons.containsKey(e.getPerson_id())) {
                    marker.remove();
                    removed = true;
                }
            }
            if(!dataCache.isMaleEvents) {
                if(dataCache.males.containsKey(e.getPerson_id())) {
                    marker.remove();
                    removed = true;
                }
            }
            if(!dataCache.isFemaleEvents) {
                if(dataCache.females.containsKey(e.getPerson_id())) {
                    marker.remove();
                    removed = true;
                }
            }
            if(!removed) {
                locations.put(e.getEvent_id(), latLng);
            }
        }
        if(dataCache.currEvent != null) {
            updateLines(dataCache.currEvent);
        }
    }

    @Override
    public void onMapLoaded() {
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Event e = (Event)marker.getTag();

        assert e != null;
        LatLng latLng = new LatLng(e.getLatitude(), e.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        updateEventText(e);
        updateLines(e);

        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu, menu);
    }

    @SuppressLint("SetTextI18n")
    public void updateEventText(Event e) {
        String personName = "";
        for(Person p: dataCache.personsResult.getData()) {
            if(p.getPerson_id().equals(e.getPerson_id())) {
                personName = p.getFirst_name()+ " " + p.getLast_name();
                dataCache.currPerson = p;
                dataCache.currEvent = e;
            }
        }

        TextView heading = view.findViewById(R.id.eventText);
        heading.setText(personName+"\n"+e.getEvent_type()+": "+e.getCity()+", "+e.getCountry()+" ("+e.getYear()+")");

        if(dataCache.currPerson.getGender().equals("f")) {
            Drawable[] drawables = heading.getCompoundDrawables();
            drawables[0].setColorFilter(0xffff00ff, PorterDuff.Mode.MULTIPLY);
        } else {
            Drawable[] drawables = heading.getCompoundDrawables();
            drawables[0].setColorFilter(0xff0000ff, PorterDuff.Mode.MULTIPLY);
        }
    }

    public void drawAndRemoveLines(Event start, Event end, float width, int color) {
        LatLng startPoint = new LatLng(start.getLatitude(), start.getLongitude());
        LatLng endPoint = new LatLng(end.getLatitude(), end.getLongitude());

        if(locations.containsKey(end.getEvent_id()) && locations.containsKey(start.getEvent_id())) {
            Polyline line = map.addPolyline(new PolylineOptions()
                    .add(startPoint, endPoint)
                    .width(width).color(color));
            line.setTag(start);
            lines.add(line);
        }
    }

    private void updateLines(Event start) {
        Person person = dataCache.personsMap.get(start.getPerson_id());

        for(Polyline line: lines) {
            line.remove();
        } lines.clear();

        if(dataCache.isSpouseLines) {
            boolean hasSpouse = false;
            for(Person p: dataCache.personsResult.getData()) {
                if(p.getSpouse_id().equals(person.getPerson_id())) {
                    hasSpouse = true;
                }
            }
            if(hasSpouse) {
                Person spouse = dataCache.personsMap.get(person.getSpouse_id());
                Event earliestEvent = dataCache.getEarliestEvent(spouse);
                drawAndRemoveLines(start, earliestEvent, 15, Color.RED);
            }
        }
        if(dataCache.isFamilyTreeLines) {
            updateFamilyLines(start, 30);
        }
        if(dataCache.isLifeStoryLines) {
            updateLifeLines();
            //updateLifeLines(dataCache.getEarliestEvent(person));
        }
    }

    private void updateFamilyLines(Event start, float width) {
        Person person = dataCache.personsMap.get(start.getPerson_id());

        if(person.getFather_id() == null || person.getMother_id() == null) {
            System.out.println("this is the last in a long line of heroes");
        } else {
            Person father = dataCache.personsMap.get(person.getFather_id());
            Person mother = dataCache.personsMap.get(person.getMother_id());

            Event earliestFather = dataCache.getEarliestEvent(father);
            Event earliestMother = dataCache.getEarliestEvent(mother);

            drawAndRemoveLines(start, earliestFather, width, Color.BLUE);
            drawAndRemoveLines(start, earliestMother, width, Color.BLUE);

            updateFamilyLines(earliestFather, width/2);
            updateFamilyLines(earliestMother, width/2);
        }
    }

    public void updateLifeLines() {
        List<Event>events = dataCache.updateEvents();
        if(events.size() > 1) {
            for(int i=0; i<events.size(); i++) {
                if(i == 0) {
                    drawAndRemoveLines(events.get(0), events.get(1), 15, Color.GREEN);
                } else {
                    drawAndRemoveLines(events.get(i-1), events.get(i), 15, Color.GREEN);
                }
            }
        }

//        Person person = dataCache.personsMap.get(start.getPerson_id());
//        Event nextEvent = start;
//        int lowestGap = MAX_YEAR;
//
//        for(Event end: dataCache.eventsList) {
//            if(end.getPerson_id().equals(person.getPerson_id())) {
//                int gap = end.getYear()-nextEvent.getYear();
//                if(gap < lowestGap && gap > 0) {
//                    nextEvent = end;
//                    lowestGap = gap;
//                }
//            }
//        }
//
//        System.out.println("Drawiing: "+start+" to "+nextEvent);
//        drawAndRemoveLines(start, nextEvent, 15, Color.GREEN);
//        if(nextEvent.equals(start)) {
//            System.out.println("We have no more events to check");
//        } else {
//            System.out.println("Drawing: "+start+" to "+nextEvent);
//            drawAndRemoveLines(start, nextEvent, 15, Color.GREEN);
//            updateLifeLines(nextEvent);
//        }
    }
}