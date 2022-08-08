package edu.byu.cs240.familymap;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import model.Event;
import model.Person;

public class PersonActivity extends AppCompatActivity {
    DataCache dataCache = DataCache.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Family Map: Person Details");

        // 1. update person's name
        String personName = dataCache.currPerson.getFirst_name()+" "+dataCache.currPerson.getLast_name();
        this.findViewById(R.id.personName);
        TextView textView = this.findViewById(R.id.personName);
        textView.setText(personName);
        // 2. update person's gender
        String gender = "Female";
        if(dataCache.currPerson.getGender().equals("m")) {
            gender = "Male";
        }
        this.findViewById(R.id.personGender);
        textView = this.findViewById(R.id.personGender);
        textView.setText(gender);

        ExpandableListView expandableListView = findViewById(R.id.eventsList);
        System.out.print("I WANT TO MAKE MYSELF BELIEVE: "+dataCache.updateEvents());
        expandableListView.setAdapter(new ExpandableListAdapter(dataCache.updateEvents(), dataCache.updatePersons()));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        return true;
    }

    private class ExpandableListAdapter extends BaseExpandableListAdapter {

        private static final int EVENT_POSITION = 0;   // (EVENT)
        private static final int PERSON_POSITION = 1; // (PERSON)

        private final List<Event> events;
        private final List<Person> persons;

        ExpandableListAdapter(List<Event> events, List<Person> persons) {
            this.events = events;
            this.persons = persons;
        }

        @Override
        // TELL THE ADAPTER HOW MANY GROUPS
        public int getGroupCount() {
            return 2;
        }

        @Override
        // TELL THE ADAPTER HOW MANY KIDS PER GROUP
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
                case EVENT_POSITION:
                    return events.size();
                case PERSON_POSITION:
                    return persons.size();
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            switch (groupPosition) {
                case EVENT_POSITION:
                    return "event YEET";
                case PERSON_POSITION:
                    return "person YEET";
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            switch (groupPosition) {
                case EVENT_POSITION:
                    return events.get(childPosition);
                case PERSON_POSITION:
                    return persons.get(childPosition);
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_group, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.listTitle);

            switch (groupPosition) {
                case EVENT_POSITION:
                    titleView.setText("event YEET");
                    break;
                case PERSON_POSITION:
                    titleView.setText("person YEET");
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView;

            switch(groupPosition) {
                case EVENT_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.event_item, parent, false);
                    initializeEventView(itemView, childPosition);

                    break;
                case PERSON_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.person_item, parent, false);
                    initializePersonView(itemView, childPosition);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return itemView;
        }

        @SuppressLint("SetTextI18n")
        private void initializeEventView(View EventItemView, final int childPosition) {
            TextView heading = EventItemView.findViewById(R.id.eventTitle);
            TextView description = EventItemView.findViewById(R.id.eventDescription);
            TextView icon = EventItemView.findViewById(R.id.eventIcon);

            Event e = events.get(childPosition);
            Person p = DataCache.getInstance().personsMap.get(e.getPerson_id());

            int colorPosition = 0;
            if(dataCache.eventColors.get(e.getEvent_type().toLowerCase(Locale.ROOT)) == null) {
                colorPosition = 0;
            } else dataCache.eventColors.get(e.getEvent_type().toLowerCase(Locale.ROOT));
            Drawable[] drawables = icon.getCompoundDrawables();
            drawables[0].setColorFilter(DataCache.colorsAsInts[colorPosition], PorterDuff.Mode.MULTIPLY);
            dataCache.eventColors.get(e.getEvent_type());

            heading.setText(e.getEvent_type()+": "+e.getCity()+", "+e.getCountry()+" ("+e.getYear()+")");
            description.setText(p.getFirst_name()+" "+p.getLast_name());

            EventItemView.setOnClickListener(v -> {
                TextView textview = v.findViewById(R.id.eventTitle);
                System.out.println("we have clicked an event");
                String eventString = textview.getText().toString();

                for(Event e1 : events) {
                    String testString = e1.getEvent_type()+": "+ e1.getCity()+", "+ e1.getCountry()+" ("+ e1.getYear()+")";
                    if(eventString.contains(testString)) {
                        System.out.println(testString);
                        DataCache.getInstance().currEvent = e1;
                    }
                }
                startActivity(new Intent(PersonActivity.this, EventActivity.class));
            });
        }

        @SuppressLint("SetTextI18n")
        private void initializePersonView(View PersonItemView, final int childPosition) {
            TextView heading = PersonItemView.findViewById(R.id.personTitle);
            TextView description = PersonItemView.findViewById(R.id.personDescription);
            TextView icon = PersonItemView.findViewById(R.id.personIcon);

            if(persons.get(childPosition).getGender().equals("f")) {
                Drawable[] drawables = icon.getCompoundDrawables();
                drawables[0].setColorFilter(0xffff00ff, PorterDuff.Mode.MULTIPLY);
            }

            heading.setText(persons.get(childPosition).getFirst_name()+" "+persons.get(childPosition).getLast_name());
            description.setText(DataCache.getInstance().orderedPersonsRelationships.get(childPosition).toString());

            PersonItemView.setOnClickListener(v -> {
                TextView textview = v.findViewById(R.id.personTitle);
                System.out.println("we have clicked a person");
                String personName = textview.getText().toString();
                System.out.println(personName);
                for(Person p: persons) {
                    String testName = p.getFirst_name()+" "+p.getLast_name();
                    if(testName.equals(personName)) {
                        DataCache.getInstance().currPerson = p;
                    }
                }
                startActivity(new Intent(PersonActivity.this, PersonActivity.class));

            });
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}