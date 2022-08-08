package edu.byu.cs240.familymap;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.*;

import model.*;
import result.*;

public class DataCache {
    private static DataCache instance = new DataCache();
    public static final float[] colors = {BitmapDescriptorFactory.HUE_BLUE,
            BitmapDescriptorFactory.HUE_GREEN,
            BitmapDescriptorFactory.HUE_RED,
            BitmapDescriptorFactory.HUE_YELLOW,
            BitmapDescriptorFactory.HUE_AZURE,
            BitmapDescriptorFactory.HUE_CYAN,
            BitmapDescriptorFactory.HUE_MAGENTA,
            BitmapDescriptorFactory.HUE_ORANGE,
            BitmapDescriptorFactory.HUE_ROSE,
            BitmapDescriptorFactory.HUE_VIOLET};
    public static final int[] colorsAsInts = { 0xff003aff, 0xff03db04, 0xffff0000, 0xfffff400, 0xff007fff, 0xff00ffff, 0xffff00ff, 0xffffa500, 0xff007f, 0xff8f00ff };

    //<EventType, position in color wheel>
    public Map<String, Integer> eventColors = new HashMap<>();

    boolean started = false;

    String host;
    String port;
    String personID;
    String username;
    String auth;

    Person currPerson;
    Event currEvent;

    PersonResult personResult;
    PersonsResult personsResult;
    EventsResult eventsResult;
    LoginResult loginResult;
    RegisterResult registerResult;

    // Settings booleans
    boolean isLifeStoryLines = true;
    boolean isFamilyTreeLines = true;
    boolean isSpouseLines = true;
    boolean isFathersSide = true;
    boolean isMothersSide = true;
    boolean isMaleEvents = true;
    boolean isFemaleEvents = true;


    // maps with everything in it
    Map<String, Person> personsMap = new HashMap<>();
    Map<String, Event> eventsMap = new HashMap<>();

    // lists with everything in it
    List<Person> personsList = new ArrayList<>();
    List<Event> eventsList = new ArrayList<>();

    // maps for the settings
    Map<String, Person> motherPersons = new HashMap<>();
    Map<String, Person> fatherPersons = new HashMap<>();
    Map<String, Person> males = new HashMap<>();
    Map<String, Person> females = new HashMap<>();

    // Store the persons and events relevant to the current person
    Map<String, Person> currPersons = new HashMap<>();
    Map<String, Event> currEvents = new HashMap<>();

    // Lists of ordered things
    List<String> orderedPersonsRelationships = new ArrayList<>();
    List<Person> orderedPersons = new ArrayList<>();
    List<Event> orderedEvents = new ArrayList<>();

    public static DataCache getInstance() {
        return instance;
    }

    private DataCache(){}

    public void addAll() {
        for(Person p: personsResult.getData()) {
            personsMap.put(p.getPerson_id(), p);
            personsList.add(p);
        }
        for(Event e: eventsResult.getData()) {
            eventsMap.put(e.getPerson_id(), e);
            eventsList.add(e);
        }
        calculateTrees();
    }

    public List<Event> updateEvents() {
        currEvents.clear();
        for(Event e: eventsList) {
            if(e.getPerson_id().equals(currPerson.getPerson_id())) {
                String eventKey = e.getEvent_type();
                currEvents.put(eventKey, e);
            }
        }
        orderedEvents.clear();
        reorderEvents(new ArrayList<>(currEvents.values()));
        return orderedEvents;
    } private void reorderEvents(List<Event> currList) {
        int earliest = 999999999;
        int position = 0;

        if(currList.size() != 0) {
            for(int i=0; i<currList.size(); i++) {
                if(currList.get(i).getYear() <= earliest) {
                    earliest = currList.get(i).getYear();
                    position = i;
                }
            }
            orderedEvents.add(currList.get(position));
            currList.remove(position);
            reorderEvents(currList);
        }
    }

    public void clearAll() {
        instance = new DataCache();
    }

    // update persons to be the persons associated with the current person
    public List<Person> updatePersons() {
        currPersons.clear();
        for(Person p: personsResult.getData()) {
            if(p.getPerson_id().equals(currPerson.getFather_id())) {
                currPersons.put("Father", p);
            }
            if(p.getPerson_id().equals(currPerson.getMother_id())) {
                currPersons.put("Mother", p);
            }
            if(p.getPerson_id().equals(currPerson.getSpouse_id())) {
                currPersons.put("Spouse", p);
            }

            // LOGIC NOTE: we only need to check if they have at least one non-null parent because the family generator will not generate a single parent
            if(p.getFather_id() != null) {
                if(p.getFather_id().equals(currPerson.getPerson_id()) ||
                    p.getMother_id().equals(currPerson.getPerson_id())) {
                    currPersons.put("Child", p);
                }
            }
        }
        reorderPersons();
        return orderedPersons;
    } private void reorderPersons() {
        // TODO: we need the people in this order: Father, Mother, Spouse, Child
        orderedPersonsRelationships.clear();
        orderedPersons.clear();
        if(currPersons.containsKey("Father")) {
            orderedPersonsRelationships.add("Father");
            orderedPersons.add(currPersons.get("Father"));
        }
        if(currPersons.containsKey("Mother")) {
            orderedPersonsRelationships.add("Mother");
            orderedPersons.add(currPersons.get("Mother"));
        }
        if(currPersons.containsKey("Spouse")) {
            orderedPersonsRelationships.add("Spouse");
            orderedPersons.add(currPersons.get("Spouse"));
        }
        if(currPersons.containsKey("Child")) {
            orderedPersonsRelationships.add("Child");
            orderedPersons.add(currPersons.get("Child"));
        }
    }

    // These are run at the start of the program and never again
    private void calculateTrees() {
        for(Person p: personsResult.getData()) {
            if(p.getGender().equals("m")) {
                males.put(p.getPerson_id(), p);
            } else {
                females.put(p.getPerson_id(), p);
            }

            if(p.getPerson_id().equals(personID)) {
                System.out.println("This is real, this is me");
            } else {
                System.out.println(p.getFirst_name()+" "+p.getLast_name());
                if(treesHelper(p).equals("fatherSide")) {
                    fatherPersons.put(p.getPerson_id(), p);
                }
                if(treesHelper(p).equals("motherSide")) {
                    motherPersons.put(p.getPerson_id(), p);
                }
            }
        }
    } private String treesHelper(Person p) {
        System.out.println(p.getFirst_name()+" "+p.getLast_name());
        if(p.getPerson_id().equals(personResult.getFatherID())) {
            return "fatherSide";
        }
        if(p.getPerson_id().equals(personResult.getMotherID())) {
            return "motherSide";
        }
        else {
            for(Person child: personsResult.getData()) {
                if(child.getFather_id() == null) {
                    System.out.println("he's null");
                }
                else if(child.getFather_id().equals(p.getPerson_id())) {
                    return treesHelper(child);
                }
                else if(child.getMother_id().equals(p.getPerson_id())) {
                    return treesHelper(child);
                }
            }
        }
        return "error";
    }

    public Event getEarliestEvent(Person person) {
        int earliestYear = 999999999;
        Event earliestEvent = null;
        for(Event end: eventsResult.getData()) {
            if(end.getPerson_id().equals(person.getPerson_id())) {
                if(end.getYear() < earliestYear) {
                    earliestYear = end.getYear();
                    earliestEvent = end;
                }
            }
        }
        return earliestEvent;
    }

    public List<Event> getFilteredEvents() {
        List<Event> events = new ArrayList<>();
        for(Event e: eventsResult.getData()) {
            if(fatherPersons.containsKey(e.getPerson_id()) && isFathersSide) {
                if(isMaleEvents) {
                    if(males.containsKey(e.getPerson_id())) {
                        events.add(e);
                    }
                }
                if(isFemaleEvents) {
                    if(females.containsKey(e.getPerson_id())) {
                        events.add(e);
                    }
                }
            }
            if(motherPersons.containsKey(e.getPerson_id()) && isMothersSide) {
                if(isMaleEvents) {
                    if(males.containsKey(e.getPerson_id())) {
                        events.add(e);
                    }
                }
                if(isFemaleEvents) {
                    if(females.containsKey(e.getPerson_id())) {
                        events.add(e);
                    }
                }
            }
            if(!motherPersons.containsKey(e.getPerson_id()) && !fatherPersons.containsKey(e.getPerson_id())) {
                if(isMaleEvents) {
                    if(males.containsKey(e.getPerson_id())) {
                        events.add(e);
                    }
                }
                if(isFemaleEvents) {
                    if(females.containsKey(e.getPerson_id())) {
                        events.add(e);
                    }
                }
            }
        }
        return events;
    }


}
