package com.example.bettinggame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bettinggame.model.TutorialStep;
import java.util.List;

public class TutorialAdapter extends RecyclerView.Adapter<TutorialAdapter.TutorialViewHolder> {

    private List<TutorialStep> tutorialSteps;
    private Context context;

    public TutorialAdapter(Context context, List<TutorialStep> tutorialSteps) {
        this.context = context;
        this.tutorialSteps = tutorialSteps;
    }

    @NonNull
    @Override
    public TutorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tutorial_step, parent, false);
        return new TutorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TutorialViewHolder holder, int position) {
        TutorialStep step = tutorialSteps.get(position);
        holder.imageViewTutorial.setImageResource(step.getImageResId());
        holder.textViewTutorialDescription.setText(step.getDescription());
    }

    @Override
    public int getItemCount() {
        return tutorialSteps == null ? 0 : tutorialSteps.size();
    }

    static class TutorialViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewTutorial;
        TextView textViewTutorialDescription;

        TutorialViewHolder(View itemView) {
            super(itemView);
            imageViewTutorial = itemView.findViewById(R.id.imageViewTutorial);
            textViewTutorialDescription = itemView.findViewById(R.id.textViewTutorialDescription);
        }
    }
}
