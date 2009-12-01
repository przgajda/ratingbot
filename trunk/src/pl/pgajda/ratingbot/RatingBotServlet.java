package pl.pgajda.ratingbot;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.wave.api.*;

@SuppressWarnings("serial")
public class RatingBotServlet extends AbstractRobotServlet {
	public static final String myId = "rating-bot_appspot.com";

	@Override
	public void processEvents(RobotMessageBundle bundle) {
		Wavelet wavelet = bundle.getWavelet();

		if (bundle.wasSelfAdded()) {
			Blip blip = wavelet.appendBlip();
			TextView textView = blip.getDocument();
			textView.appendMarkup("Hello. You may rate every new blip now.\n" +
					"Use <b>#!rb-changefont-no</b> to disable font changes and\n" +
					"<b>#!rb-changefont-yes</b> to re-enable them.");
		}

		for (Event e: bundle.getEvents()) {
			switch(e.getType()) {
				case BLIP_SUBMITTED:
					updatePreferences(e);

					int rating = getRating(e.getBlip().getDocument());
					updateVoteForm(e, rating);
					break;
					
				case FORM_BUTTON_CLICKED:
					vote(e);
					break;
			}
		}
	}
	
	private void updatePreferences(Event e)
	{
		TextView content = e.getBlip().getDocument();
		Wavelet wave = e.getWavelet();
		
		Pattern p = Pattern.compile("#!rb-changefont-(yes|no)");
		Matcher m = p.matcher(content.getText());
		if( m.matches() ) {
			String value = m.group(1);
			wave.setDataDocument("changefont", value);
			blipReply(e.getBlip(), "ChangeFont is now set to " + value);
		}
	}
	
	private void updateVoteForm(Event e, int rating)
	{	
		TextView content = e.getBlip().getDocument();
		String changeFontPref = e.getWavelet().getDataDocument("changefont");
		
		if( changeFontPref == null || changeFontPref.equals("yes") )
			setFontStyle(content, rating);
		
		content.getFormView().delete("voteM");
		content.getFormView().delete("voteP");
		content.getFormView().delete("ratingButton");
		
		if( !content.getText().endsWith("\n") )
			content.append("\n");
		
		int rangeStart = content.getText().length();
		
		FormElement plus = new FormElement(ElementType.BUTTON);
		plus.setName("voteP");
		plus.setValue("+");
		content.appendElement(plus);
		
		FormElement ratingButton = new FormElement(ElementType.BUTTON);
		ratingButton.setName("ratingButton");
		ratingButton.setValue(" [ " + String.format("%+d", rating) + " ] ");
		content.appendElement(ratingButton);
		
		FormElement minus = new FormElement(ElementType.BUTTON);
		minus.setName("voteM");
		minus.setValue("-");
		content.appendElement(minus);
		
		int rangeEnd = content.getText().length();
		
		content.setAnnotation(new Range(rangeStart, rangeEnd), "style/color", "rgb(100,100,100)");
		content.setAnnotation(new Range(rangeStart, rangeEnd), "style/fontSize", "10px");
	}
	
	private void vote(Event e)
	{
		TextView content = e.getBlip().getDocument();
		String voter = e.getModifiedBy().replace('@', '_');
		
		if( voter.equals(myId) )
			return ;
		
		int rating = getRating(content);

		int vote;
		List<Annotation> votes = content.getAnnotations("voter-"+voter);
		if( votes.size() > 0 )
			vote = Integer.parseInt(votes.get(0).getValue());
		else
			vote = 0;
		
		if( e.getButtonName().equals("voteP") ) {
			content.setAnnotation("voter-"+voter, "1");
			rating += 1-vote;
		}
		else if( e.getButtonName().equals("voteM") ) {
			content.setAnnotation("voter-"+voter, "-1");
			rating -= 1+vote;
		}
		
		content.setAnnotation("rating", ""+rating);
				
		updateVoteForm(e, rating);
	}
	
	private void setFontStyle(TextView content, int rating)
	{
		double fontSize = 12.0+5.0*Math.atan((double)rating/30.0);
		content.setAnnotation("style/fontSize", Math.round(fontSize)+"px");
		if( rating > 100 )
			content.setAnnotation("style/fontWeight", "bold");
		else if( rating < -50 )
			content.setAnnotation("style/color", "rgb(128,128,128)");		
	}
	
	private int getRating(TextView content) 
	{
		int rating = 0;
		
		try {
			List<Annotation> ratings = content.getAnnotations("rating");
		 
			if( ratings.size() > 0 ) {
				String ratingStr = ratings.get(0).getValue();
				rating = Integer.parseInt(ratingStr);
			}
			else {
				content.setAnnotation("rating", ""+rating);
			}
		} catch(NullPointerException ignore) {}
		
		return rating;
	}
	
	private void blipReply(Blip blip, String message)
	{
		blip.createChild().getDocument().append(message);
	}
}
