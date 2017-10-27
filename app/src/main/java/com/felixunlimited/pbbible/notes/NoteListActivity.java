package com.felixunlimited.pbbible.notes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.felixunlimited.pbbible.R;

import java.util.ArrayList;

import static android.support.v4.app.NavUtils.navigateUpFromSameTask;

/**
 * An activity representing a list of Util. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link NoteDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class NoteListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    String mScripture;
    private boolean multiSelect = false;
    private ArrayList<Integer> selectedItems = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.felixunlimited.pbbible.Util.setTheme(this, R.style.Theme_AppCompat_Light);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_note_list);
        // Show the Up button in the action bar.
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mScripture = getIntent().getStringExtra(Util.ARG_SCRIPTURE);

        if (!Util.NOTES_DIR.exists())
            Util.NOTES_DIR.mkdirs();
        View recyclerView = findViewById(R.id.note_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.note_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notes_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void openNote(String rawNote) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(Util.ARG_TWO_PANE, "twoPane");
            arguments.putString(Util.ARG_OPEN_NOTE_FROM_LIST, rawNote);
            arguments.putString(Util.ARG_SCRIPTURE, mScripture);
            NoteDetailFragment fragment = new NoteDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.note_detail_container, fragment)
                    .commit();
        } else {
            Context context = this;
            Intent intent = new Intent(context, NoteDetailActivity.class);
            intent.putExtra(Util.ARG_OPEN_NOTE_FROM_LIST, rawNote);
            intent.putExtra(Util.ARG_SCRIPTURE, mScripture);

            context.startActivity(intent);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        multiSelect = true;
        menu.add("Delete");
    }
    private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    };

    @Nullable
    @Override
    public ActionMode startSupportActionMode(@NonNull ActionMode.Callback callback) {
        return super.startSupportActionMode(callback);
    }

    @Override
    public void onActionModeFinished(android.view.ActionMode mode) {
        super.onActionModeFinished(mode);

    }

    @Override
    public void onSupportActionModeStarted(@NonNull ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        Toast.makeText(this, "Context action mode support started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSupportActionModeFinished(@NonNull ActionMode mode) {
        super.onSupportActionModeFinished(mode);
        Toast.makeText(this, "Context action support finished", Toast.LENGTH_SHORT).show();
        multiSelect = false;
        selectedItems.clear();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Toast.makeText(this, "Context action", Toast.LENGTH_SHORT).show();

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpFromSameTask(this);
            return true;
        }
        if (id == R.id.createNote) {
            openNote(null);
            return true;
        }
        if (id == R.id.delete) {}
//            AlertDialog.Builder builder;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
//            } else {
//                builder = new AlertDialog.Builder(this);
//            }
//            builder.setTitle("Delete note")
//                    .setMessage("Are you sure you want to delete this note?")
//                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            // continue with delete
//
//                        }
//                    })
//                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            // do nothing
//                        }
//                    })
//                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .show();
//        }

        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new NotesAdapter());
    }

    public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

        public NotesAdapter() { }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.note_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Note note = Util.getNoteFromFile(Util.NOTES_DIR.listFiles()[position]);
            holder.mNote = note;
            if (note != null) {
                holder.mCreatedView.setText("created: "+Util.formatDate(Long.parseLong(note.created)));
                holder.mTitleView.setText(note.title);
                holder.mSpeakerView.setText(note.speaker);
                holder.mContentView.setText(Util.firstLine(note.content));
                holder.mModifiedView.setText("edited: "+Util.formatDate(Long.parseLong(note.modified)));

                if (note.speaker.equals(""))
                    holder.mSpeakerView.setVisibility(View.GONE);
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openNote(holder.mNote.rawNote);
                }
            });

            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ((AppCompatActivity)view.getContext()).startSupportActionMode(actionModeCallbacks);
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return Util.getCount(Util.NOTES_DIR);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mCreatedView, mSpeakerView, mTitleView, mModifiedView, mContentView;
            public Note mNote;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mCreatedView = (TextView) view.findViewById(R.id.created);
                mSpeakerView = (TextView) view.findViewById(R.id.speaker);
                mModifiedView = (TextView) view.findViewById(R.id.modified);
                mTitleView = (TextView) view.findViewById(R.id.title);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
