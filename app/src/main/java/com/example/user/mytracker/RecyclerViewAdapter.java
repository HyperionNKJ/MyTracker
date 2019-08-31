package com.example.user.mytracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private Context mContext;
    private List<Entry> list; // if shopping list, use this as recycler array

    private boolean isExpenses;
    private List<Map.Entry<String, List<Entry>>> chronologicalList; // if expenses list, use this as recycler array

    private static final int DELETE = 0;
    private static final int EDIT = 1;

    // list is either Expense list or Shopping list. Corresponding filename = "Expenses List / Shopping List"
    public RecyclerViewAdapter(Context mContext, List<Entry> list, String filename) {
        this.mContext = mContext;
        this.list = list;
        this.isExpenses = filename.equals("Expenses List");
        if (isExpenses) {
            initializeChronologicalList();
        }
    }

   /* Group entries of similar date together in expense list
    * Each expense entry is of the format 'Fried Rice = 6000 = 31-08-2019 = 22-57-24'
    */
    private void initializeChronologicalList() {
        Map<String, List<Entry>> chronologicalMap = new LinkedHashMap<>(); // Key = date, Value = entries dated 'date'
        for (Entry entry : list) {
            String date = entry.getDate();
            if (!chronologicalMap.containsKey(date)) {
                List<Entry> similarDateEntries = new ArrayList<>();
                similarDateEntries.add(entry);
                chronologicalMap.put(date, similarDateEntries);
            } else {
                chronologicalMap.get(date).add(entry);
            }
        }
        chronologicalList = new ArrayList<>(chronologicalMap.entrySet());
    }

    @NonNull
    @Override
    // Responsible for inflating the view (Not important to understand. Same for any recyclerView adapter
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int rowID = isExpenses ? R.layout.recycler_row_expenses : R.layout.recycler_row_shopping;
        View view = LayoutInflater.from(parent.getContext()).inflate(rowID, parent, false);
        return new ViewHolder(view);
    }

    @Override
    /*
     * Display the data at the specified position. This method is used to update the contents of the itemView.
     * FOR EXPENSES:
     *      Array -> chronologicalList
     *      Row Layout -> R.layout.recycler_row_expenses
     * FOR SHOPPING:
     *      Array -> list
     *      Row Layout -> R.layout.recycler_row_shopping
     */
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (isExpenses) {
            Map.Entry<String, List<Entry>> dateEntriesPair = chronologicalList.get(position);
            String date = dateEntriesPair.getKey();
            List<Entry> entries = dateEntriesPair.getValue();

            holder.date.setText(date);

            long dateTotalExpenses = 0;
            for (Entry entry : entries) {
                appendEntryUnderDate(entry, holder);
                dateTotalExpenses += Long.parseLong(entry.getAmount());
            }

            holder.dateTotal.setText("Total: " + Entry.formatAmount(String.valueOf(dateTotalExpenses)));
        } else { // SHOPPING
            Entry entry = list.get(position);

            holder.name.setText(entry.getName());
            holder.amount.setText(entry.getAmount());
            holder.name.setOnClickListener(getOnClickListener(entry));
        }
    }

    private void appendEntryUnderDate(Entry entry, final ViewHolder holder) {
        // First, get the view by reusing recycler_row_shopping's layout
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_row_shopping, holder.dateEntries, false);
        // Second, initialize and set name and amount
        TextView mName = view.findViewById(R.id.tv_name);
        TextView mAmount = view.findViewById(R.id.tv_amount);
        mName.setText(entry.getName());
        mAmount.setText(entry.getFormattedAmount());
        // Third, append view (entry) onto date's entries
        holder.dateEntries.addView(view);
        // Fourth, add onLongClickListener
        mName.setOnClickListener(getOnClickListener(entry));
    }

    @Override
    // Tells adapter how many entries are in list. If 0 then recycler view will be empty
    public int getItemCount() {
        return isExpenses ? chronologicalList.size() : list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder { // holds each entry in memory
        // for expenses
        TextView date;
        TextView dateTotal;
        LinearLayout dateEntries;

        // for shopping
        TextView name;
        TextView amount;

        public ViewHolder(View itemView) {
            super(itemView);
            if (isExpenses) {
                date = itemView.findViewById(R.id.tv_date);
                dateTotal = itemView.findViewById(R.id.tv_date_total);
                dateEntries = itemView.findViewById(R.id.ll_date_entries);
            } else { // SHOPPING
                name = itemView.findViewById(R.id.tv_name);
                amount = itemView.findViewById(R.id.tv_amount);
            }
        }
    }

    // TODO: DELETE AND EDIT FEATURE
    private View.OnClickListener getOnClickListener(final Entry entry) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = entry.getName();
                final String amount = entry.getAmount();
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("Name: " + name + "\nAmount: " + amount + "\n\nWhat do you want to do with it?")
                        .setCancelable(true)
                        .setNeutralButton("Nothing", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                                builder1.setMessage("Name: " + name + "\nAmount: " + amount + "\n\nThis entry will be deleted.")
                                        .setCancelable(true)
                                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
//                                                deleteEntry();
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                AlertDialog alert = builder1.create();
                                alert.setTitle("Confirm delete?");
                                alert.show();
                            }
                        })
                        .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                                final View dialogView = inflater.inflate(R.layout.edit_dialog, null);

                                builder.setCancelable(true)
                                        .setView(dialogView)
                                        .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                EditText mNewName = dialogView.findViewById(R.id.ed_new_name);
                                                EditText mNewAmount = dialogView.findViewById(R.id.ed_new_amount);
                                                String newName = mNewName.getText().toString().trim();
                                                String newAmount = mNewAmount.getText().toString().trim();
//                                                validateInput();
//                                                editEntry();
                                                dialog.dismiss();
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                builder.create().show();
                            }
                        });
                builder.create().show();
            }
        };
    }

    /*
    // action can be either edit or delete
    private void performAction(int position, int action, String englishInput, String koreanInput) {
        String entry = vocabList.get(position);

        if (action == DELETE) {
            vocabList.remove(position);
            visibilityArray.remove(position);
            this.notifyItemRemoved(position);
        } else if (action == EDIT) {
            vocabList.set(position, englishInput + " = " + koreanInput);
            this.notifyDataSetChanged();
        }

        // edit or delete from file
        StringBuilder sb = new StringBuilder();
        FileInputStream fis;
        FileOutputStream fos = null;

        try {
            fis = mContext.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String engKorPair;
            while ((engKorPair = br.readLine()) != null) {
                if (engKorPair.equals(entry)) {
                    if (action == DELETE) {
                        continue;
                    } else if (action == EDIT) {
                        sb.append(englishInput).append(" = ").append(koreanInput).append("\n");
                        continue;
                    }
                }
                sb.append(engKorPair).append("\n");
            }
            fos = mContext.openFileOutput(filename, MODE_PRIVATE);
            fos.write(sb.toString().getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
*/
}