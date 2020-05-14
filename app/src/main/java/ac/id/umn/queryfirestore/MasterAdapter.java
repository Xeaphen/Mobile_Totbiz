package ac.id.umn.queryfirestore;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.database.collection.LLRBNode;
import com.google.firebase.firestore.DocumentSnapshot;

import io.opencensus.resource.Resource;

public class MasterAdapter extends FirestoreRecyclerAdapter<MasterModel, MasterAdapter.ProductViewHolder> {
    private OnItemClickListener listener;

    public MasterAdapter(@NonNull FirestoreRecyclerOptions<MasterModel> options) {
        super(options);
    }
    @Override
    protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull MasterModel model) {
        holder.setProductName(model);
    }
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ProductViewHolder(view);
    }

    public void deleteItem(int position){
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder{
        private View view;

        ProductViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(pos),pos);
                    }
                }
            });
        }

        void setProductName(final MasterModel product) {
            TextView txtView1, txtView2;
            CardView card;
            txtView1 = view.findViewById(R.id.textView1);
            txtView2 = view.findViewById(R.id.textView2);

            switch (product.getStatus()){
                case 1:
                    view.setBackgroundColor(ContextCompat.getColor(view.getContext(),R.color.code1));
                    break;
                case 2:
                    view.setBackgroundColor(ContextCompat.getColor(view.getContext(),R.color.code2));
                    break;
                case 3:
                    view.setBackgroundColor(ContextCompat.getColor(view.getContext(),R.color.code3));
                    break;
                case 4:
                    view.setBackgroundColor(ContextCompat.getColor(view.getContext(),R.color.code4));
                    break;
            }


            txtView1.setText(product.getTitle());
            txtView2.setText(product.getDesc());
        }
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot,  int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}
