/*******************************************************************************
 * Copyright (c) 2014 Helsingin Diakonissalaitos and the authors
 *  
 * The MIT License (MIT)
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *  
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *   
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS 
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.apps8os.motivator.ui;

import org.apps8os.motivator.R;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Represents an image carousel made with View Pager.
 * @author Toni Järvinen
 *
 */
public class ImagesPagerAdapter extends PagerAdapter  {
    
    private int[] mImages;
    private int[] mTitles;
    private Resources mResources;
    private Context mContext;
 
   
    public ImagesPagerAdapter(int[] images, Context context) {
    	super();
    	mImages = images;
    	mContext = context;
    	mResources = context.getResources();
    }
    
    public ImagesPagerAdapter(int[] images, int[] titles, Context context) {
    	this(images, context);
    	mTitles = titles;
    }

    @Override
    public void destroyItem(ViewGroup viewGroup, int position, Object object) {
        viewGroup.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mImages.length;
    }
    
    /**
     * Gets the text associated with the image.
     * Requires that the mTitles and mImages are in the same order.
     */
    @Override
    public String getPageTitle(int position) {
    	return mContext.getString(mTitles[position]);
    }

    @Override
    public Object instantiateItem(ViewGroup viewGroup, int position) {
    	// Get the ImageView by inflating it from layout xml
        ImageView carouselImage = (ImageView) View.inflate(viewGroup.getContext(), R.layout.element_mood_selection_image, null);
        
        // Set the correct image and a tag for the position
        carouselImage.setImageDrawable(mResources.getDrawable(mImages[position]));
        carouselImage.setTag(position);
        
        viewGroup.addView(carouselImage);
        
        // Return the view
        return carouselImage;
    }

    @Override
    public boolean isViewFromObject(View v, Object object) {
        return v == object;
    }
}
