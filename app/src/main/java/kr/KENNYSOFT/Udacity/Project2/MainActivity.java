package kr.KENNYSOFT.Udacity.Project2;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

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

public class MainActivity extends AppCompatActivity
{
	final String API_KEY="YOUR_API_KEY";

	MovieSQLite sql;
	GridView gridView;
	boolean mTwoPane;
	int mode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if(findViewById(R.id.fragment)!=null)mTwoPane=true;

		ActionBar actionBar=getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,getResources().getStringArray(R.array.mode)),new ActionBar.OnNavigationListener()
		{
			@Override
			public boolean onNavigationItemSelected(int itemPosition,long itemId)
			{
				mode=itemPosition;
				switch(itemPosition)
				{
				case 0:
					new MovieTask(MainActivity.this,gridView).execute("http://api.themoviedb.org/3/movie/popular?api_key="+API_KEY);
					break;
				case 1:
					new MovieTask(MainActivity.this,gridView).execute("http://api.themoviedb.org/3/movie/top_rated?api_key="+API_KEY);
					break;
				case 2:
					updateFavorite();
					break;
				}
				return true;
			}
		});

		sql=new MovieSQLite(this,"favorite.db",null,1);

		gridView=(GridView)findViewById(R.id.gridview);
		new MovieTask(this,gridView).execute("http://api.themoviedb.org/3/movie/popular?api_key="+API_KEY);
	}

	@Override
	public void onResume()
	{
		updateFavorite();
		super.onResume();
	}

	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}

	public void updateFavorite()
	{
		List<MovieItem> movieItems=new ArrayList<>();
		if(mode!=2)return;
		Cursor c=sql.getReadableDatabase().query("favorite",null,null,null,null,null,"_id DESC");
		while(c.moveToNext())movieItems.add(new MovieItem(c.getInt(c.getColumnIndex("id")),c.getString(c.getColumnIndex("poster_path"))));
		c.close();
		gridView.setAdapter(new MovieItemAdapter(MainActivity.this,movieItems));
	}
}

class MovieItemAdapter extends ArrayAdapter<MovieItem>
{
	Context context;

	MovieItemAdapter(Context context,List<MovieItem> movieItems)
	{
		super(context,0,movieItems);
		this.context=context;
	}

	@Override
	public View getView(int position,View convertView,ViewGroup parent)
	{
		final MovieItem movieItem=getItem(position);
		if(convertView==null)convertView=LayoutInflater.from(context).inflate(R.layout.list_item_poster,parent,false);
		ImageView imageView=(ImageView)convertView.findViewById(R.id.poster_image);
		Picasso.with(context).load(movieItem.poster_path).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(imageView);
		imageView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(((MainActivity)context).mTwoPane)
				{
					Bundle bundle=new Bundle();
					bundle.putParcelable("movieItem",movieItem);
					MovieFragment movieFragment=new MovieFragment();
					movieFragment.setArguments(bundle);
					((MainActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment,movieFragment).commit();
				}
				else context.startActivity(new Intent(context,MovieActivity.class).putExtra("movieItem",movieItem));
			}
		});
		return convertView;
	}
}

class MovieTask extends AsyncTask<String,Void,String>
{
	Context context;
	GridView gridView;
	List<MovieItem> movieItemList=new ArrayList<>();

	MovieTask(Context context,GridView gridView)
	{
		this.context=context;
		this.gridView=gridView;
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
			for(int i=0;i<results.length();++i)movieItemList.add(new MovieItem((JSONObject)results.get(i)));
			gridView.setAdapter(new MovieItemAdapter(context,movieItemList));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}