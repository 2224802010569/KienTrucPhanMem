package com.example.fintrack.AnalyticService.data;

import android.database.Cursor;

import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.fintrack.AnalyticService.entity.AnalyticsData;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsRepository {

    private SupportSQLiteDatabase db;

    public AnalyticsRepository(SupportSQLiteDatabase db){
        this.db = db;
    }

    public List<AnalyticsData> getAnalyticsData(String userId){

        List<AnalyticsData> list = new ArrayList<>();

        Cursor c = db.query(
                "SELECT c.name, SUM(t.amount) " +
                        "FROM transactions t " +
                        "JOIN categories c ON t.category_id = c.category_id " +
                        "WHERE t.tx_type_id='EXPENSE' " +
                        "AND t.user_id=? " +
                        "GROUP BY c.name",
                new Object[]{userId}
        );

        double total = 0;

        List<String> categories = new ArrayList<>();
        List<Double> amounts = new ArrayList<>();

        while(c.moveToNext()){

            String cat = c.getString(0);
            double amt = c.getDouble(1);

            categories.add(cat);
            amounts.add(amt);

            total += amt;
        }

        c.close();

        for(int i=0;i<categories.size();i++){

            int percent = 0;

            if(total > 0){
                percent = (int)((amounts.get(i) / total) * 100);
            }

            list.add(
                    new AnalyticsData(
                            categories.get(i),
                            amounts.get(i),
                            percent
                    )
            );
        }

        return list;
    }
}