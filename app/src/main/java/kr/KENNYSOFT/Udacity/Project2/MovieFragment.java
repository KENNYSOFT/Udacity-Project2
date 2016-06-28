package kr.KENNYSOFT.Udacity.Project2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MovieFragment extends Fragment
{
	final String API_KEY="YOUR_API_KEY";

	MovieSQLite sql;
	MovieItem movieItem;
	TextView movie_title,movie_release_date,movie_vote_average,movie_plot_synopsis;
	ListView trailers,reviews;
	ScrollView scrollView;

	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
	{
		View view=inflater.inflate(R.layout.fragment_movie,container,false);
		
		sql=new MovieSQLite(getActivity(),"favorite.db",null,1);

		movie_title=(TextView)view.findViewById(R.id.movie_title);
		movie_release_date=(TextView)view.findViewById(R.id.movie_release_date);
		movie_vote_average=(TextView)view.findViewById(R.id.movie_vote_average);
		movie_plot_synopsis=(TextView)view.findViewById(R.id.movie_plot_synopsis);

		movieItem=this.getArguments().getParcelable("movieItem");
		Picasso.with(getActivity()).load(movieItem.poster_path).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into((ImageView)view.findViewById(R.id.movie_poster));

		scrollView=(ScrollView)view.findViewById(R.id.scrollview);

		trailers=(ListView)view.findViewById(R.id.movie_trailer);
		reviews=(ListView)view.findViewById(R.id.movie_review);
		new InfoTask(getActivity(),this).execute("http://api.themoviedb.org/3/movie/"+movieItem.id+"?api_key="+API_KEY);
		new TrailerTask(getActivity(),this).execute("http://api.themoviedb.org/3/movie/"+movieItem.id+"/videos?api_key="+API_KEY);
		new ReviewTask(getActivity(),this).execute("http://api.themoviedb.org/3/movie/"+movieItem.id+"/reviews?api_key="+API_KEY);
		
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		menu.clear();
		Cursor c=sql.getReadableDatabase().query("favorite",null,"id="+movieItem.id,null,null,null,null);
		if(c.getCount()==0)menu.add(0,Menu.FIRST,Menu.NONE,R.string.favorite).setIcon(android.R.drawable.btn_star_big_off).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		else menu.add(0,Menu.FIRST+1,Menu.NONE,R.string.favorite).setIcon(android.R.drawable.btn_star_big_on).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		c.close();
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case Menu.FIRST:
			ContentValues values=new ContentValues();
			values.put("id",movieItem.id);
			values.put("poster_path",movieItem.poster_path);
			sql.getWritableDatabase().insert("favorite",null,values);
			getActivity().invalidateOptionsMenu();
			if(getActivity().getClass()==MainActivity.class)((MainActivity)getActivity()).updateFavorite();
			return true;
		case Menu.FIRST+1:
			sql.getWritableDatabase().delete("favorite","id="+movieItem.id,null);
			getActivity().invalidateOptionsMenu();
			if(getActivity().getClass()==MainActivity.class)((MainActivity)getActivity()).updateFavorite();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}

class InfoTask extends AsyncTask<String,Void,String>
{
	Context context;
	MovieFragment fragment;

	InfoTask(Context context,MovieFragment fragment)
	{
		this.context=context;
		this.fragment=fragment;
	}

	@Override
	protected String doInBackground(String... urls)
	{
		String html="";
		try
		{
			URLConnection connection=new URL(urls[0]).openConnection();
			InputStream is=connection.getInputStream();
			BufferedReader in=new BufferedReader(new InputStreamReader(is));
			String line;
			while((line=in.readLine())!=null)html=html+line+"\n";
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return html;
	}

	@Override
	protected void onPostExecute(String html)
	{
		try
		{
			JSONObject json=new JSONObject(html);
			fragment.movie_title.setText((String)json.get("title"));
			fragment.movie_release_date.setText(((String)json.get("release_date")).substring(0,4));
			fragment.movie_vote_average.setText(String.format(Locale.getDefault(),context.getString(R.string.movie_vote_format),Double.parseDouble(json.get("vote_average").toString())));
			fragment.movie_plot_synopsis.setText((String)json.get("overview"));
			fragment.scrollView.scrollTo(0,0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

class TrailerItem
{
	String name,key;

	TrailerItem(JSONObject object)
	{
		try
		{
			this.name=(String)object.get("name");
			this.key=(String)object.get("key");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

class TrailerItemAdapter extends ArrayAdapter<TrailerItem>
{
	Context context;

	TrailerItemAdapter(Context context,List<TrailerItem> trailers)
	{
		super(context,0,trailers);
		this.context=context;
	}

	@Override
	public View getView(int position,View convertView,ViewGroup parent)
	{
		final TrailerItem trailerItem=getItem(position);
		if(convertView==null)convertView=LayoutInflater.from(context).inflate(R.layout.list_item_trailer,parent,false);
		((TextView)convertView.findViewById(R.id.trailer_name)).setText(trailerItem.name);
		convertView.findViewById(R.id.trailer_name).setSelected(true);
		convertView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				context.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com/watch?v="+trailerItem.key)));
			}
		});
		return convertView;
	}
}

class TrailerTask extends AsyncTask<String,Void,String>
{
	Context context;
	MovieFragment fragment;
	List<TrailerItem> trailerItemList=new ArrayList<>();

	TrailerTask(Context context,MovieFragment fragment)
	{
		this.context=context;
		this.fragment=fragment;
	}

	@Override
	protected String doInBackground(String... urls)
	{
		String html="";
		try
		{
			URLConnection connection=new URL(urls[0]).openConnection();
			InputStream is=connection.getInputStream();
			BufferedReader in=new BufferedReader(new InputStreamReader(is));
			String line;
			while((line=in.readLine())!=null)html=html+line+"\n";
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return html;
	}

	@Override
	protected void onPostExecute(String html)
	{
		try
		{
			JSONObject json=new JSONObject(html);
			JSONArray results=(JSONArray)json.get("results");
			for(int i=0;i<results.length();++i)trailerItemList.add(new TrailerItem((JSONObject)results.get(i)));
			ListView listView=fragment.trailers;
			listView.setAdapter(new TrailerItemAdapter(context,trailerItemList));
			int totalHeight=0;
			int desiredWidth=View.MeasureSpec.makeMeasureSpec(listView.getWidth(),View.MeasureSpec.AT_MOST);
			for(int i=0;i<results.length();++i)
			{
				View listItem=listView.getAdapter().getView(i,null,listView);
				listItem.measure(desiredWidth,View.MeasureSpec.UNSPECIFIED);
				totalHeight=totalHeight+listItem.getMeasuredHeight();
			}
			ViewGroup.LayoutParams params=listView.getLayoutParams();
			params.height=totalHeight+(listView.getDividerHeight()*(results.length()-1));
			listView.setLayoutParams(params);
			listView.requestLayout();
			fragment.scrollView.scrollTo(0,0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

class ReviewItem
{
	String author,content;

	ReviewItem(JSONObject object)
	{
		try
		{
			this.author=(String)object.get("author");
			this.content=(String)object.get("content");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

class ReviewItemAdapter extends ArrayAdapter<ReviewItem>
{
	Context context;

	ReviewItemAdapter(Context context,List<ReviewItem> Reviews)
	{
		super(context,0,Reviews);
		this.context=context;
	}

	@Override
	public View getView(int position,View convertView,ViewGroup parent)
	{
		final ReviewItem ReviewItem=getItem(position);
		if(convertView==null)convertView=LayoutInflater.from(context).inflate(R.layout.list_item_review,parent,false);
		((TextView)convertView.findViewById(R.id.review_author)).setText(ReviewItem.author);
		((TextView)convertView.findViewById(R.id.review_content)).setText(ReviewItem.content);
		return convertView;
	}
}

class ReviewTask extends AsyncTask<String,Void,String>
{
	Context context;
	MovieFragment fragment;
	List<ReviewItem> ReviewItemList=new ArrayList<>();

	ReviewTask(Context context,MovieFragment fragment)
	{
		this.context=context;
		this.fragment=fragment;
	}

	@Override
	protected String doInBackground(String... urls)
	{
		String html="";
		try
		{
			URLConnection connection=new URL(urls[0]).openConnection();
			InputStream is=connection.getInputStream();
			BufferedReader in=new BufferedReader(new InputStreamReader(is));
			String line;
			while((line=in.readLine())!=null)html=html+line+"\n";
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return html;
	}

	@Override
	protected void onPostExecute(String html)
	{
		try
		{
			JSONObject json=new JSONObject(html);
			JSONArray results=(JSONArray)json.get("results");
			for(int i=0;i<results.length();++i)ReviewItemList.add(new ReviewItem((JSONObject)results.get(i)));
			ListView listView=fragment.reviews;
			listView.setAdapter(new ReviewItemAdapter(context,ReviewItemList));
			int totalHeight=0;
			int desiredWidth=View.MeasureSpec.makeMeasureSpec(listView.getWidth(),View.MeasureSpec.AT_MOST);
			for(int i=0;i<results.length();++i)
			{
				View listItem=listView.getAdapter().getView(i,null,listView);
				listItem.measure(desiredWidth,View.MeasureSpec.UNSPECIFIED);
				totalHeight=totalHeight+listItem.getMeasuredHeight();
			}
			ViewGroup.LayoutParams params=listView.getLayoutParams();
			params.height=totalHeight+(listView.getDividerHeight()*(results.length()-1));
			listView.setLayoutParams(params);
			listView.requestLayout();
			fragment.scrollView.scrollTo(0,0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}