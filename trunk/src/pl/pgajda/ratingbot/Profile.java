package pl.pgajda.ratingbot;

import com.google.wave.api.ProfileServlet;

@SuppressWarnings("serial")
public class Profile extends ProfileServlet {
	@Override
	public String getRobotName() {
		return "RatingBot";
	}

	@Override
	public String getRobotAvatarUrl() {
		return "http://waveblog.pl/ratingbot.jpg";
	}

  	@Override
  	public String getRobotProfilePageUrl() {
  		return "http://waveblog.pl";
  	}
}
