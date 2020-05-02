package com.akbolatss.workshop.slidingpanel.sampleapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.psoffritti.librarysampleapptemplate.core.Constants
import com.psoffritti.librarysampleapptemplate.core.SampleAppTemplateActivity
import com.psoffritti.librarysampleapptemplate.core.utils.ExampleActivityDetails
import com.akbolatss.workshop.slidingpanel.sampleapp.examples.advanced.AdvancedExampleActivity
import com.akbolatss.workshop.slidingpanel.sampleapp.examples.basic.BasicExampleHorizontalActivity
import com.akbolatss.workshop.slidingpanel.sampleapp.examples.basic.BasicExampleVerticalActivity
import com.akbolatss.workshop.slidingpanel.sampleapp.examples.dragview.DragViewExampleActivity
import com.akbolatss.workshop.slidingpanel.sampleapp.examples.fittoscreenview.FitToScreenViewExampleActivity
import com.akbolatss.workshop.slidingpanel.sampleapp.examples.scrollableview.ScrollableViewExampleActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, SampleAppTemplateActivity::class.java)

        intent.putExtra(Constants.TITLE.name, getString(R.string.app_name))
        intent.putExtra(Constants.GITHUB_URL.name, "https://github.com/iRYO400/sliding-panel")
        intent.putExtra(Constants.HOMEPAGE_URL.name, "https://github.com/iRYO400/sliding-panel/blob/master/README.md")
        intent.putExtra(Constants.PLAYSTORE_PACKAGE_NAME.name, "com.akbolatss.workshop.slidingpanel.sampleapp")

        val examples = arrayOf(
            ExampleActivityDetails(
                R.string.basic_example_vertical,
                null,
                BasicExampleVerticalActivity::class.java
            ),
            ExampleActivityDetails(
                R.string.basic_example_horizontal,
                null,
                BasicExampleHorizontalActivity::class.java
            ),
            ExampleActivityDetails(
                R.string.drag_view_example,
                null,
                DragViewExampleActivity::class.java
            ),
            ExampleActivityDetails(
                R.string.fit_to_screen_view_example,
                null,
                FitToScreenViewExampleActivity::class.java
            ),
            ExampleActivityDetails(
                R.string.scrollable_view_example,
                null,
                ScrollableViewExampleActivity::class.java
            ),
            ExampleActivityDetails(
                R.string.advanced_example_activity,
                null,
                AdvancedExampleActivity::class.java
            )
        )

        intent.putExtra(Constants.EXAMPLES.name, examples)

        startActivity(intent)
        finish()
    }
}
