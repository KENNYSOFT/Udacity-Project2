package kr.KENNYSOFT.Udacity.Project2;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class MovieItem implements Parcelable
{
	String poster_path,title,release_date,plot_synopsis;
	double vote_average;
	int id;

	MovieItem(JSONObject object)
	{
		try
		{
			this.poster_path="http://image.tmdb.org/t/p/w185"+object.get("poster_path");
			this.title=(String)object.get("title");
			this.release_date=(String)object.get("release_date");
			this.vote_average=Double.parseDouble(object.get("vote_average").toString());
			this.plot_synopsis=(String)object.get("overview");
			this.id=Integer.parseInt(object.get("id").toString());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	MovieItem(int id,String poster_path)
	{
		this.poster_path=poster_path;
		this.id=id;
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest,int flags)
	{
		dest.writeString(poster_path);
		dest.writeString(title);
		dest.writeString(release_date);
		dest.writeDouble(vote_average);
		dest.writeString(plot_synopsis);
		dest.writeInt(id);
	}

	public static final Parcelable.Creator<MovieItem> CREATOR=new Parcelable.Creator<MovieItem>()
	{
		@Override
		public MovieItem createFromParcel(Parcel in)
		{
			return new MovieItem(in);
		}

		@Override
		public MovieItem[] newArray(int size)
		{
			return new MovieItem[size];
		}
	};

	MovieItem(Parcel in)
	{
		poster_path=in.readString();
		title=in.readString();
		release_date=in.readString();
		vote_average=in.readDouble();
		plot_synopsis=in.readString();
		id=in.readInt();
	}
}