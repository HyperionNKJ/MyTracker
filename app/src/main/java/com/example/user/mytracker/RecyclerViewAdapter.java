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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private Context mContext;
    private List<Entry> list; // if shopping list, use this as recycler array

    private boolean isExpenses;
    private String fileName;
    private List<Map.Entry<String, List<Entry>>> chronologicalList; // if expenses list, use this as recycler array

    private static final int DELETE = 0;
    private static final int EDIT = 1;

    // list is either Expense list or Shopping list. Corresponding filename = "Expenses List / Shopping List"
    public RecyclerViewAdapter(Context mContext, List<Entry> list, String filename) {
        this.mContext = mContext;
        this.list = list;
        this.fileName = filename;
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
            holder.dateEntries.removeAllViews(); // to prevent entries duplicating
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
                                                performAction(entry, DELETE);
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
                                ((EditText) dialogView.findViewById(R.id.ed_new_name)).setText(entry.getName());
                                ((EditText) dialogView.findViewById(R.id.ed_new_amount)).setText(entry.getAmount());

                                builder.setCancelable(true)
                                        .setView(dialogView)
                                        .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                EditText mNewName = dialogView.findViewById(R.id.ed_new_name);
                                                EditText mNewAmount = dialogView.findViewById(R.id.ed_new_amount);
                                                String newName = mNewName.getText().toString();
                                                String newAmount = mNewAmount.getText().toString();

                                                if (isValidInput(newName, newAmount)) {
                                                    entry.setName(newName.trim());
                                                    String trimmedAmount = newAmount.trim();
                                                    if (trimmedAmount.equals("")) { // happens when amount input is space
                                                        trimmedAmount = " ";
                                                    }
                                                    entry.setAmount(trimmedAmount);
                                                    performAction(entry, EDIT);
                                                }
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

    private boolean isValidInput(String inputName, String inputAmount) {
        if (inputName.equals("") || inputAmount.equals("")) {
            Toast.makeText(mContext, "Edit unsuccessful\nPlease include both name and amount", Toast.LENGTH_SHORT).show();
            return false;
        } else if (inputName.contains("\n") || inputName.contains("\r") || inputAmount.contains("\n") || inputAmount.contains("\r")) {
            Toast.makeText(mContext, "Edit unsuccessful\nInput should be within a single line", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!inputName.matches(".*[a-zA-Z]+.*")) {
            Toast.makeText(mContext, "Name should consist at least an alphabet", Toast.LENGTH_SHORT).show();
            return false;
        } else if (isExpenses && !inputAmount.matches("^ *[0-9]+ *$")) {
            Toast.makeText(mContext, "Edit unsuccessful\nAmount should be a non-negative integer without comma", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!isExpenses && !inputAmount.matches("^ *[\\p{Alnum} ]+ *$")) {
            Toast.makeText(mContext, "Edit unsuccessful\nAmount should be alphanumeric (including space)", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void performAction (Entry entry, int action) {
        String actionString = (action == DELETE) ? "deleted" : "edited";
        try {
            updateArray(entry, action);
            notifyDataSetChanged();
            updateFile(entry, action);
        } catch (Exception e) {
            Toast.makeText(mContext, "Entry not " + actionString + ". Something went wrong!", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(mContext, "Entry " + actionString, Toast.LENGTH_SHORT).show();
    }

    private void updateArray(Entry entry, int action)  {
        if (isExpenses) {
            updateChronologicalList(entry, action);
        }
        updateList(entry, action);
    }

    // List<Map.Entry<String, List<Entry>>> ChronologicalList
    private void updateChronologicalList(Entry entry, int action) {
        int dateIndex = findEntryIndexInChronologicalList(entry);
        List<Entry> entries = chronologicalList.get(dateIndex).getValue();
        int entryIndex = findEntryIndexInList(entry, entries);

        if (action == DELETE) {
            entries.remove(entryIndex);
        } else if (action == EDIT) {
            entries.set(entryIndex, entry);
        }
        chronologicalList.get(dateIndex).setValue(entries);
    }

    private void updateList(Entry entry, int action) {
        int entryIndex = findEntryIndexInList(entry, list);
        if (action == DELETE) {
            list.remove(entryIndex);
        } else if (action == EDIT) {
            list.set(entryIndex, entry);
        }
    }

    private int findEntryIndexInChronologicalList(Entry entry) {
        String entryDate = entry.getDate();
        for (int i = 0; i < chronologicalList.size(); i++) {
            if (chronologicalList.get(i).getKey().equals(entryDate)) {
                return i;
            }
        }
        return -1;
    }

    // uses dateTime which is a unique identifier for entries
    private int findEntryIndexInList(Entry target, List<Entry> list) {
        for (int i = 0; i < list.size(); i ++) {
            if (list.get(i).getDateTime().equals(target.getDateTime())) {
                return i;
            }
        }
        return -1;
    }

    private void updateFile(Entry target, int action) {
        StringBuilder updatedContentString = new StringBuilder();
        FileInputStream fis;
        FileOutputStream fos = null;

        try {
            fis = mContext.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String entry;
            while ((entry = br.readLine()) != null) {
                if (target.isEquals(entry)) {
                    if (action == DELETE) {
                        continue;
                    } else if (action == EDIT) {
                        updatedContentString.append(target.getRawEntryString()).append("\n");
                        continue;
                    }
                }
                updatedContentString.append(entry).append("\n");
            }
            fos = mContext.openFileOutput(fileName, MODE_PRIVATE);
            fos.write(updatedContentString.toString().getBytes());
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
}