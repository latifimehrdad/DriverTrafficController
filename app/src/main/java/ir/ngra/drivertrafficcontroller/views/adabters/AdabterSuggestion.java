package ir.ngra.drivertrafficcontroller.views.adabters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cunoraz.gifview.library.GifView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.ngra.drivertrafficcontroller.R;
import ir.ngra.drivertrafficcontroller.databinding.AdabterSuggestionBinding;
import ir.ngra.drivertrafficcontroller.models.ModelAdabterSuggestion;
import ir.ngra.drivertrafficcontroller.views.fragments.HomeOsm;

public class AdabterSuggestion extends RecyclerView.Adapter<AdabterSuggestion.CustomHolder> {

    private List<ModelAdabterSuggestion> items;
    private LayoutInflater layoutInflater;
    private Context context;
    private HomeOsm homeOsm;

    public AdabterSuggestion(List<ModelAdabterSuggestion> items, Context context, HomeOsm homeOsm) {
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
        return new AdabterSuggestion.CustomHolder(DataBindingUtil.inflate(layoutInflater, R.layout.adabter_suggestion, parent, false));
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
        AdabterSuggestionBinding binding;

        @BindView(R.id.LinearLayoutShowOnMap)
        LinearLayout LinearLayoutShowOnMap;

        @BindView(R.id.GifViewAddress)
        GifView GifViewAddress;

        public CustomHolder(AdabterSuggestionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            View view = binding.getRoot();
            ButterKnife.bind(this, view);
        }

        public void bind(ModelAdabterSuggestion item, final int positon) {

            GifViewAddress.setVisibility(View.GONE);

            if (item.isLoadMore())
                LinearLayoutShowOnMap.setVisibility(View.GONE);
            else
                LinearLayoutShowOnMap.setVisibility(View.VISIBLE);

            LinearLayoutShowOnMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    homeOsm.ShowOnMap(positon);
                }
            });

            binding.setSuggestion(item);
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (item.isLoadMore()) {
                        GifViewAddress.setVisibility(View.VISIBLE);
                        homeOsm.ChooseAddressFromSuggestion(positon);
                    }
                }
            });
            binding.executePendingBindings();
        }
    }
}
