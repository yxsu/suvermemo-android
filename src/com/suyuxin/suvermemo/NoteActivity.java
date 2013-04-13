package com.suyuxin.suvermemo;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class NoteActivity extends FragmentActivity {

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	String notebook_guid;
	protected NoteDbAdapter database;
	SectionsPagerAdapter pager_adapter;
	protected Queue<Entry<String, NoteDbAdapter.NoteInfo>> queue_notes;
	protected RatingBar rating_bar;
	protected int count_total_note;//total number of notes in this notebook
	protected int count_total_today_note;
	
	protected OnClickListener listener_next_note = new OnClickListener()
	{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			//rating
			float rating = rating_bar.getRating() / rating_bar.getNumStars();
			queue_notes.remove();
			//set the next note
			pager_adapter.destroyAllItem();
			pager_adapter.notifyDataSetChanged();
			pager_adapter = new SectionsPagerAdapter(getSupportFragmentManager(), 
				queue_notes.element().getValue().title, 
				queue_notes.element().getValue().content);
			mViewPager.setAdapter(pager_adapter);
			pager_adapter.notifyDataSetChanged();
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note);
		//receive data
		notebook_guid = getIntent().getStringExtra("notebook_guid");
		count_total_note = getIntent().getIntExtra("notebook_count", 0);
		//create read date
		database = new NoteDbAdapter(this);
		updateNoteList();
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
	
		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		pager_adapter = new SectionsPagerAdapter(
				getSupportFragmentManager(), 
				queue_notes.element().getValue().title, 
				queue_notes.element().getValue().content);
		mViewPager.setAdapter(pager_adapter);
		

	}
	
	private void updateNoteList()
	{
		database.open();
		Iterator<Entry<String, NoteDbAdapter.NoteInfo>> iter = 
				database.getNote(notebook_guid).entrySet().iterator();
		queue_notes = new LinkedList<Entry<String, NoteDbAdapter.NoteInfo>>();
		while(iter.hasNext())
		{
			queue_notes.add(iter.next());
		}
		database.close();
		count_total_today_note = queue_notes.size();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.note, menu);
		return true;
	}
	
	

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private String[] contents;
		private String title;
		private FragmentTransaction current_transaction;
		public SectionsPagerAdapter(FragmentManager fm, String title, String content) {
			super(fm);
			this.title = title;
			contents = SplitNoteContent(content);
		}

		public void destroyAllItem()
		{
			current_transaction = getSupportFragmentManager().beginTransaction();
			for(int i = 0; i < getCount(); i++)
			{
				Object object = this.instantiateItem(mViewPager, i);
				if(object != null)
				{
					current_transaction.remove((Fragment)object);
				}
			}
			current_transaction.commit();
			getSupportFragmentManager().executePendingTransactions();
		}
		
		private String[] SplitNoteContent(String content)
		{
			//set font size
			int position = content.indexOf("<en-note style=") + 16;
			content = content.substring(0, position) + "font-size: "
					+ getResources().getDimensionPixelSize(R.dimen.note_font_size) 
					+ "pt; " + content.substring(position);
			Log.i("SplitNoteContent", content);
			//find answers
			Pattern font_style = Pattern.compile("<font color.*?</font>");
			Matcher matcher = font_style.matcher(content);
			List<String> answer = new ArrayList<String>();
			while(matcher.find())
			{
				String result = matcher.group();
				result = result.substring(22, result.length() - 7);
				if(result.startsWith("<") && result.endsWith("/>"))
					continue;
				
				answer.add(result);
			}
			//replace
			String[] questions = new String[answer.size() + 1];
			questions[questions.length - 1] = content;
			for(int index = answer.size() - 1; index >= 0; index--)
			{
				content = content.replace(answer.get(index), "[......]");
				questions[index] = content;
			}
			return questions;
		}
		
		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			if(position == 0)
			{
				args.putString(DummySectionFragment.ARG_SECTION_TYPE,
						DummySectionFragment.ARG_SECTION_HEAD);
			}else if(position == getCount() - 1)
			{
				args.putString(DummySectionFragment.ARG_SECTION_TYPE,
						DummySectionFragment.ARG_SECTION_TAIL);
			}else
			{
				args.putString(DummySectionFragment.ARG_SECTION_TYPE,
						DummySectionFragment.ARG_SECTION_NORMAL);
				args.putString(DummySectionFragment.ARG_SECTION_CONTENT,
						contents[position - 1]);
			}
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return contents.length + 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			if(position == getCount() - 1)
				return getResources().getString(R.string.titile_evaluation_statistics);
			
			if(position % 2 == 0)
				return title;
			else
				return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_CONTENT = "section_content";
		public static final String ARG_SECTION_TYPE = "section_type";
		public static final String ARG_SECTION_HEAD = "section_head";
		public static final String ARG_SECTION_NORMAL = "section_normal";
		public static final String ARG_SECTION_TAIL = "section_tail";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			if(getArguments().getString(ARG_SECTION_TYPE) == ARG_SECTION_NORMAL)
			{
				View rootView = inflater.inflate(R.layout.fragment_note_dummy,
						container, false);
				WebView view = (WebView) rootView
						.findViewById(R.id.section_label);
				//dummyTextView.setText(getArguments().getString(ARG_SECTION_CONTENT));
				view.loadData(getArguments().getString(ARG_SECTION_CONTENT),
						"text/html; charset=UTF-8", null);
				return rootView;
			}else if(getArguments().getString(ARG_SECTION_TYPE) == ARG_SECTION_TAIL)
			{
				View rootView = inflater.inflate(R.layout.fragment_note_tail, container, false);
				rating_bar = (RatingBar)rootView.findViewById(R.id.ratingBar_study_result);
				Button next = (Button)rootView.findViewById(R.id.button_note_next);
				next.setOnClickListener(listener_next_note);
				TextView statistics_info = (TextView)rootView.findViewById(R.id.textView_note_statistics);
				statistics_info.setText("Finished: " + (count_total_today_note - queue_notes.size()) + "\n"
						+"Today's Task: " + count_total_today_note + "\n"
						+"Notebook: " + count_total_note);
				return rootView;
			}
			else
			{
				View rootView = inflater.inflate(R.layout.fragment_note_head, container, false);
				
				return rootView;
			}
		}
	}

}
