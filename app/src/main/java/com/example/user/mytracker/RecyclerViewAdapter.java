package com.example.user.mytracker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private Context mContext;
    private List<String> list; // if shopping list, use this as recycler array

    private boolean isExpenses;
    private List<Map.Entry<String, List<String>>> chronologicalList; // if expenses list, use this as recycler array

    private static final int DELETE = 0;
    private static final int EDIT = 1;

    // list is either Expense list or Shopping list. Corresponding filename = "Expenses List / Shopping List"
    public RecyclerViewAdapter(Context mContext, List<String> list, String filename) {
        this.mContext = mContext;
        this.list = list;
        this.isExpenses = filename.equals("Expenses List");
        if (isExpenses) {
            initializeChronologicalList();
        }
    }

   /* Group entries of similar date together in expense list
    * Each expense entry is of the format 'Kimchi Rameon = 1800 == 31-08-2019'
    * Hence, Use ' == ' delimiter to extract date and ' = ' to separate name and amount
    */
    private void initializeChronologicalList() {
        Map<String, List<String>> chronologicalMap = new LinkedHashMap<>(); // Key = date, Value = entries dated 'date'
        for (String entryWithDate : list) {
            String[] output = entryWithDate.split(" == ");
            String date = output[1]; // '31-08-2019'
            String entry = output[0]; // 'Kimchi Rameon = 1800'

            if (!chronologicalMap.containsKey(date)) {
                List<String> similarDateEntries = new ArrayList<>();
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
     *      Entry format -> 'PineApple = 15000 == 30-08-2019'
     * FOR SHOPPING:
     *      Array -> list
     *      Row Layout -> R.layout.recycler_row_shopping
     *      Entry format -> 'Wall Hook = 3'
     */
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (isExpenses) {
            Map.Entry<String, List<String>> dateEntriesPair = chronologicalList.get(position);
            String date = dateEntriesPair.getKey();
            List<String> entries = dateEntriesPair.getValue();

            holder.date.setText(date);

            double dateTotalExpenses = 0;
            for (String entry : entries) {
                String[] output = entry.split(" = "); // e.g. 'Kimchi Rameon = 1800'
                String name = output[0]; // 'Kimchi Rameon'
                String amount = output[1]; // '1800'

                appendEntryUnderDate(name, amount, holder);
                dateTotalExpenses += Double.parseDouble(amount);
            }

            holder.dateTotal.setText("Total: " + formatAmount(String.valueOf(dateTotalExpenses)));
        } else { // SHOPPING
            String entry = list.get(position);
            String[] output = entry.split(" = ");
            holder.name.setText(output[0]);
            holder.amount.setText(formatAmount(output[1]));
        }
    }

    private void appendEntryUnderDate(String name, String amount, final ViewHolder holder) {
        // First, get the view by reusing recycler_row_shopping's layout
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_row_shopping, holder.dateEntries, false);
        // Second, initialize name and amount
        ((TextView) view.findViewById(R.id.tv_name)).setText(name);
        ((TextView) view.findViewById(R.id.tv_amount)).setText(formatAmount(amount));
        // Third, append view (entry) onto date's entries
        holder.dateEntries.addView(view);
    }

    private String formatAmount(String amount) {
        return String.format("%,d", Math.round(Double.parseDouble(amount))) + " KRW";
    }

       /* String entry = list.get(position);
        final String[] output = entry.split(" = ");
        holder.english.setText(output[0]);
        if (checkBoxVisibility) {
            holder.visibility.setVisibility(View.VISIBLE);
        } else {
            holder.visibility.setVisibility(View.INVISIBLE);
        }
        int backupLastPos = lastPosition;
        if (visibilityArray.get(position)) {
            holder.visibility.setChecked(true);
            holder.korean.setText(output[1]);
        } else {
            holder.visibility.setChecked(false);
            holder.korean.setText("");
        }
        lastPosition = backupLastPos;
        holder.visibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int h_position = holder.getAdapterPosition();
                if (buttonView.isChecked()) {
                    holder.korean.setText(output[1]);
                    visibilityArray.set(h_position, true);
                } else {
                    holder.korean.setText("");
                    visibilityArray.set(h_position, false);
                }
                if (h_position == visibilityArray.size() - 1) {
                    lastPosition = -1;
                } else {
                    lastPosition = h_position;
                }
            }
        });
        holder.english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(output[0] + " = " + output[1] + "\n\nWhat do you want to do with it?")
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
                                builder1.setMessage(output[0] + " = " + output[1] + " will be deleted.")
                                        .setCancelable(true)
                                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                performAction(holder.getAdapterPosition(), DELETE, null, null);
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
                                                EditText et_englishInput = dialogView.findViewById(R.id.ed_englishInput);
                                                EditText et_koreanInput = dialogView.findViewById(R.id.ed_koreanInput);
                                                String englishInput = et_englishInput.getText().toString();
                                                String koreanInput = et_koreanInput.getText().toString();

                                                if (englishInput.equals("") || koreanInput.equals("")) {
                                                    Toast.makeText(mContext, "Invalid input. Entry not edited", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    performAction(holder.getAdapterPosition(), EDIT, englishInput, koreanInput);
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
        });
    }

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

    public void toggleAll(boolean toggleTo) {
        Collections.fill(visibilityArray, toggleTo);
        notifyDataSetChanged();
        lastPosition = -1;
    }

    public void toggleAfterLastPosition() {
        if (visibilityArray.isEmpty()) {
            Toast.makeText(mContext, filename + " is empty!", Toast.LENGTH_LONG).show();
            return;
        }
        if (visibilityArray.get(lastPosition + 1)) {
            visibilityArray.set(lastPosition + 1, false);
        } else {
            visibilityArray.set(lastPosition + 1, true);
        }
        notifyItemChanged(lastPosition + 1);
        if (lastPosition + 1 == vocabList.size() - 1) {
            lastPosition = -1;
        } else {
            lastPosition++;
        }
    }

    public void setCheckBoxVisibility(boolean checkBoxVisibility) {
        this.checkBoxVisibility = checkBoxVisibility;
        this.notifyDataSetChanged();
    } */

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
}