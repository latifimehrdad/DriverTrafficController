package ir.ngra.drivertrafficcontroller.views.adabters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cunoraz.gifview.library.GifView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.ngra.drivertrafficcontroller.R;
import ir.ngra.drivertrafficcontroller.databinding.AdabterDestinationBinding;
import ir.ngra.drivertrafficcontroller.databinding.AdabterSuggestionBinding;
import ir.ngra.drivertrafficcontroller.models.ModelAdabterSuggestion;
import ir.ngra.drivertrafficcontroller.models.ModelSuggestionAddress;
import ir.ngra.drivertrafficcontroller.views.fragments.HomeOsm;

public class AdabterDestination extends RecyclerView.Adapter<AdabterDestination.CustomHolder> {

    private List<ModelSuggestionAddress> items;
    private LayoutInflater layoutInflater;
    private Context context;
    private HomeOsm homeOsm;

    public AdabterDestination(List<ModelSuggestionAddress> items, Context context, HomeOsm homeOsm) {
        this.items = items;
        this.context = context;
        this.homeOsm = homeOsm;
    }

    @NonNull
    @Override
    public CustomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        return new AdabterDestination.CustomHolder(DataBindingUtil.inflate(layoutInflater, R.layout.adabter_destination, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class CustomHolder extends RecyclerView.ViewHolder {
        AdabterDestinationBinding binding;

        @BindView(R.id.ImageViewDeleteDestination)
        ImageView ImageViewDeleteDestination;

        @BindView(R.id.TextViewRow)
        TextView TextViewRow;

        public CustomHolder(AdabterDestinationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            View view = binding.getRoot();
            ButterKnife.bind(this, view);
        }

        public void bind(ModelSuggestionAddress item, final int positon) {//________________________ bind
            Context context = TextViewRow.getContext();
            String row = context.getResources().getString(R.string.Destination);
            row = row + " " +  RowNumber(positon+1, context);
            TextViewRow.setText(row);
            ImageViewDeleteDestination.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    homeOsm.DeleteDestination(positon);
                }
            });

            binding.setSuggestion(item);
            binding.executePendingBindings();
        }//_________________________________________________________________________________________ bind


        private String RowNumber (Integer row, Context context) {//_________________________________ RowNumber
            switch (row){
                case 1:
                    return context.getString(R.string.One);
                case 2:
                    return context.getString(R.string.Two);
                case 3:
                    return context.getString(R.string.Three);
                case 4:
                    return context.getString(R.string.Four);
                case 5:
                    return context.getString(R.string.Five);
                default:
                    return context.getString(R.string.Next);
            }
        }//_________________________________________________________________________________________ RowNumber

    }
}

