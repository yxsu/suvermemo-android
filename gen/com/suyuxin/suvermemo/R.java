/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * aapt tool from the resource data it found.  It
 * should not be modified by hand.
 */

package com.suyuxin.suvermemo;

public final class R {
    public static final class attr {
        /** <p>Must be a reference to another resource, in the form "<code>@[+][<i>package</i>:]<i>type</i>:<i>name</i></code>"
or to a theme attribute in the form "<code>?[<i>package</i>:][<i>type</i>:]<i>name</i></code>".
         */
        public static final int buttonBarButtonStyle=0x7f010001;
        /** <p>Must be a reference to another resource, in the form "<code>@[+][<i>package</i>:]<i>type</i>:<i>name</i></code>"
or to a theme attribute in the form "<code>?[<i>package</i>:][<i>type</i>:]<i>name</i></code>".
         */
        public static final int buttonBarStyle=0x7f010000;
    }
    public static final class color {
        public static final int black_overlay=0x7f040000;
    }
    public static final class dimen {
        /**  Default screen margins, per the Android Design guidelines. 

         Customize dimensions originally defined in res/values/dimens.xml (such as
         screen margins) for sw720dp devices (e.g. 10" tablets) in landscape here.
    
         */
        public static final int activity_horizontal_margin=0x7f050000;
        public static final int activity_vertical_margin=0x7f050001;
        public static final int note_activity_title_size=0x7f050002;
        public static final int note_font_size=0x7f050003;
    }
    public static final class drawable {
        public static final int ic_launcher=0x7f020000;
        public static final int notebook=0x7f020001;
        public static final int row_background=0x7f020002;
        public static final int row_background_default=0x7f020003;
        public static final int row_background_pressed=0x7f020004;
    }
    public static final class id {
        public static final int action_settings=0x7f09000b;
        public static final int button_notebook_enter=0x7f090008;
        public static final int button_notebook_sync=0x7f090009;
        public static final int esdk__switch_service=0x7f09000a;
        public static final int esdk__webview=0x7f090004;
        public static final int image_notebook=0x7f090006;
        public static final int listView_notebook=0x7f090001;
        public static final int pager=0x7f090002;
        public static final int pager_title_strip=0x7f090003;
        public static final int progressBar_sync=0x7f090000;
        public static final int section_label=0x7f090005;
        public static final int text_notebook_name=0x7f090007;
    }
    public static final class layout {
        public static final int activity_main=0x7f030000;
        public static final int activity_note=0x7f030001;
        public static final int esdk__webview=0x7f030002;
        public static final int fragment_note_dummy=0x7f030003;
        public static final int notebook_row=0x7f030004;
    }
    public static final class menu {
        public static final int esdk__oauth=0x7f080000;
        public static final int note=0x7f080001;
    }
    public static final class string {
        public static final int action_settings=0x7f060001;
        public static final int app_name=0x7f060000;
        public static final int dummy_button=0x7f060015;
        public static final int dummy_content=0x7f060016;
        public static final int error_create_notestore=0x7f06000a;
        public static final int error_download_note=0x7f060012;
        public static final int error_list_notebooks=0x7f060009;
        public static final int esdk__err_api_setup=0x7f060010;
        public static final int esdk__err_authentication=0x7f06000f;
        public static final int esdk__err_protocol_version=0x7f06000e;
        public static final int esdk__evernote=0x7f06000b;
        public static final int esdk__evernote_login_failed=0x7f06000c;
        public static final int esdk__evernote_login_successful=0x7f06000d;
        public static final int esdk__loading=0x7f060011;
        public static final int hello_world=0x7f060002;
        public static final int text_download_note_finished=0x7f060013;
        public static final int text_empty_notebook_list=0x7f060004;
        public static final int text_load=0x7f060008;
        public static final int text_notebook_enter=0x7f06001a;
        public static final int text_notebook_in_sync=0x7f060006;
        public static final int text_notebook_sync=0x7f060005;
        public static final int title_activity_data=0x7f060007;
        public static final int title_activity_note=0x7f060014;
        public static final int title_activity_notebook_list=0x7f060003;
        public static final int title_section1=0x7f060017;
        public static final int title_section2=0x7f060018;
        public static final int title_section3=0x7f060019;
    }
    public static final class style {
        /** 
        Base application theme, dependent on API level. This theme is replaced
        by AppBaseTheme from res/values-vXX/styles.xml on newer devices.
    

            Theme customizations available in newer API levels can go in
            res/values-vXX/styles.xml, while customizations related to
            backward-compatibility can go here.
        

        Base application theme for API 11+. This theme completely replaces
        AppBaseTheme from res/values/styles.xml on API 11+ devices.
    
 API 11 theme customizations can go here. 

        Base application theme for API 14+. This theme completely replaces
        AppBaseTheme from BOTH res/values/styles.xml and
        res/values-v11/styles.xml on API 14+ devices.
    
 API 14 theme customizations can go here. 
         */
        public static final int AppBaseTheme=0x7f070000;
        /**  Application theme. 
 All customizations that are NOT specific to a particular API-level can go here. 
         */
        public static final int AppTheme=0x7f070001;
        public static final int ButtonBar=0x7f070003;
        public static final int ButtonBarButton=0x7f070004;
        public static final int FullscreenActionBarStyle=0x7f070005;
        public static final int FullscreenTheme=0x7f070002;
    }
    public static final class styleable {
        /** 
         Declare custom theme attributes that allow changing which styles are
         used for button bars depending on the API level.
         ?android:attr/buttonBarStyle is new as of API 11 so this is
         necessary to support previous API levels.
    
           <p>Includes the following attributes:</p>
           <table>
           <colgroup align="left" />
           <colgroup align="left" />
           <tr><th>Attribute</th><th>Description</th></tr>
           <tr><td><code>{@link #ButtonBarContainerTheme_buttonBarButtonStyle com.suyuxin.suvermemo:buttonBarButtonStyle}</code></td><td></td></tr>
           <tr><td><code>{@link #ButtonBarContainerTheme_buttonBarStyle com.suyuxin.suvermemo:buttonBarStyle}</code></td><td></td></tr>
           </table>
           @see #ButtonBarContainerTheme_buttonBarButtonStyle
           @see #ButtonBarContainerTheme_buttonBarStyle
         */
        public static final int[] ButtonBarContainerTheme = {
            0x7f010000, 0x7f010001
        };
        /**
          <p>This symbol is the offset where the {@link com.suyuxin.suvermemo.R.attr#buttonBarButtonStyle}
          attribute's value can be found in the {@link #ButtonBarContainerTheme} array.


          <p>Must be a reference to another resource, in the form "<code>@[+][<i>package</i>:]<i>type</i>:<i>name</i></code>"
or to a theme attribute in the form "<code>?[<i>package</i>:][<i>type</i>:]<i>name</i></code>".
          @attr name android:buttonBarButtonStyle
        */
        public static final int ButtonBarContainerTheme_buttonBarButtonStyle = 1;
        /**
          <p>This symbol is the offset where the {@link com.suyuxin.suvermemo.R.attr#buttonBarStyle}
          attribute's value can be found in the {@link #ButtonBarContainerTheme} array.


          <p>Must be a reference to another resource, in the form "<code>@[+][<i>package</i>:]<i>type</i>:<i>name</i></code>"
or to a theme attribute in the form "<code>?[<i>package</i>:][<i>type</i>:]<i>name</i></code>".
          @attr name android:buttonBarStyle
        */
        public static final int ButtonBarContainerTheme_buttonBarStyle = 0;
    };
}
