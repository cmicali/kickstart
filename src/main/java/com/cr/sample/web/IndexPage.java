package com.cr.sample.web;

import com.cr.kickstart.stripes.WebPageBase;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * Author: chrismicali
 */
@UrlBinding("/index")
public class IndexPage extends WebPageBase {

    @DefaultHandler
    public Resolution index() {
        return new ForwardResolution("/WEB-INF/jsp/index.jsp");
    }

}
