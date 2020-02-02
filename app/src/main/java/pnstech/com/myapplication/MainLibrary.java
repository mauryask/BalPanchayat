package pnstech.com.myapplication;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Intent;

import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pnstech.myapplication.RecyclerViewAdapter;
import com.pnstech.myapplication.ReturnTags;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;
;

public class MainLibrary extends AppCompatActivity  implements RecyclerViewAdapter.OnItemClickListener{

    private Toolbar mtoolbar;
    private FloatingActionButton message_button;
    private SharedPreferences sharedPreferences;

    public static final String EXTRA_URL = "imageUrl";
    public static final String EXTRA_BOOK_NAME = "bookName";
    public static final String EXTRA_BOOK_AUTHOR = "bookWriter";
    public static final String EXTRA_BOOK_CONTRIBUTER = "bookContributer";
    public static final String EXTRA_BOOK_DATE = "bookDate";
    public static final String EXTRA_BOOK_ID = "bookID";



    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<ReturnTags> mList;
    private RequestQueue requestQueue;


    //notification badge
    private BottomNavigationView bottomNavigationView;
    private View notificationBadge;


    public static String USER_TYPE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_library);

        message_button = (FloatingActionButton) findViewById(R.id.message_button);


        message_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setData(Uri.parse("mailto:"));
                String[] to = {"pnssoftwares7@gmail.com"};
                intent.putExtra(Intent.EXTRA_EMAIL, to);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Have You Any Query Or Suggestion?");
                intent.putExtra(Intent.EXTRA_TEXT, "Write Here....");
                intent.setType("message/rfc822");// this is must
                Intent.createChooser(intent, "Choose Email"); //second argument is optional
                startActivity(intent);
            }
        });

        recyclerView =  findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true); //recycler view dont change its width and height
        recyclerView.setLayoutManager(new LinearLayoutManager((this)));

        mList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);
        parseJson();

        //creating side three dot menu
        mtoolbar = (Toolbar) findViewById(R.id.toolbarx);
        bottomNavigationView = findViewById(R.id.navigation_view);

        mtoolbar.inflateMenu(R.menu.menu_main);

        //test

        mtoolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.about:
                        startActivity(new Intent(MainLibrary.this, About.class));
                        break;

                        }
                return true;
            }
        });


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.notify:
                        removeBadge();
                        notificationBadge.setVisibility(GONE);
                        startActivity(new Intent(MainLibrary.this, pnstech.com.myapplication.Notification.class));
                        break;

                    case R.id.home:
                        startActivity(new Intent(MainLibrary.this, DashBoard.class));
                        break;

                    case R.id.settings:
                        startActivity(new Intent(MainLibrary.this, Settings.class));
                        break;

                    case R.id.search:
                        startActivity(new Intent(MainLibrary.this, SearchActivity.class));
                        break;
                }
                return true;
            }
            });

        setUpToolbarMenu(); //enabling three dot menu
       showBadge();


       // getting user type
        sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE);

        USER_TYPE = sharedPreferences.getString("userType","");
    }


    public void showBadge() //show notfication badge
    {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(0);
        notificationBadge = LayoutInflater.from(this).inflate(R.layout.notification_badge, menuView,false);

        sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE);
        String notifyCount = sharedPreferences.getString("notifyCount", "");

        TextView badge_count =  notificationBadge.findViewById(R.id.notify_count);

        if(!notifyCount.equals("0"))
            badge_count.setText(notifyCount);

        else
            notificationBadge.setVisibility(GONE);

        itemView.addView(notificationBadge);
    }

    public void removeBadge()
    {

        String url = "https://www.iamannitian.co.in/test/remove_badge.php";
        StringRequest sr = new StringRequest(1, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if(response.equals("1")) {

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("notifyCount",Integer.toString(0));
                            editor.apply();
                            notificationBadge.setVisibility(GONE);

                        }
                    }
                }, new Response.ErrorListener() { //error
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map =  new HashMap<>();
                map.put("idKey",sharedPreferences.getString("userId",""));
                return map;
            }
        };

        RequestQueue rq = Volley.newRequestQueue(MainLibrary.this);
        rq.add(sr);
    }



    private void parseJson()
    {
        String url_n = "https://iamannitian.co.in/test/recycler_view.php";
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url_n, null,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("book_data");

                           for(int i=0; i<jsonArray.length(); i++)
                            {
                                JSONObject data = jsonArray.getJSONObject(i);

                                String bookIdx = data.getString("id");
                                String bookNamex = data.getString("book_name");
                                String writerNamex = data.getString("writer_name");
                                String contributerNamex = data.getString("contributer_name");
                                String datex = data.getString("datex");
                                String urlx = "https://iamannitian.co.in/test/book_covers/"+data.getString("url");

                                mList.add(new ReturnTags(urlx, bookNamex, writerNamex, contributerNamex, datex, bookIdx));
                            }

                            recyclerViewAdapter = new RecyclerViewAdapter(MainLibrary.this,mList);
                            recyclerView.setAdapter(recyclerViewAdapter);

                            recyclerViewAdapter.setOnItemClickListener(MainLibrary.this);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    }, new Response.ErrorListener(){
                    @Override

                    public void onErrorResponse(VolleyError error)
                    {
                        error.printStackTrace();
                    }
                         });

          requestQueue.add(request);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main,menu);

        //remaining methods
        return super.onCreateOptionsMenu(menu);

    }

    //setting side three dot menu
    private void setUpToolbarMenu()
    {
        mtoolbar = findViewById(R.id.toolbarx);
        mtoolbar.setTitle("Library");
    }

    @Override
    public void onItemClick(int position) {
        Intent intent =  new Intent(this, OnBookClick.class);
        ReturnTags clickedItem =  mList.get(position);
        intent.putExtra(EXTRA_URL, clickedItem.getImageUrl());
        intent.putExtra(EXTRA_BOOK_NAME, clickedItem.getBookName());
        intent.putExtra(EXTRA_BOOK_AUTHOR, clickedItem.getBookWriter());
        intent.putExtra(EXTRA_BOOK_CONTRIBUTER, clickedItem.getBookContributer());
        intent.putExtra(EXTRA_BOOK_DATE, clickedItem.getBookDate());
        intent.putExtra(EXTRA_BOOK_ID, clickedItem.getbookId());
        startActivity(intent);
    }


}
