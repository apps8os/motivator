package org.apps8os.motivator.ui;

import org.apps8os.motivator.R;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GuidePageFragment extends Fragment{
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.fragment_guide_page, viewGroup, false);
		
		
		int guidePage = getArguments().getInt(StartGuideActivity.GUIDE_PAGE);
		
		switch (guidePage) {
		case 0:
			((TextView) rootView.findViewById(R.id.title)).setText("Tervetuloa!");
			((TextView) rootView.findViewById(R.id.content)).setText(Html.fromHtml("Motivaattori on tarkoitettu <b>Sinulle</b>."));
			break;
		case 1:
			((TextView) rootView.findViewById(R.id.title)).setText("Jaksot");
			((TextView) rootView.findViewById(R.id.content)).setText(Html.fromHtml("Motivaattorissa pääset seuraamaan <b>fiilistäsi</b> ja suunnittelemaan" +
					" juomistasi jaksoittain."));
			break;
		case 2:
			((TextView) rootView.findViewById(R.id.title)).setText("Motivaattori");
			((TextView) rootView.findViewById(R.id.content)).setText(Html.fromHtml("Ensiksi aloitamme <b>jakson</b> Sinulle. Pääset seuraavaksi valitsemaan" +
					" jakson pituuden ja nimen."));
		}
		
		return rootView;
		
	}

}
