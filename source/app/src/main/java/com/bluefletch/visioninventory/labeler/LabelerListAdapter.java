package com.bluefletch.visioninventory.labeler;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bluefletch.visioninventory.R;
import com.bluefletch.visioninventory.data.ProductModel;

import java.util.HashSet;
import java.util.List;

public class LabelerListAdapter extends RecyclerView.Adapter<LabelerListAdapter.ViewHolder> {

    final String TAG = getClass().getSimpleName();

    private List<ProductModel> _dataset;
    private HashSet<String> _selected;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView productLabel;
        public TextView productDescription;
        public TextView productBarcode;
        public EditText productPrice;
        public CheckBox productSelect;

        public ViewHolder(RelativeLayout v) {
            super(v);

            productLabel = v.findViewById(R.id.labelProductLabel);
            productDescription = v.findViewById(R.id.labelProductDesc);
            productBarcode = v.findViewById(R.id.labelProductBarcode);
            productPrice = v.findViewById(R.id.labelEditPrice);
            productSelect = v.findViewById(R.id.labelSelect);

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public LabelerListAdapter(List<ProductModel> productList) {
        _dataset = productList;
        _selected = new HashSet<>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public LabelerListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_label_card, parent, false);
        //TODO: set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        final ViewHolder holder = viewHolder;
        ProductModel product = _dataset.get(position);

        holder.productLabel.setText(product.getLabel());
        holder.productDescription.setText(product.getDescription());
        holder.productBarcode.setText(product.getBarcode());
        holder.productPrice.setTag(position);
        holder.productPrice.setText(product.getPrice());

        final String barcode = product.getBarcode();

        holder.productSelect.setChecked(_selected.contains(barcode));
        holder.productSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    Log.d(TAG, "Adding barcode: " + barcode);
                    _selected.add(barcode);
                }
                else {
                    Log.d(TAG, "Removing barcode: " + barcode);
                    _selected.remove(barcode);
                }
            }
        });

        holder.productPrice.setTag(position);

        holder.productPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (holder.productPrice.getTag() != null) {
                    _dataset.get((int) holder.productPrice.getTag()).setPrice(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    public HashSet<String> getSelectedProducts() {
        return _selected;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return _dataset.size();
    }
}
