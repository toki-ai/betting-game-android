package com.example.bettinggame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Import lớp model
import com.example.bettinggame.model.RaceResult;

import java.util.List;
import java.util.Locale;

public class RaceResultAdapter extends RecyclerView.Adapter<RaceResultAdapter.ViewHolder> {

    private List<RaceResult> raceResults;
    private Context context;

    public RaceResultAdapter(List<RaceResult> raceResults, Context context) {
        this.raceResults = raceResults;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_race_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RaceResult result = raceResults.get(position); // RaceResult giờ đây từ package model
        if (result == null) return;

        holder.textViewRank.setText(String.format(Locale.getDefault(), "Hạng: %d", result.getRank()));
        if (result.getDuck() != null) {
            holder.textViewDuckName.setText(result.getDuck().getName()); // getDuck() trả về model.Duck
        } else {
            holder.textViewDuckName.setText("N/A");
        }

        double amount = result.getAmountWon();
        String amountString;
        if (amount > 0) {
            amountString = String.format(Locale.getDefault(), "+%dđ", (int)amount);
        } else if (amount < 0) {
            amountString = String.format(Locale.getDefault(), "%dđ", (int)amount);
        } else {
            amountString = "0đ";
        }
        holder.textViewAmountWonLost.setText(amountString);
    }

    @Override
    public int getItemCount() {
        return raceResults == null ? 0 : raceResults.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewRank;
        TextView textViewDuckName;
        TextView textViewAmountWonLost;

        ViewHolder(View itemView) {
            super(itemView);
            textViewRank = itemView.findViewById(R.id.textViewRank);
            textViewDuckName = itemView.findViewById(R.id.textViewDuckName);
            textViewAmountWonLost = itemView.findViewById(R.id.textViewAmountWonLost);
        }
    }

    public void updateData(List<RaceResult> newRaceResults) {
        this.raceResults.clear();
        if (newRaceResults != null) {
            this.raceResults.addAll(newRaceResults);
        }
        notifyDataSetChanged();
    }
}
