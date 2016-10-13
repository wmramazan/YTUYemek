package com.adnagu.ytuyemek;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class MealListAdapter extends RecyclerView.Adapter<MealListAdapter.MealListViewHolder> {

    private ArrayList<Meal> meals;
    private Context context;
    private Calendar date;

    public MealListAdapter(Context context, ArrayList<Meal> meals) {
        this.context = context;
        this.meals = meals;
        this.date = Calendar.getInstance();
    }

    @Override
    public MealListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meal_card, parent, false);
        return new MealListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MealListViewHolder holder, int position) {
        final Meal meal = meals.get(position);

        if(meal.getId() == date.get(Calendar.DAY_OF_MONTH)) holder.title.setText(R.string.mealOfToday);
        else holder.title.setText(meal.getId()+"/"+String.valueOf(date.get(Calendar.MONTH)+1)+"/"+date.get(Calendar.YEAR));

        if(meal.getLunch() == null) {
            holder.dinner_layout.setVisibility(View.GONE);
            holder.lunch.setText(context.getString(R.string.noMeal));
        } else {
            holder.lunch.setText(Html.fromHtml(meal.getLunch()));
            if(meal.getDinner() == null) {
                holder.dinner_layout.setVisibility(View.GONE);
            } else {
                holder.dinner_layout.setVisibility(View.VISIBLE);
                holder.dinner.setText(Html.fromHtml(meal.getDinner()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return (null != meals ? meals.size() : 0);
    }

    public static class MealListViewHolder extends RecyclerView.ViewHolder {

        protected TextView title, lunch, dinner;
        protected LinearLayout dinner_layout;

        public MealListViewHolder(View itemView) {
            super(itemView);
            this.title = (TextView) itemView.findViewById(R.id.meal_title);
            this.lunch = (TextView) itemView.findViewById(R.id.meal_lunch);
            this.dinner = (TextView) itemView.findViewById(R.id.meal_dinner);
            this.dinner_layout = (LinearLayout) itemView.findViewById(R.id.meal_dinner_layout);
        }
    }
}