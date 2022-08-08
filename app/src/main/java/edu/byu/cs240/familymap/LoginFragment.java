package edu.byu.cs240.familymap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.DeadSystemException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import request.*;
import result.*;

public class LoginFragment extends Fragment {
    private Listener listener;
    private View view;

    private EditText etHost;
    private EditText etPort;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etEmail;
    private EditText etFirstName;
    private EditText etLastName;
    private RadioGroup rbGender;

    private Button registerBtn;
    private Button loginBtn;
    public boolean filled;

    public interface Listener {
        void openMap();
    }
    public void registerListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @SuppressLint("CutPasteId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);

        // get field references
        this.etHost = view.findViewById(R.id.hostField);
        this.etPort = view.findViewById(R.id.portField);
        this.etUsername = view.findViewById(R.id.usernameField);
        this.etPassword = view.findViewById(R.id.passwordField);
        this.etEmail = view.findViewById(R.id.emailField);
        this.etFirstName = view.findViewById(R.id.firstNameField);
        this.etLastName = view.findViewById(R.id.lastNameField);
        this.rbGender = view.findViewById(R.id.gender);
        this.loginBtn = view.findViewById(R.id.signIn_button);
        this.registerBtn = view.findViewById(R.id.register_button);
        this.filled = false;

        // add watchers for login
        etHost.addTextChangedListener(LoginTextWatcher);
        etPort.addTextChangedListener(LoginTextWatcher);
        etUsername.addTextChangedListener(LoginTextWatcher);
        etPassword.addTextChangedListener(LoginTextWatcher);

        // add watchers for register
        etHost.addTextChangedListener(RegisterTextWatcher);
        etPort.addTextChangedListener(RegisterTextWatcher);
        etUsername.addTextChangedListener(RegisterTextWatcher);
        etPassword.addTextChangedListener(RegisterTextWatcher);
        etEmail.addTextChangedListener(RegisterTextWatcher);
        etFirstName.addTextChangedListener(RegisterTextWatcher);
        etLastName.addTextChangedListener(RegisterTextWatcher);
        rbGender.setOnCheckedChangeListener((group, checkedId) -> {
            String host = etHost.getText().toString().trim();
            String port = etPort.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();

            if(!host.isEmpty() && !port.isEmpty() && !username.isEmpty() && !password.isEmpty()
                    && !email.isEmpty() && !firstName.isEmpty() && !lastName.isEmpty()) {
                registerBtn.setEnabled(true);
            } else registerBtn.setEnabled(false);
        });

        // SIGN IN BUTTON
        Button signIn = view.findViewById(R.id.signIn_button);
        signIn.setOnClickListener(v -> {
            System.out.println("Sign in button has been clicked");

            // grab server information from text fields
            String host = ((EditText) view.findViewById(R.id.hostField)).getText().toString();
            String port = ((EditText) view.findViewById(R.id.portField)).getText().toString();

            // initialize new LoginRequest
            // TODO: create new constructor for LoginRequest
            LoginRequest request = new LoginRequest();
            request.username = ((EditText) view.findViewById(R.id.usernameField)).getText().toString();
            request.password = ((EditText) view.findViewById(R.id.passwordField)).getText().toString();

            // initialize and execute new LoginTask
            LoginTask task = new LoginTask(LoginHandler, host, port, request);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(task);
        });

        // REGISTER BUTTON
        Button register = view.findViewById(R.id.register_button);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Register button has been clicked");

                // grab sever information from text fields
                String host = ((EditText) view.findViewById(R.id.hostField)).getText().toString();
                String port = ((EditText) view.findViewById(R.id.portField)).getText().toString();

                // function to grab gender from radio buttons
                String gender;
                if(((RadioButton)(view.findViewById(R.id.male))).isChecked()) {
                    gender = "m";
                } else gender = "f";

                // initialize new RegisterRequest
                // TODO: create default new constructor for a RegisterRequest
                RegisterRequest request = new RegisterRequest();
                request.username = ((EditText) view.findViewById(R.id.usernameField)).getText().toString();
                request.password = ((EditText) view.findViewById(R.id.passwordField)).getText().toString();
                request.email = ((EditText) view.findViewById(R.id.emailField)).getText().toString();
                request.firstName = ((EditText) view.findViewById(R.id.firstNameField)).getText().toString();
                request.lastName = ((EditText) view.findViewById(R.id.lastNameField)).getText().toString();
                request.gender = gender;

                // initialize and execute new RegisterTask
                RegisterTask task = new RegisterTask(RegisterHandler, host, port, request);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(task);
            }
        });
        return view;
    }

    Handler LoginHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            System.out.println("Creating new LoginHandler");
            Bundle bundle = msg.getData();
            if(bundle.getBoolean("success")) {
                System.out.println("we got a good LoginResult");
                System.out.println(bundle.getString("auth"));

                DataTask task = new DataTask(DataHandler, DataCache.getInstance().host, DataCache.getInstance().port);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(task);
            } else {
                Context context = getContext();
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, "ERROR registering user", duration);
                toast.show();
            }
            // keep going in here
            // get all people from the person
            // do toast
            // call second task pass in second handler as parameter
        }
    };
    Handler DataHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            System.out.println("Creating new data handler");
            Bundle bundle = msg.getData();
            if(bundle.getBoolean("pSuccess") && bundle.getBoolean("eSuccess")) {
                System.out.println("successful data handle");

                // THIS IS FOR SWITCHING FRAGS AND WE WILL NOT NEED IT JUST YET
                if(listener != null) {
                    Context context = getContext();
                    String firstName = DataCache.getInstance().personResult.getFirstName();
                    String lastName = DataCache.getInstance().personResult.getLastName();
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, firstName+" "+lastName, duration);
                    toast.show();
                    System.out.println("we are ready to switch to the next page");
                    listener.openMap();
                }
            }
        }
    };
    Handler RegisterHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            System.out.println("Creating new RegisterHandler");
            Bundle bundle = msg.getData();
            if(bundle.getBoolean("success")) {
                System.out.println("we got a good RegisterResult");

                DataTask task = new DataTask(DataHandler, DataCache.getInstance().host, DataCache.getInstance().port);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(task);
            } else {
                Context context = getContext();
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, "ERROR registering user", duration);
                toast.show();
            }
        }
    };

    public static class LoginTask implements Runnable {
        private final Handler msgHandler;
        private final String host;
        private final String port;
        private final LoginRequest request;

        public LoginTask(Handler msgHandler, String host, String port, LoginRequest request) {
            System.out.println("Creating new LoginTask");
            this.msgHandler = msgHandler;
            this.host = host;
            this.port = port;
            this.request = request;
        }

        @Override
        public void run() {
            // use ServerProxy (which should be okay) and get a LoginResult, use the result
            // to populate a new bundle, put that bundle in a message and then send it to
            // the Handler

            System.out.println("We are attempting to run the login operations");
            ServerProxy sp = new ServerProxy(host, port);
            LoginResult result = sp.Login(request);

            Message msg = Message.obtain();
            Bundle msgBundle = new Bundle();

            msgBundle.putString("auth",result.getAuthtoken());
            msgBundle.putBoolean("success", result.success);

            DataCache.getInstance().loginResult = result;
            DataCache.getInstance().personID = result.getPersonID();
            DataCache.getInstance().username = result.getUsername();
            DataCache.getInstance().auth = result.getAuthtoken();
            DataCache.getInstance().host = host;
            DataCache.getInstance().port = port;

            msg.setData(msgBundle);
            msgHandler.sendMessage(msg);
        }
    }
    public static class DataTask implements Runnable {
        private final Handler msgHandler;
        private final String host;
        private final String port;

        public DataTask(Handler msgHandler, String host, String port) {
            System.out.println("Creating new DataTask");
            this.msgHandler = msgHandler;
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            ServerProxy sp = new ServerProxy(host, port);
            PersonsResult pResult = sp.Persons();
            EventsResult eResult = sp.Events();
            PersonResult result = sp.Person(DataCache.getInstance().personID);

            Message msg = Message.obtain();
            Bundle msgBundle = new Bundle();

            msgBundle.putString("firstName", result.getFirstName());
            msgBundle.putString("lastName", result.getLastName());
            msgBundle.putBoolean("pSuccess", pResult.success);
            msgBundle.putBoolean("eSuccess", eResult.success);

            DataCache.getInstance().personsResult = pResult;
            DataCache.getInstance().eventsResult = eResult;
            DataCache.getInstance().personResult = result;
            DataCache.getInstance().addAll();

            msg.setData(msgBundle);
            msgHandler.sendMessage(msg);
        }
    }
    public static class RegisterTask implements Runnable {
        private final Handler msgHandler;
        private final String host;
        private final String port;
        private final RegisterRequest request;

        public RegisterTask(Handler msgHandler, String host, String port, RegisterRequest request) {
            System.out.println("Creating new RegisterRequest");
            this.msgHandler = msgHandler;
            this.host = host;
            this.port = port;
            this.request = request;
        }

        @Override
        public void run() {
            System.out.println("We are attempting to run the register operations");
            ServerProxy sp = new ServerProxy(host, port);
            RegisterResult result = sp.Register(request);

            Message msg = Message.obtain();
            Bundle msgBundle = new Bundle();

            msgBundle.putString("auth",result.getAuthtoken());
            msgBundle.putBoolean("success", result.success);

            DataCache.getInstance().registerResult = result;
            DataCache.getInstance().personID = result.getPersonID();
            DataCache.getInstance().username = result.getUsername();
            DataCache.getInstance().auth = result.getAuthtoken();
            DataCache.getInstance().host = host;
            DataCache.getInstance().port = port;

            msg.setData(msgBundle);
            msgHandler.sendMessage(msg);
        }
    }

    private final TextWatcher LoginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String host = etHost.getText().toString();
            String port = etPort.getText().toString();
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            if(!host.isEmpty() && !port.isEmpty() &&
                    !username.isEmpty() && !password.isEmpty()) {
                loginBtn.setEnabled(true);
            }
            else loginBtn.setEnabled(false);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private final TextWatcher RegisterTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String host = etHost.getText().toString().trim();
            String port = etPort.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();

            if(!host.isEmpty() && !port.isEmpty() && !username.isEmpty() && !password.isEmpty()
                    && !email.isEmpty() && !firstName.isEmpty() && !lastName.isEmpty()) {
                if(rbGender.getCheckedRadioButtonId() == -1) {
                    registerBtn.setEnabled(false);
                } else registerBtn.setEnabled(true);
            } else registerBtn.setEnabled(false);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}