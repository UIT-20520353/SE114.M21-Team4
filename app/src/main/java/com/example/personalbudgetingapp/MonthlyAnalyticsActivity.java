package com.example.personalbudgetingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChartView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class MonthlyAnalyticsActivity extends AppCompatActivity {

    private Toolbar settingsToolbar;

    private FirebaseAuth mAuth;
    private String onlineUserId = "";
    private DatabaseReference expensesRef, personalRef;

    private TextView totalBudgetAmountTextView, analyticsTransportAmount, analyticsFoodAmount, analyticsHouseAmount, analyticsEntertainmentAmount, analyticsEducationAmount, analyticsCharityAmount, analyticsApparelAmount, analyticsHealthAmount, analyticsPersonalAmount, analyticsOtherAmount;

    private RelativeLayout relativeLayoutTransport, relativeLayoutFood, relativeLayoutHouse, relativeLayoutEntertainment, relativeLayoutEducation, relativeLayoutCharity, relativeLayoutApparel, relativeLayoutHealth, relativeLayoutPersonal, relativeLayoutOther;

    private PieChart myPieChart;

    private TextView progress_ratio_transport, progress_ratio_food, progress_ratio_house, progress_ratio_entertainment, progress_ratio_education, progress_ratio_charity, progress_ratio_apparel, progress_ratio_health, progress_ratio_personal, progress_ratio_other;
    private ImageView transport_status, food_status, house_status, entertainment_status, education_status, charity_status, apparel_status, health_status, personal_status, other_status;

    private TextView monthRatioSpending, monthSpendAmount;
    private ImageView monthRatioSpending_Image;
    private RelativeLayout linearLayoutAnalysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_analytics);

        settingsToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Monthly Analytics");

        mAuth = FirebaseAuth.getInstance();
        onlineUserId = mAuth.getCurrentUser().getUid();

        expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        personalRef = FirebaseDatabase.getInstance().getReference("personal").child(onlineUserId);

        myPieChart = findViewById(R.id.myPieChart);

        monthSpendAmount = findViewById(R.id.monthSpendAmount);
        monthRatioSpending = findViewById(R.id.monthRatioSpending);
        monthRatioSpending_Image = findViewById(R.id.monthRatioSpending_Image);
        linearLayoutAnalysis = findViewById(R.id.linearLayoutAnalysis);

        // Image View
        transport_status = findViewById(R.id.transport_status);
        food_status = findViewById(R.id.food_status);
        house_status = findViewById(R.id.house_status);
        entertainment_status = findViewById(R.id.entertainment_status);
        education_status = findViewById(R.id.education_status);
        charity_status = findViewById(R.id.charity_status);
        apparel_status = findViewById(R.id.apparel_status);
        health_status = findViewById(R.id.health_status);
        personal_status = findViewById(R.id.personal_status);
        other_status = findViewById(R.id.other_status);

        // Text View
        progress_ratio_transport = findViewById(R.id.progress_ratio_transport);
        progress_ratio_food = findViewById(R.id.progress_ratio_food);
        progress_ratio_house = findViewById(R.id.progress_ratio_house);
        progress_ratio_entertainment = findViewById(R.id.progress_ratio_entertainment);
        progress_ratio_education = findViewById(R.id.progress_ratio_education);
        progress_ratio_charity = findViewById(R.id.progress_ratio_charity);
        progress_ratio_apparel = findViewById(R.id.progress_ratio_apparel);
        progress_ratio_health = findViewById(R.id.progress_ratio_health);
        progress_ratio_personal = findViewById(R.id.progress_ratio_personal);
        progress_ratio_other = findViewById(R.id.progress_ratio_other);

        relativeLayoutTransport = findViewById(R.id.relativeLayoutTransport);
        relativeLayoutFood = findViewById(R.id.relativeLayoutFood);
        relativeLayoutHouse = findViewById(R.id.relativeLayoutHouse);
        relativeLayoutEntertainment = findViewById(R.id.relativeLayoutEntertainment);
        relativeLayoutEducation = findViewById(R.id.relativeLayoutEducation);
        relativeLayoutCharity = findViewById(R.id.relativeLayoutCharity);
        relativeLayoutApparel = findViewById(R.id.relativeLayoutApparel);
        relativeLayoutHealth = findViewById(R.id.relativeLayoutHealth);
        relativeLayoutPersonal = findViewById(R.id.relativeLayoutPersonal);
        relativeLayoutOther = findViewById(R.id.relativeLayoutOther);

        analyticsTransportAmount = findViewById(R.id.analyticsTransportAmount);
        analyticsFoodAmount = findViewById(R.id.analyticsFoodAmount);
        analyticsHouseAmount = findViewById(R.id.analyticsHouseAmount);
        analyticsEntertainmentAmount = findViewById(R.id.analyticsEntertainmentAmount);
        analyticsEducationAmount = findViewById(R.id.analyticsEducationAmount);
        analyticsCharityAmount = findViewById(R.id.analyticsCharityAmount);
        analyticsApparelAmount = findViewById(R.id.analyticsApparelAmount);
        analyticsHealthAmount = findViewById(R.id.analyticsHealthAmount);
        analyticsPersonalAmount = findViewById(R.id.analyticsPersonalAmount);
        analyticsOtherAmount = findViewById(R.id.analyticsOtherAmount);

        totalBudgetAmountTextView = findViewById(R.id.totalAmountSpendOn);

        getTotalWeekTransportExpenses();
        getTotalWeekFoodExpenses();
        getTotalWeekHouseExpenses();
        getTotalWeekEntertainmentExpenses();
        getTotalWeekEducationExpenses();
        getTotalWeekCharityExpenses();
        getTotalWeekApparelExpenses();
        getTotalWeekHealthExpenses();
        getTotalWeekPersonalExpenses();
        getTotalWeekOtherExpenses();
        getTotalMonthSpending();

        new java.util.Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                initPieChart();
                loadGraph();
                setStatusAndImageResource();
            }
        }, 2000);
    }

    private void getTotalWeekTransportExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        Months months = Months.monthsBetween(epoch, now);
        String itemNmonth = "Transport" + months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalAmount = 0;
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsTransportAmount.setText("Spent: " + totalAmount);
                    }
                    personalRef.child("monthTransport").setValue(totalAmount);
                } else {
                    relativeLayoutTransport.setVisibility(View.GONE);
                    personalRef.child("monthTransport").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeekFoodExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        Months months = Months.monthsBetween(epoch, now);
        String itemNmonth = "Food" + months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalAmount = 0;
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsFoodAmount.setText("Spent: " + totalAmount);
                    }
                    personalRef.child("monthFood").setValue(totalAmount);
                } else {
                    relativeLayoutFood.setVisibility(View.GONE);
                    personalRef.child("monthFood").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeekHouseExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        Months months = Months.monthsBetween(epoch, now);
        String itemNmonth = "House" + months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalAmount = 0;
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsHouseAmount.setText("Spent: " + totalAmount);
                    }
                    personalRef.child("monthHouse").setValue(totalAmount);
                } else {
                    relativeLayoutHouse.setVisibility(View.GONE);
                    personalRef.child("monthHouse").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeekEntertainmentExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        Months months = Months.monthsBetween(epoch, now);
        String itemNmonth = "Entertainment" + months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalAmount = 0;
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsEntertainmentAmount.setText("Spent: " + totalAmount);
                    }
                    personalRef.child("monthEntertainment").setValue(totalAmount);
                } else {
                    relativeLayoutEntertainment.setVisibility(View.GONE);
                    personalRef.child("monthEntertainment").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeekEducationExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        Months months = Months.monthsBetween(epoch, now);
        String itemNmonth = "Education" + months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalAmount = 0;
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsEducationAmount.setText("Spent: " + totalAmount);
                    }
                    personalRef.child("monthEducation").setValue(totalAmount);
                } else {
                    relativeLayoutEducation.setVisibility(View.GONE);
                    personalRef.child("monthEducation").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeekCharityExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        Months months = Months.monthsBetween(epoch, now);
        String itemNmonth = "Charity" + months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalAmount = 0;
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsCharityAmount.setText("Spent: " + totalAmount);
                    }
                    personalRef.child("monthCharity").setValue(totalAmount);
                } else {
                    relativeLayoutCharity.setVisibility(View.GONE);
                    personalRef.child("monthCharity").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeekApparelExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        Months months = Months.monthsBetween(epoch, now);
        String itemNmonth = "Apparel" + months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalAmount = 0;
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsApparelAmount.setText("Spent: " + totalAmount);
                    }
                    personalRef.child("monthApparel").setValue(totalAmount);
                } else {
                    relativeLayoutApparel.setVisibility(View.GONE);
                    personalRef.child("monthApparel").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeekHealthExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        Months months = Months.monthsBetween(epoch, now);
        String itemNmonth = "Health" + months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalAmount = 0;
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsHealthAmount.setText("Spent: " + totalAmount);
                    }
                    personalRef.child("monthHealth").setValue(totalAmount);
                } else {
                    relativeLayoutHealth.setVisibility(View.GONE);
                    personalRef.child("monthHealth").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeekPersonalExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        Months months = Months.monthsBetween(epoch, now);
        String itemNmonth = "Personal" + months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalAmount = 0;
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsPersonalAmount.setText("Spent: " + totalAmount);
                    }
                    personalRef.child("monthPersonal").setValue(totalAmount);
                } else {
                    relativeLayoutPersonal.setVisibility(View.GONE);
                    personalRef.child("monthPersonal").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeekOtherExpenses() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        Months months = Months.monthsBetween(epoch, now);
        String itemNmonth = "Other" + months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNmonth").equalTo(itemNmonth);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalAmount = 0;
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsOtherAmount.setText("Spent: " + totalAmount);
                    }
                    personalRef.child("monthOther").setValue(totalAmount);
                } else {
                    relativeLayoutOther.setVisibility(View.GONE);
                    personalRef.child("monthOther").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalMonthSpending() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        Months months = Months.monthsBetween(epoch, now);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("month").equalTo(months.getMonths());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    int totalAmount = 0;
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                    }
                    totalBudgetAmountTextView.setText("Today month's spending: $" + totalAmount);
                    monthSpendAmount.setText("Total spent: $" + totalAmount);
                } else {
                    //totalBudgetAmountTextView.setText("You're not spent today");
                    //anyChartView.setVisibility(View.GONE);
                    myPieChart.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initPieChart(){
        //using percentage as values instead of amount
        myPieChart.setUsePercentValues(true);

        //remove the description label on the lower left corner, default true if not set
        myPieChart.getDescription().setEnabled(false);

        //enabling the user to rotate the chart, default true
        myPieChart.setRotationEnabled(true);
        //adding friction when rotating the pie chart
        myPieChart.setDragDecelerationFrictionCoef(0.9f);
        //setting the first entry start from right hand side, default starting from top
        myPieChart.setRotationAngle(0);

        //highlight the entry when it is tapped, default true if not set
        myPieChart.setHighlightPerTapEnabled(true);
        //adding animation so the entries pop up from 0 degree
        //myPieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        //setting the color of the hole in the middle, default white
        //myPieChart.setHoleColor(Color.parseColor("#000000"));

//        myPieChart.setHoleRadius(0f);
//        myPieChart.setTransparentCircleRadius(0f);

    }

    private void loadGraph() {
        personalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int traTotal;
                    if (snapshot.hasChild("monthTransport")) {
                        traTotal = Integer.parseInt(snapshot.child("monthTransport").getValue().toString());
                    } else {
                        traTotal = 0;
                    }

                    int foodTotal;
                    if (snapshot.hasChild("monthFood")) {
                        foodTotal = Integer.parseInt(snapshot.child("monthFood").getValue().toString());
                    } else {
                        foodTotal = 0;
                    }

                    int houseTotal;
                    if (snapshot.hasChild("monthHouse")) {
                        houseTotal = Integer.parseInt(snapshot.child("monthHouse").getValue().toString());
                    } else {
                        houseTotal = 0;
                    }

                    int entTotal;
                    if (snapshot.hasChild("monthEntertainment")) {
                        entTotal = Integer.parseInt(snapshot.child("monthEntertainment").getValue().toString());
                    } else {
                        entTotal = 0;
                    }

                    int eduTotal;
                    if (snapshot.hasChild("monthEducation")) {
                        eduTotal = Integer.parseInt(snapshot.child("monthEducation").getValue().toString());
                    } else {
                        eduTotal = 0;
                    }

                    int charityTotal;
                    if (snapshot.hasChild("monthCharity")) {
                        charityTotal = Integer.parseInt(snapshot.child("monthCharity").getValue().toString());
                    } else {
                        charityTotal = 0;
                    }

                    int apparelTotal;
                    if (snapshot.hasChild("monthApparel")) {
                        apparelTotal = Integer.parseInt(snapshot.child("monthApparel").getValue().toString());
                    } else {
                        apparelTotal = 0;
                    }

                    int healthTotal;
                    if (snapshot.hasChild("monthHealth")) {
                        healthTotal = Integer.parseInt(snapshot.child("monthHealth").getValue().toString());
                    } else {
                        healthTotal = 0;
                    }

                    int personalTotal;
                    if (snapshot.hasChild("monthPersonal")) {
                        personalTotal = Integer.parseInt(snapshot.child("monthPersonal").getValue().toString());
                    } else {
                        personalTotal = 0;
                    }

                    int otherTotal;
                    if (snapshot.hasChild("monthOther")) {
                        otherTotal = Integer.parseInt(snapshot.child("monthOther").getValue().toString());
                    } else {
                        otherTotal = 0;
                    }


                    ArrayList<PieEntry> pieEntries = new ArrayList<>();
                    String label = "type";

                    Map<String, Integer> typeAmountMap = new HashMap<>();
                    if (traTotal != 0)
                        typeAmountMap.put("Transport",traTotal);
                    if (foodTotal != 0)
                        typeAmountMap.put("Food", foodTotal);
                    if (houseTotal != 0)
                        typeAmountMap.put("House", houseTotal);
                    if (entTotal != 0)
                        typeAmountMap.put("Entertainment", entTotal);
                    if (eduTotal != 0)
                        typeAmountMap.put("Education", eduTotal);
                    if (charityTotal != 0)
                        typeAmountMap.put("Charity", charityTotal);
                    if (apparelTotal != 0)
                        typeAmountMap.put("Apparel", apparelTotal);
                    if (healthTotal != 0)
                        typeAmountMap.put("Health", healthTotal);
                    if (personalTotal != 0)
                        typeAmountMap.put("Personal", personalTotal);
                    if (otherTotal != 0)
                        typeAmountMap.put("Other", otherTotal);

                    ArrayList<Integer> colors = new ArrayList<>();
                    colors.add(Color.parseColor("#304567"));
                    colors.add(Color.parseColor("#309967"));
                    colors.add(Color.parseColor("#476567"));
                    colors.add(Color.parseColor("#890567"));
                    colors.add(Color.parseColor("#a35567"));
                    colors.add(Color.parseColor("#ff5f67"));
                    colors.add(Color.parseColor("#3ca567"));
                    colors.add(Color.parseColor("#ff8800"));
                    colors.add(Color.parseColor("#ef233c"));
                    colors.add(Color.parseColor("#72ddf7"));

                    for(String type: typeAmountMap.keySet()){
                        pieEntries.add(new PieEntry(typeAmountMap.get(type).floatValue(), type));
                    }

                    //collecting the entries with label name
                    PieDataSet pieDataSet = new PieDataSet(pieEntries,label);
                    //setting text size of the value
                    pieDataSet.setValueTextSize(12f);
                    //providing color list for coloring different entries
                    pieDataSet.setColors(colors);
                    //grouping the data set from entry to chart
                    PieData pieData = new PieData(pieDataSet);
                    //showing the value of the entries, default true if not set
                    pieData.setDrawValues(true);

                    pieData.setValueFormatter(new PercentFormatter());

                    myPieChart.setData(pieData);
                    myPieChart.invalidate();
                } else {
                    Toast.makeText(MonthlyAnalyticsActivity.this, "Child does not exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setStatusAndImageResource() {
        personalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    float traTotal;
                    if (snapshot.hasChild("monthTransport")) {
                        traTotal = Integer.parseInt(snapshot.child("monthTransport").getValue().toString());
                    } else {
                        traTotal = 0;
                    }

                    float foodTotal;
                    if (snapshot.hasChild("monthFood")) {
                        foodTotal = Integer.parseInt(snapshot.child("monthFood").getValue().toString());
                    } else {
                        foodTotal = 0;
                    }

                    float houseTotal;
                    if (snapshot.hasChild("monthHouse")) {
                        houseTotal = Integer.parseInt(snapshot.child("monthHouse").getValue().toString());
                    } else {
                        houseTotal = 0;
                    }

                    float entTotal;
                    if (snapshot.hasChild("monthEntertainment")) {
                        entTotal = Integer.parseInt(snapshot.child("monthEntertainment").getValue().toString());
                    } else {
                        entTotal = 0;
                    }

                    float eduTotal;
                    if (snapshot.hasChild("monthEducation")) {
                        eduTotal = Integer.parseInt(snapshot.child("monthEducation").getValue().toString());
                    } else {
                        eduTotal = 0;
                    }

                    float charityTotal;
                    if (snapshot.hasChild("monthCharity")) {
                        charityTotal = Integer.parseInt(snapshot.child("monthCharity").getValue().toString());
                    } else {
                        charityTotal = 0;
                    }

                    float apparelTotal;
                    if (snapshot.hasChild("monthApparel")) {
                        apparelTotal = Integer.parseInt(snapshot.child("monthApparel").getValue().toString());
                    } else {
                        apparelTotal = 0;
                    }

                    float healthTotal;
                    if (snapshot.hasChild("monthHealth")) {
                        healthTotal = Integer.parseInt(snapshot.child("monthHealth").getValue().toString());
                    } else {
                        healthTotal = 0;
                    }

                    float personalTotal;
                    if (snapshot.hasChild("monthPersonal")) {
                        personalTotal = Integer.parseInt(snapshot.child("monthPersonal").getValue().toString());
                    } else {
                        personalTotal = 0;
                    }

                    float otherTotal;
                    if (snapshot.hasChild("monthOther")) {
                        otherTotal = Integer.parseInt(snapshot.child("monthOther").getValue().toString());
                    } else {
                        otherTotal = 0;
                    }

                    float monthTotalSpendAmount;
                    if (snapshot.hasChild("month")) {
                        monthTotalSpendAmount = Integer.parseInt(snapshot.child("month").getValue().toString());
                    } else {
                        monthTotalSpendAmount = 0;
                    }



                    float traRatio;
                    if (snapshot.hasChild("monthTransRatio")) {
                        traRatio = Integer.parseInt(snapshot.child("monthTransRatio").getValue().toString());
                    } else {
                        traRatio = 0;
                    }

                    float foodRatio;
                    if (snapshot.hasChild("monthFoodRatio")) {
                        foodRatio = Integer.parseInt(snapshot.child("monthFoodRatio").getValue().toString());
                    } else {
                        foodRatio = 0;
                    }

                    float houseRatio;
                    if (snapshot.hasChild("monthHouseRatio")) {
                        houseRatio = Integer.parseInt(snapshot.child("monthHouseRatio").getValue().toString());
                    } else {
                        houseRatio = 0;
                    }

                    float entRatio;
                    if (snapshot.hasChild("monthEntertainmentRatio")) {
                        entRatio = Integer.parseInt(snapshot.child("monthEntertainmentRatio").getValue().toString());
                    } else {
                        entRatio = 0;
                    }

                    float eduRatio;
                    if (snapshot.hasChild("monthEducationRatio")) {
                        eduRatio = Integer.parseInt(snapshot.child("monthEducationRatio").getValue().toString());
                    } else {
                        eduRatio = 0;
                    }

                    float charityRatio;
                    if (snapshot.hasChild("monthCharityRatio")) {
                        charityRatio = Integer.parseInt(snapshot.child("monthCharityRatio").getValue().toString());
                    } else {
                        charityRatio = 0;
                    }

                    float appRatio;
                    if (snapshot.hasChild("monthApparelRatio")) {
                        appRatio = Integer.parseInt(snapshot.child("monthApparelRatio").getValue().toString());
                    } else {
                        appRatio = 0;
                    }

                    float healthRatio;
                    if (snapshot.hasChild("monthHealthRatio")) {
                        healthRatio = Integer.parseInt(snapshot.child("monthHealthRatio").getValue().toString());
                    } else {
                        healthRatio = 0;
                    }

                    float personalRatio;
                    if (snapshot.hasChild("monthPersonalRatio")) {
                        personalRatio = Integer.parseInt(snapshot.child("monthPersonalRatio").getValue().toString());
                    } else {
                        personalRatio = 0;
                    }

                    float otherRatio;
                    if (snapshot.hasChild("monthOtherRatio")) {
                        otherRatio = Integer.parseInt(snapshot.child("monthOtherRatio").getValue().toString());
                    } else {
                        otherRatio = 0;
                    }

                    float monthTotalSpendAmountRatio;
                    if (snapshot.hasChild("monthBudget")) {
                        monthTotalSpendAmountRatio = Integer.parseInt(snapshot.child("monthBudget").getValue().toString());
                    } else {
                        monthTotalSpendAmountRatio = 0;
                    }


                    float monthPercent = ( monthTotalSpendAmount / monthTotalSpendAmountRatio) * 100;
                    if (monthPercent < 50) {
                        monthRatioSpending.setText(monthPercent + " %" + " used of " + monthTotalSpendAmountRatio + ". Status: ");
                        monthRatioSpending_Image.setImageResource(R.drawable.green);
                    } else if (monthPercent >= 50 && monthPercent < 100) {
                        monthRatioSpending.setText(monthPercent + " %" + " used of " + monthTotalSpendAmountRatio + ". Status: ");
                        monthRatioSpending_Image.setImageResource(R.drawable.brown);
                    } else {
                        monthRatioSpending.setText(monthPercent + " %" + " used of " + monthTotalSpendAmountRatio + ". Status: ");
                        monthRatioSpending_Image.setImageResource(R.drawable.red);
                    }


                    float transportPercent = (traTotal / traRatio) * 100;
                    if (transportPercent < 50) {
                        progress_ratio_transport.setText(transportPercent + " %" + " used of " + traRatio + ". Status: ");
                        transport_status.setImageResource(R.drawable.green);
                    } else if (transportPercent >= 50 && transportPercent < 100) {
                        progress_ratio_transport.setText(transportPercent + " %" + " used of " + traRatio + ". Status: ");
                        transport_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_transport.setText(transportPercent + " %" + " used of " + traRatio + ". Status: ");
                        transport_status.setImageResource(R.drawable.red);
                    }

                    float foodPercent = (foodTotal / foodRatio) * 100;
                    if (foodPercent < 50) {
                        progress_ratio_food.setText(foodPercent + " %" + " used of " + foodRatio + ". Status: ");
                        food_status.setImageResource(R.drawable.green);
                    } else if (foodPercent >= 50 && foodPercent < 100) {
                        progress_ratio_food.setText(foodPercent + " %" + " used of " + foodRatio + ". Status: ");
                        food_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_food.setText(foodPercent + " %" + " used of " + foodRatio + ". Status: ");
                        food_status.setImageResource(R.drawable.red);
                    }

                    float housePercent = (houseTotal / houseRatio) * 100;
                    if (housePercent < 50) {
                        progress_ratio_house.setText(housePercent + " %" + " used of " + houseRatio + ". Status: ");
                        house_status.setImageResource(R.drawable.green);
                    } else if (housePercent >= 50 && housePercent < 100) {
                        progress_ratio_house.setText(housePercent + " %" + " used of " + houseRatio + ". Status: ");
                        house_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_house.setText(housePercent + " %" + " used of " + houseRatio + ". Status: ");
                        house_status.setImageResource(R.drawable.red);
                    }

                    float entPercent = (entTotal / entRatio) * 100;
                    if (entPercent < 50) {
                        progress_ratio_entertainment.setText(entPercent + " %" + " used of " + entRatio + ". Status: ");
                        entertainment_status.setImageResource(R.drawable.green);
                    } else if (entPercent >= 50 && entPercent < 100) {
                        progress_ratio_entertainment.setText(entPercent + " %" + " used of " + entRatio + ". Status: ");
                        entertainment_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_entertainment.setText(entPercent + " %" + " used of " + entRatio + ". Status: ");
                        entertainment_status.setImageResource(R.drawable.red);
                    }

                    float eduPercent = (eduTotal / eduRatio) * 100;
                    if (eduPercent < 50) {
                        progress_ratio_education.setText(eduPercent + " %" + " used of " + eduRatio + ". Status: ");
                        education_status.setImageResource(R.drawable.green);
                    } else if (eduPercent >= 50 && eduPercent < 100) {
                        progress_ratio_education.setText(eduPercent + " %" + " used of " + eduRatio + ". Status: ");
                        education_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_education.setText(eduPercent + " %" + " used of " + eduRatio + ". Status: ");
                        education_status.setImageResource(R.drawable.red);
                    }

                    float charityPercent = (charityTotal / charityRatio) * 100;
                    if (charityPercent < 50) {
                        progress_ratio_charity.setText(charityPercent + " %" + " used of " + charityRatio + ". Status: ");
                        charity_status.setImageResource(R.drawable.green);
                    } else if (charityPercent >= 50 && charityPercent < 100) {
                        progress_ratio_charity.setText(charityPercent + " %" + " used of " + charityRatio + ". Status: ");
                        charity_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_charity.setText(charityPercent + " %" + " used of " + charityRatio + ". Status: ");
                        charity_status.setImageResource(R.drawable.red);
                    }

                    float appPercent = (apparelTotal / appRatio) * 100;
                    if (appPercent < 50) {
                        progress_ratio_apparel.setText(appPercent + " %" + " used of " + appRatio + ". Status: ");
                        apparel_status.setImageResource(R.drawable.green);
                    } else if (appPercent >= 50 && appPercent < 100) {
                        progress_ratio_apparel.setText(appPercent + " %" + " used of " + appRatio + ". Status: ");
                        apparel_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_apparel.setText(appPercent + " %" + " used of " + appRatio + ". Status: ");
                        apparel_status.setImageResource(R.drawable.red);
                    }

                    float healthPercent = (healthTotal / healthRatio) * 100;
                    if (healthPercent < 50) {
                        progress_ratio_health.setText(healthPercent + " %" + " used of " + healthRatio + ". Status: ");
                        health_status.setImageResource(R.drawable.green);
                    } else if (healthPercent >= 50 && healthPercent < 100) {
                        progress_ratio_health.setText(healthPercent + " %" + " used of " + healthRatio + ". Status: ");
                        health_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_health.setText(healthPercent + " %" + " used of " + healthRatio + ". Status: ");
                        health_status.setImageResource(R.drawable.red);
                    }

                    float otherPercent = (otherTotal / otherRatio) * 100;
                    if (otherPercent < 50) {
                        progress_ratio_other.setText(otherPercent + " %" + " used of " + otherRatio + ". Status: ");
                        other_status.setImageResource(R.drawable.green);
                    } else if (otherPercent >= 50 && otherPercent < 100) {
                        progress_ratio_other.setText(otherPercent + " %" + " used of " + otherRatio + ". Status: ");
                        other_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_other.setText(otherPercent + " %" + " used of " + otherRatio + ". Status: ");
                        other_status.setImageResource(R.drawable.red);
                    }

                    float personalPercent = (personalTotal / personalRatio) * 100;
                    if (personalPercent < 50) {
                        progress_ratio_personal.setText(personalPercent + " %" + " used of " + personalRatio + ". Status: ");
                        personal_status.setImageResource(R.drawable.green);
                    } else if (personalPercent >= 50 && personalPercent < 100) {
                        progress_ratio_personal.setText(personalPercent + " %" + " used of " + personalRatio + ". Status: ");
                        personal_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_personal.setText(personalPercent + " %" + " used of " + personalRatio + ". Status: ");
                        personal_status.setImageResource(R.drawable.red);
                    }
                } else {
                    Toast.makeText(MonthlyAnalyticsActivity.this, "setStatusAndImageResource error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}