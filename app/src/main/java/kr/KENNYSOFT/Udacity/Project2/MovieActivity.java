package kr.KENNYSOFT.Udacity.Project2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class MovieActivity extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_movie);

		Bundle bundle=new Bundle();
		bundle.putParcelable("movieItem",getIntent().getExtras().getParcelable("movieItem"));
		MovieFragment movieFragment=new MovieFragment();
		movieFragment.setArguments(bundle);

		getSupportFragmentManager().beginTransaction().replace(android.R.id.content,movieFragment).commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}