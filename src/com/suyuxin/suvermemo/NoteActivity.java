package com.suyuxin.suvermemo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

public class NoteActivity extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	String notebook_guid;
	protected NoteDbAdapter database;
	protected List<Entry<String, NoteDbAdapter.NoteInfo>> list_notes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note);
		//receive data
		notebook_guid = getIntent().getStringExtra("notebook_guid");
		//create read date
		database = new NoteDbAdapter(this);
		updateNoteList();
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager(), 
				list_notes.get(0).getValue().title, 
				SplitNoteContent(list_notes.get(0).getValue().content));

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

	}
	
	private void updateNoteList()
	{
		database.open();
		Iterator<Entry<String, NoteDbAdapter.NoteInfo>> iter = 
				database.getNote(notebook_guid).entrySet().iterator();
		list_notes = new LinkedList<Entry<String, NoteDbAdapter.NoteInfo>>();
		while(iter.hasNext())
		{
			list_notes.add(iter.next());
		}
		database.close();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.note, menu);
		return true;
	}
	
	private String[] SplitNoteContent(String content)
	{
		//set font size
		int position = content.indexOf("<en-note style=") + 16;
		content = content.substring(0, position) + "font-size: "
				+ getResources().getDimensionPixelSize(R.dimen.note_font_size) 
				+ "pt; " + content.substring(position);
		Log.i("NoteActivity", content);
		//find answers
		Pattern font_style = Pattern.compile("<font color.*?</font>");
		Matcher matcher = font_style.matcher(content);
		List<String> answer = new ArrayList<String>();
		while(matcher.find())
		{
			String result = matcher.group();
			result = result.substring(22, result.length() - 7);
			answer.add(result);
		}
		String[] questions = new String[answer.size() + 1];
		questions[questions.length - 1] = content;
		for(int index = answer.size() - 1; index >= 0; index--)
		{
			content = content.replace(answer.get(index), "[......]");
			questions[index] = content;
		}
		return questions;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private String[] contents;
		private String title;
		public SectionsPagerAdapter(FragmentManager fm, String title, String[] contents) {
			super(fm);
			this.title = title;
			this.contents = contents;
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
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
	public static class DummySectionFragment extends Fragment {
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
			View rootView = inflater.inflate(R.layout.fragment_note_dummy,
					container, false);
			if(getArguments().getString(ARG_SECTION_TYPE) == ARG_SECTION_NORMAL)
			{
				WebView view = (WebView) rootView
						.findViewById(R.id.section_label);
				//dummyTextView.setText(getArguments().getString(ARG_SECTION_CONTENT));
				view.loadData(getArguments().getString(ARG_SECTION_CONTENT),
						"text/html", null);
				
			}
			return rootView;
		}
	}

}
