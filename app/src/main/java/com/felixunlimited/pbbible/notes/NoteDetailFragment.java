package com.felixunlimited.pbbible.notes;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.felixunlimited.pbbible.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A fragment representing a single Util detail screen.
 * This fragment is either contained in a {@link NoteListActivity}
 * in two-pane mode (on tablets) or a {@link NoteDetailActivity}
 * on handsets.
 */
public class NoteDetailFragment extends Fragment{
    public EditText mCreatedView, mSpeakerView, mTitleView, mModifiedView, mContentView;
    FloatingActionButton fab;
    boolean mIsEnabled = true;
    public int INDEX_CREATED = 0;
    public int INDEX_TITLE = 1;
    public int INDEX_SPEAKER = 2;
    public int INDEX_CONTENT = 3;
    public int INDEX_MODIFIED = 4;

    /**
     * The dummy content this fragment is presenting.
     */
    private Note mNote;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NoteDetailFragment() {
    }

    final Handler handler = new Handler();
    Timer timer = new Timer();
    TimerTask doAsynchronousTask = new TimerTask() {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @SuppressWarnings("unchecked")
                public void run() {
                    try {
                        getNoteFromView();
                        if (mNote != null)
                            Util.saveNoteToFile(mNote);
                    }
                    catch (Exception e) {
                        // TODO Auto-generated catch block
                    }
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (savedInstanceState == null) {
            if (getArguments().getString(Util.ARG_OPEN_NOTE_FROM_LIST) != null) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                mNote = Note.extractNoteFromRaw(getArguments().getString(Util.ARG_OPEN_NOTE_FROM_LIST));
                getActivity().setTitle(mNote.title);
                if (getArguments().getString(Util.ARG_SCRIPTURE) != null) {
                    mNote.content += getArguments().getString(Util.ARG_SCRIPTURE);
                }
            }
        }
        else
            mNote = Note.extractNoteFromRaw(savedInstanceState.getString(Util.ARG_NOTE_BEING_EDITED));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.note_detail, container, false);
//getActivity().finish();
        // Show the dummy content as text in a EditText.
        fab = (FloatingActionButton) rootView.findViewById(R.id.edit_note);
//        String detailTitle
        String detailLabel = getString(R.string.title_note_detail);
        CharSequence parentActivity = getActivity().getTitle();
        if (getArguments().containsKey(Util.ARG_TWO_PANE))
            fab.setVisibility(View.GONE);
        mCreatedView = (EditText) rootView.findViewById(R.id.created);
        //mCreatedView.setEnabled(false);
        mTitleView = (EditText) rootView.findViewById(R.id.title);
        mSpeakerView = (EditText) rootView.findViewById(R.id.speaker);
        mContentView = (EditText) rootView.findViewById(R.id.content);
        mModifiedView = (EditText) rootView.findViewById(R.id.modified);
        mContentView.setEnabled(true);
        timer.schedule(doAsynchronousTask, 0, 5000);
        mContentView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                getNoteFromView();
                Util.saveNoteToFile(mNote);
            }
        });
        mContentView.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                                actionId == EditorInfo.IME_ACTION_DONE ||
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            if (!event.isShiftPressed()) {
                                // the user is done typing.
                                getNoteFromView();
                                if (mNote != null)
                                    Util.saveNoteToFile(mNote);
                                return true; // consume.
                            }
                        }
                        return false; // pass on to other listeners.
                    }
                });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNoteFromView();
                Util.saveNoteToFile(mNote);
                newNote();
            }
        });
        setText();
        setEnabled(true);
        if (mNote == null)
            newNote();

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
        getNoteFromView();
        if (mNote.content == null || mNote.content.isEmpty())
            Util.deleteNote(mNote);
        else
            Util.saveNoteToFile(mNote);
    }

    public void getNoteFromView() {
        //mNote.created = String.valueOf(mCreatedView.getText());
        mNote.title = String.valueOf(mTitleView.getText());
        mNote.speaker = String.valueOf(mSpeakerView.getText());
        mNote.content = String.valueOf(mContentView.getText());
        //mNote.modified = String.valueOf(mModifiedView.getText());
    }

    public void setEnabled (boolean flag) {
//        if (mIsEnabled)
        mCreatedView.setEnabled(false);
        mTitleView.setEnabled(flag);
        mSpeakerView.setEnabled(flag);
        mContentView.setEnabled(flag);
        mModifiedView.setEnabled(false);
        mIsEnabled = !flag;
//        if (flag)
//            fab.setVisibility(View.GONE);
//        else
//            fab.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Util.ARG_NOTE_BEING_EDITED, Note.getRaFromNotewNote(mNote));
    }

    public void newNote() {
        mNote = new Note(getContext());
        if (getArguments().getString(Util.ARG_SCRIPTURE) != null) {
            mNote.content += getArguments().getString(Util.ARG_SCRIPTURE);
        }
        setEnabled(true);
        setText();
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.notes_list, menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.createNote) {
//            newNote();
//            return true;
//        }
//        if (id == R.id.delete) {
//            Util.deleteNote(mNote);
//            startActivity(new Intent(getContext(), NoteListActivity.class));
//            getActivity().finish();
//            return true;
//        }
//        if (id == R.id.share) {
////            newNote();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    public void setText()
    {
        if (mNote != null) {
            mCreatedView.setText(mNote.created);
            mTitleView.setText(mNote.title);
            mSpeakerView.setText(mNote.speaker);
            mContentView.setText(mNote.content);
            mModifiedView.setText(mNote.modified);
        }
    }

//
//    @Override
//    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
//        return false;
//    }
//
//    @Override
//    public boolean onDoubleTap(MotionEvent motionEvent) {
//        setEnabled(true);
//        return true;
//    }
//
//    @Override
//    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
//        return false;
//    }
}
