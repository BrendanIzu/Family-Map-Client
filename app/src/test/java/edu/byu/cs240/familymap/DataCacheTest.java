package edu.byu.cs240.familymap;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.Event;
import model.Person;
import result.EventsResult;
import result.PersonResult;
import result.PersonsResult;

public class DataCacheTest {
    DataCache dataCache = DataCache.getInstance();
    Person sheila = new Person("Sheila_Parker", "sheila", "Sheila", "Parker", "f", "Blaine_McGary", "Betty_White", "Davis_Hyer");
    Person davis = new Person("Davis_Hyer", "sheila", "Davis", "Hyer", "m", null, null, "Sheila_Parker");
    Person blaine = new Person("Blaine_McGary", "sheila", "Blaine", "McGary", "m", "Ken_Rodham", "Mrs_Rodham", "Betty_White");
    Person betty = new Person("Betty_White", "sheila", "Betty", "White", "f", "Frank_Jones", "Mrs_Jones", "Blaine_McGary");
    Person sus = new Person("there", "is", "an", "i", "m", "poster", "among", "us");

    Event birth = new Event("Sheila_Birth", "sheila", "Sheila_Parker", -36.1833,
            144.9667, "Australia", "Melbourne", "birth", 1970);
    Event completedAsteroids = new Event("Sheila_Asteroids", "sheila", "Sheila_Parker", 77.4667,
            -68.7667, "Denmark", "Qaanaaq", "completed asteroids", 2014);
    Event death = new Event("Sheila_Death", "sheila", "Sheila_Parker", 40.2444,
            111.6608, "China", "Hohhot", "death", 2015);
    Event blaine_birth = new Event("Blaine_Birth", "sheila", "Blaine_McGary", -36.1833,
            144.9667, "United States", "Provo", "birth", 1776);
    Event betty_birth = new Event("Betty_Birth", "sheila", "Betty_White", -36.1833,
            144.9667, "United States", "Provo", "birth", 1776);
    Event fake = new Event("1", "bubs4444", "brendan", 21.3069,
            157.8583, "United States", "Honolulu", "birth", 2000);

    ArrayList<Person> persons = new ArrayList<>();
    ArrayList<Event> events = new ArrayList<>();

    @Test
    public void familyRelationsTest() throws InterruptedException {
        persons.add(sheila);
        persons.add(davis);
        persons.add(blaine);
        persons.add(betty);

        events.add(birth);
        events.add(completedAsteroids);
        events.add(death);
        events.add(blaine_birth);

        PersonResult personResult = new PersonResult("sheila", "Sheila_Parker", "Sheila", "Parker", "f", "Blaine_McGary", "Betty_White", "Davis_Hyer", true);
        PersonsResult personsResult = new PersonsResult(persons);
        EventsResult eventsResult = new EventsResult(events);

        dataCache.clearAll();
        dataCache.personResult = personResult;
        dataCache.personsResult = personsResult;
        dataCache.eventsResult = eventsResult;
        dataCache.addAll();

        // positive test
        assertTrue(dataCache.fatherPersons.containsKey(sheila.getFather_id()));
        assertTrue(dataCache.motherPersons.containsKey(sheila.getMother_id()));

        // negative test
        assertFalse(dataCache.fatherPersons.containsKey(sheila.getMother_id()));
        assertFalse(dataCache.fatherPersons.containsKey(sheila.getMother_id()));
    }

    @Test
    public void filterEventsTest() {
        persons.add(sheila);
        persons.add(davis);
        persons.add(blaine);
        persons.add(betty);

        events.add(birth);
        events.add(completedAsteroids);
        events.add(death);
        events.add(blaine_birth);

        PersonResult personResult = new PersonResult("sheila", "Sheila_Parker", "Sheila", "Parker", "f", "Blaine_McGary", "Betty_White", "Davis_Hyer", true);
        PersonsResult personsResult = new PersonsResult(persons);
        EventsResult eventsResult = new EventsResult(events);

        dataCache.clearAll();
        dataCache.personResult = personResult;
        dataCache.personsResult = personsResult;
        dataCache.eventsResult = eventsResult;
        dataCache.addAll();

        // positive and negative male test
        assertTrue(dataCache.males.containsKey(blaine.getPerson_id()));
        dataCache.isMaleEvents = false;
        assertFalse(checkContainsEvent(dataCache.getFilteredEvents(), blaine_birth));
        dataCache.isMaleEvents = true;

        // positive and negative female test
        assertTrue(dataCache.females.containsKey(sheila.getPerson_id()));
        dataCache.isFemaleEvents = false;
        assertFalse(checkContainsEvent(dataCache.getFilteredEvents(), birth));
        dataCache.isMaleEvents = true;

        // positive and negative father's side test
        assertTrue(dataCache.fatherPersons.containsKey(sheila.getFather_id()));
        dataCache.isFathersSide = false;
        assertFalse(checkContainsEvent(dataCache.getFilteredEvents(), blaine_birth));
        dataCache.isMaleEvents = true;

        // positive and negative mother's side test
        assertFalse(dataCache.fatherPersons.containsKey(sheila.getMother_id()));
        dataCache.isMothersSide = false;
        assertFalse(checkContainsEvent(dataCache.getFilteredEvents(), betty_birth));
        dataCache.isMaleEvents = true;
    }

    @Test
    public void eventsSortTest() {
        persons.add(sheila);
        persons.add(davis);
        persons.add(blaine);
        persons.add(betty);

        events.add(birth);
        events.add(completedAsteroids);
        events.add(death);
        events.add(blaine_birth);

        PersonResult personResult = new PersonResult("sheila", "Sheila_Parker", "Sheila", "Parker", "f", "Blaine_McGary", "Betty_White", "Davis_Hyer", true);
        PersonsResult personsResult = new PersonsResult(persons);
        EventsResult eventsResult = new EventsResult(events);

        dataCache.clearAll();
        dataCache.personResult = personResult;
        dataCache.personsResult = personsResult;
        dataCache.eventsResult = eventsResult;
        dataCache.currPerson = sheila;
        dataCache.addAll();

        List<Event> orderedEvents = dataCache.updateEvents();

        //sort events positive test
        assertTrue(checkContainsEvent(orderedEvents, birth));
        assertTrue(checkContainsEvent(orderedEvents, completedAsteroids));
        assertTrue(checkContainsEvent(orderedEvents, death));

        System.out.println(orderedEvents);
        assertEquals(orderedEvents.get(0), birth);
        assertEquals(orderedEvents.get(1), completedAsteroids);
        assertEquals(orderedEvents.get(2), death);

        // sort events negative test
        assertNotEquals(orderedEvents.get(2), birth);
        assertFalse(checkContainsEvent(orderedEvents, fake));
    }

    @Test
    public void searchTest() {
        persons.add(sheila);
        persons.add(davis);
        persons.add(blaine);
        persons.add(betty);

        events.add(birth);
        events.add(completedAsteroids);
        events.add(death);
        events.add(blaine_birth);

        PersonResult personResult = new PersonResult("sheila", "Sheila_Parker", "Sheila", "Parker", "f", "Blaine_McGary", "Betty_White", "Davis_Hyer", true);
        PersonsResult personsResult = new PersonsResult(persons);
        EventsResult eventsResult = new EventsResult(events);

        dataCache.clearAll();
        dataCache.personResult = personResult;
        dataCache.personsResult = personsResult;
        dataCache.eventsResult = eventsResult;
        dataCache.addAll();

        // search events positive test
        List<Event> events = dataCache.eventsList;

        assertTrue(dataCache.eventsMap.containsKey(sheila.getPerson_id()));
        assertTrue(dataCache.eventsMap.containsKey(blaine.getPerson_id()));

        assertTrue(checkContainsEvent(events, birth));
        assertTrue(checkContainsEvent(events, completedAsteroids));
        assertTrue(checkContainsEvent(events, death));

        // search events negative test
        assertFalse(dataCache.eventsMap.containsKey(fake.getEvent_id()));
    }

    // checks a list of events for an event
    private boolean checkContainsEvent(List<Event> list, Event check) {
        for(Event e: list) {
            if(check.equals(e)) {
                return true;
            }
        }
        return false;
    }
}
