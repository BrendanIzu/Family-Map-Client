package edu.byu.cs240.familymap;

import org.junit.Test;

import static org.junit.Assert.*;

import androidx.appcompat.app.ActionBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import model.Event;
import model.Person;
import request.LoginRequest;
import request.RegisterRequest;
import result.EventsResult;
import result.LoginResult;
import result.PersonsResult;
import result.RegisterResult;

public class ServerProxyTest {
    ServerProxy serverProxy = new ServerProxy("localhost", "8080");

    @Test
    public void loginTest() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.username = "sheila";
        loginRequest.password = "parker";

        LoginRequest badRequest = new LoginRequest();
        badRequest.username = "user";
        badRequest.password = "pass";

        // positive test
        LoginResult loginResult = serverProxy.Login(loginRequest);
        assertNotNull(loginResult);
        assertTrue(loginResult.success);
        assertEquals(loginResult.getUsername(), "sheila");

        // negative test
        LoginResult badResult = serverProxy.Login(badRequest);
        assertNotNull(badResult);
        assertFalse(badResult.success);
    }

    @Test
    @ActionBar.DisplayOptions
    public void registerTest() {
        RegisterRequest registerRequest = new RegisterRequest();
        String username = generateString();
        String pass = generateString();

        registerRequest.username = username;
        registerRequest.password = pass;
        registerRequest.gender = "m";
        registerRequest.firstName = "test";
        registerRequest.lastName = "test";
        registerRequest.email = "test";

        // positive test
        RegisterResult registerResult = serverProxy.Register(registerRequest);
        assertNotNull(registerResult);
        assertTrue(registerResult.success);
        assertEquals(registerResult.getUsername(),username);

        // negative test
        registerRequest.username = "sheila";
        registerRequest.password = "parker";
        RegisterResult badResult = serverProxy.Register(registerRequest);

        assertNotNull(badResult);
        assertFalse(badResult.success);
    }


    @Test
    public void getPersonsTest() {
        Person sheila = new Person("Sheila_Parker", "sheila", "Sheila", "Parker", "f", "Blaine_McGary", "Betty_White", "Davis_Hyer");
        Person davis = new Person("Davis_Hyer", "sheila", "Davis", "Hyer", "m", null, null, "Sheila_Parker");
        Person blaine = new Person("Blaine_McGary", "sheila", "Blaine", "McGary", "m", "Ken_Rodham", "Mrs_Rodham", "Betty_White");
        Person betty = new Person("Betty_White", "sheila", "Betty", "White", "f", "Frank_Jones", "Mrs_Jones", "Blaine_McGary");
        Person sus = new Person("there", "is", "an", "i", "m", "poster", "among", "us");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.username = "sheila";
        loginRequest.password = "parker";
        LoginResult loginResult = serverProxy.Login(loginRequest);
        DataCache.getInstance().auth = loginResult.getAuthtoken();

        // positive get persons test

        PersonsResult personsResult = serverProxy.Persons();
        assertNotNull(personsResult);
        assertTrue(personsResult.success);
        assertTrue(checkContainsPerson(personsResult.getData(), sheila));
        assertTrue(checkContainsPerson(personsResult.getData(), davis));
        assertTrue(checkContainsPerson(personsResult.getData(), blaine));
        assertTrue(checkContainsPerson(personsResult.getData(), betty));

        // negative get persons test
        assertFalse(checkContainsPerson(personsResult.getData(), sus));

    }

    @Test
    public void getEventsTest() {
        Event birth = new Event("Sheila_Birth", "sheila", "Sheila_Parker", -36.1833,
                144.9667, "Sheila_Asteroids", "Melbourne", "birth", 1970);
        Event completedAsteroids = new Event("1", "sheila", "brendan", 77.4667,
                -68.7667, "Denmark", "Qaanaaq", "completed asteroids", 2014);
        Event death = new Event("Sheila_Death", "sheila", "Sheila_Parker", 40.2444,
                111.6608, "China", "Hohhot", "death", 2015);
        Event fake = new Event("1", "bubs4444", "brendan", 21.3069,
                157.8583, "United States", "Honolulu", "birth", 2000);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.username = "sheila";
        loginRequest.password = "parker";
        LoginResult loginResult = serverProxy.Login(loginRequest);
        DataCache.getInstance().auth = loginResult.getAuthtoken();

        EventsResult eventsResult = serverProxy.Events();
        assertNotNull(eventsResult);
        assertTrue(checkContainsEvent(eventsResult.getData(), birth));
        assertTrue(checkContainsEvent(eventsResult.getData(), birth));
        assertTrue(checkContainsEvent(eventsResult.getData(), birth));
    }


    private String generateString() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        int length = 10;

        for(int i = 0; i < length; i++) {
            int index = random.nextInt(alphabet.length());
            char randomChar = alphabet.charAt(index);
            sb.append(randomChar);
        }
        return sb.toString();
    }

    // checks a list of events for an event
    private boolean checkContainsEvent(Event[] list, Event check) {
        for(Event e: list) {
            if(check.getEvent_id().equals(e.getEvent_id())) {
                return true;
            }
        }
        return false;
    }

    // checks a list of persons for a person
    private boolean checkContainsPerson(Person[] list, Person check) {
        for(Person p: list) {
            if(check.getPerson_id().equals(p.getPerson_id())) {
                return true;
            }
        }
        return false;
    }
}
