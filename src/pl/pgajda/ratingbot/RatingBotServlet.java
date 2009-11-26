package pl.pgajda.ratingbot;

import java.util.List;
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
			textView.append("Hello :)");
		}

		for (Event e: bundle.getEvents()) {
			TextView content = e.getBlip().getDocument();
			
			switch(e.getType()) {
				case BLIP_SUBMITTED:
					updateVoteForm(content, getRating(content));
					break;
					
				case FORM_BUTTON_CLICKED:
					vote(content, e);
					break;
			}
		}
	}
	
	private void updateVoteForm(TextView content, int rating)
	{	
		double fontSize = 12.0+5.0*Math.atan((double)rating/30.0);
		content.setAnnotation("style/fontSize", Math.round(fontSize)+"px");
		if( rating > 100 )
			content.setAnnotation("style/fontWeight", "bold");
		else if( rating < -50 )
			content.setAnnotation("style/color", "rgb(128,128,128)");
		
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
	
	private void vote(TextView content, Event e)
	{
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
				
		updateVoteForm(content, rating);
	}
	
	private int getRating(TextView content) 
	{
		int rating;
		
		List<Annotation> ratings = content.getAnnotations("rating");
		if( ratings.size() > 0 ) {
			String ratingStr = ratings.get(0).getValue();
			rating = Integer.parseInt(ratingStr);
		}
		else {
			rating = 0;
			content.setAnnotation("rating", ""+rating);
		}
		
		return rating;
	}
}
