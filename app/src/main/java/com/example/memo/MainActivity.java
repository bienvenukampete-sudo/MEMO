package com.example.memo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private TextView emptyView;
    private TextView tvStats;
    private int nextId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
        tvStats = findViewById(R.id.tvStats);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        updateEmptyView();
        updateStats();

        fabAdd.setOnClickListener(v -> showAddTaskDialog());
    }

    private void showAddTaskDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        EditText editTitle = dialogView.findViewById(R.id.editTaskTitle);
        RadioGroup radioGroupPriority = dialogView.findViewById(R.id.radioGroupPriority);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Nouvelle Tâche")
                .setView(dialogView)
                .setPositiveButton("Ajouter", (d, w) -> {
                    String title = editTitle.getText().toString().trim();
                    if (!title.isEmpty()) {
                        int selectedId = radioGroupPriority.getCheckedRadioButtonId();
                        Priority priority = getPriorityFromRadioButton(selectedId);

                        Task task = new Task(nextId++, title, priority, false);
                        taskList.add(task);
                        sortTasks();
                        taskAdapter.notifyDataSetChanged();
                        updateEmptyView();
                        updateStats();

                        Snackbar.make(findViewById(android.R.id.content),
                                "Tâche ajoutée avec succès ✓",
                                Snackbar.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Veuillez entrer un titre", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .create();

        dialog.show();
    }

    private void showEditTaskDialog(Task task, int position) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        EditText editTitle = dialogView.findViewById(R.id.editTaskTitle);
        RadioGroup radioGroupPriority = dialogView.findViewById(R.id.radioGroupPriority);

        // Pré-remplir avec les données actuelles
        editTitle.setText(task.getTitle());

        // Sélectionner la priorité actuelle
        if (task.getPriority() == Priority.HIGH) {
            radioGroupPriority.check(R.id.radioPriorityHigh);
        } else if (task.getPriority() == Priority.MEDIUM) {
            radioGroupPriority.check(R.id.radioPriorityMedium);
        } else {
            radioGroupPriority.check(R.id.radioPriorityLow);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Modifier la Tâche")
                .setView(dialogView)
                .setPositiveButton("Enregistrer", (d, w) -> {
                    String newTitle = editTitle.getText().toString().trim();
                    if (!newTitle.isEmpty()) {
                        int selectedId = radioGroupPriority.getCheckedRadioButtonId();
                        Priority newPriority = getPriorityFromRadioButton(selectedId);

                        task.setTitle(newTitle);
                        task.setPriority(newPriority);
                        sortTasks();
                        taskAdapter.notifyDataSetChanged();

                        Snackbar.make(findViewById(android.R.id.content),
                                "Tâche modifiée avec succès ✓",
                                Snackbar.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Veuillez entrer un titre", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .create();

        dialog.show();
    }

    private Priority getPriorityFromRadioButton(int selectedId) {
        if (selectedId == R.id.radioPriorityHigh) {
            return Priority.HIGH;
        } else if (selectedId == R.id.radioPriorityMedium) {
            return Priority.MEDIUM;
        } else {
            return Priority.LOW;
        }
    }

    private void sortTasks() {
        Collections.sort(taskList, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                if (t1.isCompleted() != t2.isCompleted()) {
                    return t1.isCompleted() ? 1 : -1;
                }
                return Integer.compare(t1.getPriority().ordinal(), t2.getPriority().ordinal());
            }
        });
    }

    private void updateEmptyView() {
        if (taskList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            tvStats.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            tvStats.setVisibility(View.VISIBLE);
        }
    }

    private void updateStats() {
        if (taskList.isEmpty()) {
            return;
        }

        int total = taskList.size();
        int completed = 0;
        for (Task task : taskList) {
            if (task.isCompleted()) {
                completed++;
            }
        }
        int remaining = total - completed;
        int percentage = (total > 0) ? (completed * 100 / total) : 0;

        String stats = String.format("Total: %d | Accomplies: %d | Restantes: %d | Progression: %d%%",
                total, completed, remaining, percentage);
        tvStats.setText(stats);
    }

    private void showDeleteConfirmationDialog(int position) {
        final Task task = taskList.get(position);

        new AlertDialog.Builder(this)
                .setTitle("⚠️ Confirmation")
                .setMessage("Voulez-vous vraiment supprimer la tâche \"" + task.getTitle() + "\" ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    final Task deletedTask = new Task(task.getId(), task.getTitle(), task.getPriority(), task.isCompleted());
                    taskList.remove(position);
                    taskAdapter.notifyDataSetChanged();
                    updateEmptyView();
                    updateStats();

                    // Snackbar avec option d'annulation
                    Snackbar.make(findViewById(android.R.id.content),
                                    "Tâche supprimée",
                                    Snackbar.LENGTH_LONG)
                            .setAction("ANNULER", v -> {
                                taskList.add(deletedTask);
                                sortTasks();
                                taskAdapter.notifyDataSetChanged();
                                updateEmptyView();
                                updateStats();
                            })
                            .show();
                })
                .setNegativeButton("Annuler", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showDeleteAllCompletedDialog() {
        int completedCount = 0;
        for (Task task : taskList) {
            if (task.isCompleted()) {
                completedCount++;
            }
        }

        final int finalCompletedCount = completedCount;

        if (completedCount == 0) {
            Toast.makeText(this, "Aucune tâche accomplie à supprimer", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("⚠️ Supprimer toutes les tâches accomplies ?")
                .setMessage("Vous allez supprimer " + completedCount + " tâche(s) accomplie(s).")
                .setPositiveButton("Supprimer tout", (dialog, which) -> {
                    List<Task> toRemove = new ArrayList<>();
                    for (Task task : taskList) {
                        if (task.isCompleted()) {
                            toRemove.add(task);
                        }
                    }
                    taskList.removeAll(toRemove);
                    taskAdapter.notifyDataSetChanged();
                    updateEmptyView();
                    updateStats();

                    Snackbar.make(findViewById(android.R.id.content),
                            finalCompletedCount + " tâche(s) supprimée(s)",
                            Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete_completed) {
            showDeleteAllCompletedDialog();
            return true;
        } else if (id == R.id.action_mark_all_complete) {
            markAllAsComplete();
            return true;
        } else if (id == R.id.action_mark_all_incomplete) {
            markAllAsIncomplete();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void markAllAsComplete() {
        if (taskList.isEmpty()) {
            Toast.makeText(this, "Aucune tâche disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Task task : taskList) {
            task.setCompleted(true);
        }
        sortTasks();
        taskAdapter.notifyDataSetChanged();
        updateStats();

        Snackbar.make(findViewById(android.R.id.content),
                "Toutes les tâches marquées comme accomplies ✓",
                Snackbar.LENGTH_SHORT).show();
    }

    private void markAllAsIncomplete() {
        if (taskList.isEmpty()) {
            Toast.makeText(this, "Aucune tâche disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Task task : taskList) {
            task.setCompleted(false);
        }
        sortTasks();
        taskAdapter.notifyDataSetChanged();
        updateStats();

        Snackbar.make(findViewById(android.R.id.content),
                "Toutes les tâches marquées comme non accomplies",
                Snackbar.LENGTH_SHORT).show();
    }

    private class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
        private List<Task> tasks;
        private static final int VIEW_TYPE_HEADER = 0;
        private static final int VIEW_TYPE_TASK = 1;

        public TaskAdapter(List<Task> tasks) {
            this.tasks = tasks;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return tasks.get(0).isCompleted() ? VIEW_TYPE_HEADER : VIEW_TYPE_TASK;
            }

            if (!tasks.get(position - 1).isCompleted() && tasks.get(position).isCompleted()) {
                return VIEW_TYPE_HEADER;
            }

            return VIEW_TYPE_TASK;
        }

        @Override
        public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_HEADER) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_header, parent, false);
                return new TaskViewHolder(view, true);
            } else {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_task, parent, false);
                return new TaskViewHolder(view, false);
            }
        }

        @Override
        public void onBindViewHolder(TaskViewHolder holder, int position) {
            if (holder.isHeader) {
                holder.bindHeader(tasks.get(position).isCompleted());
            } else {
                holder.bind(tasks.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }

        class TaskViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkBox;
            TextView tvTitle;
            TextView tvPriority;
            ImageButton btnDelete;
            ImageButton btnEdit;
            CardView cardView;
            TextView tvHeader;
            boolean isHeader;

            public TaskViewHolder(View itemView, boolean isHeader) {
                super(itemView);
                this.isHeader = isHeader;

                if (isHeader) {
                    tvHeader = itemView.findViewById(R.id.tvHeader);
                } else {
                    checkBox = itemView.findViewById(R.id.checkBoxTask);
                    tvTitle = itemView.findViewById(R.id.tvTaskTitle);
                    tvPriority = itemView.findViewById(R.id.tvTaskPriority);
                    btnDelete = itemView.findViewById(R.id.btnDelete);
                    btnEdit = itemView.findViewById(R.id.btnEdit);
                    cardView = itemView.findViewById(R.id.cardView);
                }
            }

            public void bindHeader(boolean isCompleted) {
                if (isCompleted) {
                    tvHeader.setText("✅ Tâches accomplies");
                } else {
                    tvHeader.setText("📝 Tâches à faire");
                }
            }

            public void bind(Task task) {
                tvTitle.setText(task.getTitle());
                tvPriority.setText("Priorité: " + task.getPriority().getLabel());
                tvPriority.setTextColor(task.getPriority().getColor());
                checkBox.setChecked(task.isCompleted());

                if (task.isCompleted()) {
                    tvTitle.setPaintFlags(tvTitle.getPaintFlags() |
                            android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                    tvTitle.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));
                    cardView.setAlpha(0.7f);
                } else {
                    tvTitle.setPaintFlags(tvTitle.getPaintFlags() &
                            (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                    tvTitle.setTextColor(getResources().getColor(android.R.color.black));
                    cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));
                    cardView.setAlpha(1.0f);
                }

                checkBox.setOnClickListener(v -> {
                    int currentPosition = getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        task.setCompleted(!task.isCompleted());
                        sortTasks();
                        notifyDataSetChanged();
                        updateStats();
                    }
                });

                btnEdit.setOnClickListener(v -> {
                    int currentPosition = getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        showEditTaskDialog(task, currentPosition);
                    }
                });

                btnDelete.setOnClickListener(v -> {
                    int currentPosition = getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        showDeleteConfirmationDialog(currentPosition);
                    }
                });

                // Clic long sur la carte pour modifier
                cardView.setOnLongClickListener(v -> {
                    int currentPosition = getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        showEditTaskDialog(task, currentPosition);
                    }
                    return true;
                });
            }
        }
    }
}
