package edu.byu.cs240.familymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import model.Event;
import model.Person;

public class SearchActivity extends AppCompatActivity {

    private static final int PERSON_ITEM_VIEW_TYPE = 0;
    private static final int EVENT_ITEM_VIEW_TYPE = 1;
    private static final DataCache dataCache = DataCache.getInstance();
    private static final List<Person> persons = Arrays.asList(dataCache.personsResult.getData());
    private static final List<Event> events = dataCache.getFilteredEvents();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Family Map: Search");
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        MenuItem searchItem = menu.findItem(R.id.actionSearch);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Event> filteredEvents = new ArrayList<>();
                List<Person> filteredPersons = new ArrayList<>();
                DataCache dataCache = DataCache.getInstance();

                if(newText.isEmpty()) {
                    System.out.println("searching emptiness");
                } else {
                    for(Event e: dataCache.getFilteredEvents()) {
                        Person p = DataCache.getInstance().personsMap.get(e.getPerson_id());
                        String eventTest = e.getEvent_type()+" "+e.getCity()+" "+e.getCountry()+" "+e.getYear()+" "+p.getFirst_name()+" "+p.getLast_name();
                        if(eventTest.toLowerCase().contains(newText.toLowerCase(Locale.ROOT))) {
                            filteredEvents.add(e);
                        }
                    }

                    for(Person p: persons) {
                        String personTest = p.getFirst_name()+" "+p.getLast_name();
                        if(personTest.toLowerCase().contains(newText.toLowerCase(Locale.ROOT))) {
                            filteredPersons.add(p);
                        }
                    }
                }

                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                SearchAdapter adapter = new SearchAdapter(filteredPersons, filteredEvents);
                recyclerView.setAdapter(adapter);

                return false;
            }
        });
        return true;
    }

    private class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {
        private List<Person> persons;
        private List<Event> events;

        SearchAdapter(List<Person> persons, List<Event> events) {
            this.persons = persons;
            this.events = events;
        }

        @Override
        public int getItemViewType(int position) {
            return position < persons.size() ? PERSON_ITEM_VIEW_TYPE : EVENT_ITEM_VIEW_TYPE;
        }

        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;

            if(viewType == PERSON_ITEM_VIEW_TYPE) {
                view = getLayoutInflater().inflate(R.layout.person_item, parent, false);
            } else {
                view = getLayoutInflater().inflate(R.layout.event_item, parent, false);
            }
            return new SearchViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            if(position < persons.size()) {
                holder.bind(persons.get(position));
            } else {
                holder.bind(events.get(position - persons.size()));
            }
        }

        @Override
        public int getItemCount() {
            return persons.size() + events.size();
        }

        private android.widget.Filter exampleFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Object> filteredList = new ArrayList<>();

                if(constraint == null || constraint.length() <= 0) {
                    filteredList.addAll(persons);
                    filteredList.addAll(events);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for(Person p: persons) {
                        if(p.toString().toLowerCase().contains(filterPattern)) {
                            filteredList.add(p);
                        }
                    }
                    for(Event e: events) {
                        if(e.toString().toLowerCase().contains(filterPattern)) {
                            filteredList.add(e);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                List<Object> resultList = (List) results.values;

                for(Object item : resultList) {
                    if(item.getClass().equals(Person.class)) {
                        System.out.println(((Person)item).toString());
                        //persons.add((Person)item);
                    } else System.out.println((Event)item); //events.add((Event)item);
                }
                notifyDataSetChanged();
            }
        };
    }

    private class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView heading;
        private final TextView description;
        private final TextView icon;

        private final int viewType;
        private Person person;

        SearchViewHolder(View view, int viewType) {
            super(view);
            this.viewType = viewType;

            itemView.setOnClickListener(this);

            if(viewType == PERSON_ITEM_VIEW_TYPE) {
                heading = itemView.findViewById(R.id.personTitle);
                icon = itemView.findViewById(R.id.personIcon);
                description = null;
            } else {
                heading = itemView.findViewById(R.id.eventTitle);
                description = itemView.findViewById(R.id.eventDescription);
                icon = itemView.findViewById(R.id.eventIcon);
            }
        }

        @SuppressLint("SetTextI18n")
        private void bind(Person p) {
            this.person = p;
            heading.setText(p.getFirst_name()+" "+p.getLast_name());

            if(p.getGender().equals("f")) {
                Drawable[] drawables = icon.getCompoundDrawables();
                drawables[0].setColorFilter(0xffff00ff, PorterDuff.Mode.MULTIPLY);
            }
        }

        @SuppressLint("SetTextI18n")
        private void bind(Event e) {
            //this.event = e;
            Person p = DataCache.getInstance().personsMap.get(e.getPerson_id());

            heading.setText(e.getEvent_type()+": "+e.getCity()+", "+e.getCountry()+" ("+e.getYear()+")");
            description.setText(p.getFirst_name()+" "+p.getLast_name());

            // set the color of the event marker
            int colorPosition;
            if(dataCache.eventColors.get(e.getEvent_type().toLowerCase()) == null) {
                colorPosition = 0;
            } else colorPosition = dataCache.eventColors.get(e.getEvent_type().toLowerCase());
            Drawable[] drawables = icon.getCompoundDrawables();
            drawables[0].setColorFilter(DataCache.colorsAsInts[colorPosition], PorterDuff.Mode.MULTIPLY);
            dataCache.eventColors.get(e.getEvent_type());
        }

        @Override
        public void onClick(View view) {
            if(viewType == PERSON_ITEM_VIEW_TYPE) {
                TextView textview = view.findViewById(R.id.personTitle);
                String personName = textview.getText().toString();
                System.out.println(personName);
                for(Person p: persons) {
                    String testName = p.getFirst_name()+" "+p.getLast_name();
                    if(testName.equals(personName)) {
                        DataCache.getInstance().currPerson = p;
                    }
                }
                startActivity(new Intent(SearchActivity.this, PersonActivity.class));


            } else {
                TextView textview = view.findViewById(R.id.eventTitle);
                String eventString = textview.getText().toString();

                for(Event e1 : events) {
                    String testString = e1.getEvent_type()+": "+ e1.getCity()+", "+ e1.getCountry()+" ("+ e1.getYear()+")";
                    if(eventString.contains(testString)) {
                        System.out.println(testString);
                        DataCache.getInstance().currEvent = e1;
                    }
                }
                startActivity(new Intent(SearchActivity.this, EventActivity.class));
            }
        }
    }
}